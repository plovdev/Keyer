package org.plovdev.keyer.implementations.win;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.exceptions.KeyerException;
import org.plovdev.keyer.utils.CharactersUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import static org.plovdev.keyer.implementations.win.WindowsNativeUtils.*;
import static org.plovdev.keyer.utils.NativeUtils.LINKER;
import static org.plovdev.keyer.utils.NativeUtils.findFunction;

/**
 * Windows Credential Manager native interface using Windows API.
 * <p>
 * Provides direct access to Windows Vault for storing, retrieving and deleting
 * generic credentials via CredReadW/CredWriteW/CredDeleteW functions.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.0
 */
public final class WindowsKeychainNative {
    private static final Logger log = LoggerFactory.getLogger(WindowsKeychainNative.class);

    private static final int CRED_TYPE_GENERIC = 1;
    private static final int CRED_PERSIST_LOCAL_MACHINE = 2;
    private static final String AUTHENTICATION_METHOD = "AUTH_METHOD";

    private static final StructLayout ATTRIBUTE_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("Keyword"),
            ValueLayout.JAVA_INT.withName("Flags"),
            ValueLayout.JAVA_INT.withName("ValueSize"),
            ValueLayout.ADDRESS.withName("Value")
    );

    private static final MethodHandle GET_PASSWORD = findFunction(ADVAPI32, LINKER, "CredReadW", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), GET_LAST_ERROR_OPTION);
    private static final MethodHandle SET_PASSWORD = findFunction(ADVAPI32, LINKER, "CredWriteW", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), GET_LAST_ERROR_OPTION);
    private static final MethodHandle DELETE_PASSWORD = findFunction(ADVAPI32, LINKER, "CredDeleteW", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT), GET_LAST_ERROR_OPTION);
    private static final MethodHandle CLEAN_PASSWORD = findFunction(ADVAPI32, LINKER, "CredFree", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    private static final StructLayout CREDENTIAL_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("Flags"),
            ValueLayout.JAVA_INT.withName("Type"),
            ValueLayout.ADDRESS.withName("TargetName"),
            ValueLayout.ADDRESS.withName("Comment"),
            MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("dwLowDateTime"), ValueLayout.JAVA_INT.withName("dwHighDateTime")).withName("LastWritten"),
            ValueLayout.JAVA_INT.withName("CredentialBlobSize"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withName("CredentialBlob"),
            ValueLayout.JAVA_INT.withName("Persist"),
            ValueLayout.JAVA_INT.withName("AttributeCount"),
            ValueLayout.ADDRESS.withName("Attributes"),
            ValueLayout.ADDRESS.withName("TargetAlias"),
            ValueLayout.ADDRESS.withName("UserName")
    );

    private static final long ATTR_KEYWORD_OFFSET = ATTRIBUTE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Keyword"));
    private static final long ATTR_FLAGS_OFFSET = ATTRIBUTE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Flags"));
    private static final long ATTR_VALUE_SIZE_OFFSET = ATTRIBUTE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("ValueSize"));
    private static final long ATTR_VALUE_OFFSET = ATTRIBUTE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Value"));

    private static final long ATTR_COUNT_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("AttributeCount"));
    private static final long ATTRIBUTES_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Attributes"));

    private static final long TYPE_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Type"));
    private static final long TARGET_NAME_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("TargetName"));
    private static final long USER_NAME_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("UserName"));
    private static final long CRED_BLOB_SIZE_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("CredentialBlobSize"));
    private static final long CRED_BLOB_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("CredentialBlob"));
    private static final long PERSIST_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Persist"));

    /**
     * Private constructor to prevent instantiation.
     */
    private WindowsKeychainNative() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves a password as a char array.
     * <p>
     * Converts the raw UTF-16LE byte data from Windows Credential Manager
     * to a char array. The raw byte array is zeroed immediately after conversion.
     *
     * @param appId the application identifier (service name).
     * @param alias the credential alias (account name).
     * @return password as char array (UTF-16LE encoded), or {@code null} if not found.
     * @throws KeyerException if a native call fails
     */
    public static char @Nullable [] getPassword(String appId, String alias) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);

        byte[] rawPassword = getRawPassword(appId, alias);
        if (rawPassword == null) {
            return null;
        }

        try {
            return CharactersUtils.bytesToCharsUTF_16LE(rawPassword);
        } finally {
            Arrays.fill(rawPassword, (byte) 0);
        }
    }

    /**
     * Retrieves a password as a byte array directly from Windows Credential Manager.
     * <p>
     * Performs a synchronous lookup using {@code CredReadW} with the
     * {@link #CRED_TYPE_GENERIC} credential type.
     *
     * @param appId the application identifier.
     * @param alias the credential alias.
     * @return password as byte array, or {@code null} if not found.
     * @throws KeyerException if the Windows API call fails.
     */
    public static synchronized byte @Nullable [] getRawPassword(String appId, String alias) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            MemorySegment targetSegment = allocateTargetName(arena, appId, alias);
            MemorySegment credPtrSegment = arena.allocate(ValueLayout.ADDRESS);

            MemorySegment lastError = arena.allocate(CAPTURED_STATE_LAYOUT);
            int status = (int) GET_PASSWORD.invokeExact(lastError, targetSegment, CRED_TYPE_GENERIC, 0, credPtrSegment);
            if (status == 0) {
                int errorCode = getLastError(lastError);
                if (errorCode == NOT_FOUND) return null;
                throwException(errorCode);
            }

            MemorySegment itemRef = credPtrSegment.get(ValueLayout.ADDRESS, 0);
            if (itemRef == MemorySegment.NULL) {
                throw new KeyerException("CredRead returned NULL pointer");
            }

            try {
                MemorySegment credential = itemRef.reinterpret(CREDENTIAL_LAYOUT.byteSize());
                checkAuthenticationMethod(credential);

                int blobSize = credential.get(ValueLayout.JAVA_INT, CRED_BLOB_SIZE_OFFSET);
                if (blobSize <= 0) {
                    return new byte[0];  // Empty password
                }

                MemorySegment blobPtr = credential.get(ValueLayout.ADDRESS, CRED_BLOB_OFFSET);
                if (blobPtr == MemorySegment.NULL) {
                    throw new KeyerException("Credential blob is NULL");
                }

                MemorySegment blob = blobPtr.reinterpret(blobSize);
                return blob.toArray(ValueLayout.JAVA_BYTE);
            } finally {
                CLEAN_PASSWORD.invokeExact(itemRef);
            }
        } catch (KeyerException e) {
            throw e;
        } catch (Throwable t) {
            throw new KeyerException("Windows CredRead failed", t);
        }
    }

    /**
     * Reads the {@code AUTH_METHOD} attribute from the given credential.
     * <p>
     * If the stored authentication method is {@code PASSWORD}, the native
     * password dialog is shown and the entered password is verified.
     *
     * @param credential the native credential structure
     */
    private static void checkAuthenticationMethod(@NonNull MemorySegment credential) {
        int attrCount = credential.get(ValueLayout.JAVA_INT, ATTR_COUNT_OFFSET);
        MemorySegment attrsPtr = credential.get(ValueLayout.ADDRESS, ATTRIBUTES_OFFSET);

        if (attrCount > 0 && attrsPtr != MemorySegment.NULL) {
            MemorySegment attr = attrsPtr.reinterpret(ATTRIBUTE_LAYOUT.byteSize());
            int valSize = attr.get(ValueLayout.JAVA_INT, ATTR_VALUE_SIZE_OFFSET);
            MemorySegment valPtr = attr.get(ValueLayout.ADDRESS, ATTR_VALUE_OFFSET);

            if (valSize > 0 && valPtr != MemorySegment.NULL) {
                byte[] attrBytes = valPtr.reinterpret(valSize).toArray(ValueLayout.JAVA_BYTE);
                int authMethod = ByteBuffer.wrap(attrBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
                if (authMethod == 0) {
                    log.debug("No authentication method");
                } else if (authMethod == 1) {
                    log.debug("Auth method is password.");
                    WindowsPasswordPromptUI.showInputPasswordDialog().ifPresent(WindowsPasswordChecker::checkPassword);
                } else {
                    log.debug("Unknown authentication method: {}", authMethod);
                }
            }
        }
    }

    /**
     * Stores a password from a char array.
     * <p>
     * Converts the char array to UTF-16LE bytes and delegates
     * to {@link #setPassword(String, String, byte[], AuthorizationMethod)}.
     * The char array is zeroed after conversion.
     *
     * @param appId       the application identifier.
     * @param alias       the credential alias.
     * @param newPassword the password to store (UTF-16LE encoded).
     * @param method      the authorization method.
     * @throws KeyerException if a native call fails
     */
    public static void setPassword(String appId, String alias, char[] newPassword, AuthorizationMethod method) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        byte[] passBytes = CharactersUtils.charsUTF_16LEToBytes(newPassword);
        try {
            setPassword(appId, alias, passBytes, method);
        } finally {
            Arrays.fill(passBytes, (byte) 0);
        }
    }

    /**
     * Stores or updates a password in Windows Credential Manager.
     * <p>
     * If a credential with the same target name already exists,
     * it will be overwritten automatically by Windows.
     * <p>
     * Credentials are persisted locally for the current machine
     * ({@link #CRED_PERSIST_LOCAL_MACHINE}).
     *
     * @param appId       the application identifier.
     * @param alias       the credential alias.
     * @param newPassword the password as byte array.
     * @param method      the authorization method.
     * @throws KeyerException if the Windows API call fails.
     */
    public static synchronized void setPassword(String appId, String alias, byte[] newPassword, AuthorizationMethod method) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        try (var arena = Arena.ofConfined()) {
            MemorySegment targetSegment = allocateTargetName(arena, appId, alias);
            MemorySegment passwordSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, newPassword);
            try {
                MemorySegment credential = arena.allocate(CREDENTIAL_LAYOUT);
                credential.set(ValueLayout.JAVA_INT, TYPE_OFFSET, CRED_TYPE_GENERIC);
                credential.set(ValueLayout.ADDRESS, TARGET_NAME_OFFSET, targetSegment);
                credential.set(ValueLayout.ADDRESS, USER_NAME_OFFSET, targetSegment);
                credential.set(ValueLayout.JAVA_INT, CRED_BLOB_SIZE_OFFSET, newPassword.length);
                credential.set(ValueLayout.ADDRESS, CRED_BLOB_OFFSET, passwordSegment);
                credential.set(ValueLayout.JAVA_INT, PERSIST_OFFSET, CRED_PERSIST_LOCAL_MACHINE);

                byte[] authMethodBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(AuthorizationMethod.getMethodNumber(method)).array();
                MemorySegment attrSegment = arena.allocate(ATTRIBUTE_LAYOUT);
                MemorySegment keywordSeg = arena.allocateFrom(AUTHENTICATION_METHOD, StandardCharsets.UTF_16LE);
                MemorySegment valueSeg = arena.allocateFrom(ValueLayout.JAVA_BYTE, authMethodBytes);
                attrSegment.set(ValueLayout.ADDRESS, ATTR_KEYWORD_OFFSET, keywordSeg);
                attrSegment.set(ValueLayout.JAVA_INT, ATTR_FLAGS_OFFSET, 0);
                attrSegment.set(ValueLayout.JAVA_INT, ATTR_VALUE_SIZE_OFFSET, authMethodBytes.length);
                attrSegment.set(ValueLayout.ADDRESS, ATTR_VALUE_OFFSET, valueSeg);

                credential.set(ValueLayout.JAVA_INT, ATTR_COUNT_OFFSET, 1);
                credential.set(ValueLayout.ADDRESS, ATTRIBUTES_OFFSET, attrSegment);

                MemorySegment lastError = arena.allocate(CAPTURED_STATE_LAYOUT);
                int setStatus = (int) SET_PASSWORD.invokeExact(lastError, credential, 0);
                if (setStatus == 0) {
                    int error = getLastError(lastError);
                    throwException(error);
                }
                log.debug("Password stored in Windows Vault");
            } finally {
                passwordSegment.fill((byte) 0);
            }
        } catch (KeyerException e) {
            throw e;
        } catch (Throwable t) {
            throw new KeyerException("Cannot set password", t);
        }
    }

    /**
     * Deletes a credential from Windows Credential Manager.
     * <p>
     * If the credential does not exist, the operation
     * succeeds silently.
     *
     * @param appId the application identifier.
     * @param alias the credential alias.
     * @throws KeyerException if the Windows API call fails.
     */
    public static synchronized void deletePassword(String appId, String alias) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            MemorySegment lastError = arena.allocate(CAPTURED_STATE_LAYOUT);
            int delStatus = (int) DELETE_PASSWORD.invokeExact(lastError, allocateTargetName(arena, appId, alias), CRED_TYPE_GENERIC, 0);
            log.debug("Password deleting status: {}", delStatus);

            if (delStatus == 0) {
                int error = getLastError(lastError);
                if (error == NOT_FOUND) return;
                throwException(error);
            }
        } catch (KeyerException e) {
            throw e;
        } catch (Throwable t) {
            throw new KeyerException("Can't delete password", t);
        }
    }
}