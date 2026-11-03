package org.plovdev.keyer.implementations.win;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.keyer.exceptions.AccessDeniedException;
import org.plovdev.keyer.exceptions.KeyerException;
import org.plovdev.keyer.exceptions.KeyerStatusCode;
import org.plovdev.keyer.utils.CharactersUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import static org.plovdev.keyer.utils.NativeUtils.find;

/**
 * Windows Credential Manager native interface using Windows API.
 * <p>
 * Provides direct access to Windows Vault for storing, retrieving and deleting
 * generic credentials via CredReadW/CredWriteW/CredDeleteW functions.
 *
 * @author Anton
 * @version 1.8
 * @since 1.0
 */
public final class WindowsKeychainNative {
    private static final Logger log = LoggerFactory.getLogger(WindowsKeychainNative.class);

    private static final Arena SHARED = Arena.ofAuto();
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup ADVAPI32 = SymbolLookup.libraryLookup("Advapi32", SHARED);
    private static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("Kernel32", SHARED);

    private static final int CRED_TYPE_GENERIC = 1;
    private static final int CRED_PERSIST_LOCAL_MACHINE = 2;

    private static final MethodHandle GET_PASSWORD = find(ADVAPI32, LINKER, "CredReadW", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle SET_PASSWORD = find(ADVAPI32, LINKER, "CredWriteW", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle DELETE_PASSWORD = find(ADVAPI32, LINKER, "CredDeleteW", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle CLEAN_PASSWORD = find(ADVAPI32, LINKER, "CredFree", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    private static final MethodHandle GET_LAST_ERROR = find(KERNEL32, LINKER, "GetLastError", FunctionDescriptor.of(ValueLayout.JAVA_INT));

    //====ERROR CODES====\\
    private static final int NOT_FOUND = 1168;
    private static final int ACCESS_DENIED = 5;
    private static final int INVALID_PARAMETER = 87;
    private static final int ALREADY_EXISTS = 1310;
    private static final int CALL_NOT_IMPLEMENTED = 120;

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

    private static final long TYPE_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Type"));
    private static final long TARGET_NAME_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("TargetName"));
    private static final long USER_NAME_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("UserName"));
    private static final long CRED_BLOB_SIZE_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("CredentialBlobSize"));
    private static final long CRED_BLOB_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("CredentialBlob"));
    private static final long PERSIST_OFFSET = CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Persist"));

    /**
     * Retrieves a password as a char array.
     * <p>
     * Converts the raw UTF-16LE byte data from Windows Credential Manager
     * to a char array. The raw byte array is zeroed immediately after conversion.
     *
     * @param appId the application identifier (service name).
     * @param alias the credential alias (account name).
     * @return password as char array (UTF-16LE encoded), or {@code null} if not found.
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

            int status = (int) GET_PASSWORD.invokeExact(targetSegment, CRED_TYPE_GENERIC, 0, credPtrSegment);
            if (status == 0) {
                int errorCode = getLastError();
                if (errorCode == NOT_FOUND) return null;
                throwException(errorCode);
            }

            MemorySegment itemRef = credPtrSegment.get(ValueLayout.ADDRESS, 0);
            if (itemRef == MemorySegment.NULL) {
                throw new KeyerException("CredRead returned NULL pointer");
            }

            try {
                MemorySegment credential = itemRef.reinterpret(CREDENTIAL_LAYOUT.byteSize());
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
     * Stores a password from a char array.
     * <p>
     * Converts the char array to UTF-16LE bytes and delegates
     * to {@link #setPassword(String, String, byte[])}.
     * The char array is zeroed after conversion.
     *
     * @param appId       the application identifier.
     * @param alias       the credential alias.
     * @param newPassword the password to store (UTF-16LE encoded).
     */
    public static void setPassword(String appId, String alias, char[] newPassword) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        byte[] passBytes = CharactersUtils.charsUTF_16LEToBytes(newPassword);
        try {
            setPassword(appId, alias, passBytes);
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
     * @throws KeyerException if the Windows API call fails.
     */
    public static synchronized void setPassword(String appId, String alias, byte[] newPassword) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        try (var arena = Arena.ofConfined()) {
            MemorySegment targetSegment = allocateTargetName(arena, appId, alias);
            MemorySegment passwordSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, newPassword);

            MemorySegment credential = arena.allocate(CREDENTIAL_LAYOUT);
            credential.fill((byte) 0);

            credential.set(ValueLayout.JAVA_INT, TYPE_OFFSET, CRED_TYPE_GENERIC);
            credential.set(ValueLayout.ADDRESS, TARGET_NAME_OFFSET, targetSegment);
            credential.set(ValueLayout.ADDRESS, USER_NAME_OFFSET, targetSegment);
            credential.set(ValueLayout.JAVA_INT, CRED_BLOB_SIZE_OFFSET, newPassword.length);
            credential.set(ValueLayout.ADDRESS, CRED_BLOB_OFFSET, passwordSegment);
            credential.set(ValueLayout.JAVA_INT, PERSIST_OFFSET, CRED_PERSIST_LOCAL_MACHINE);

            int setStatus = (int) SET_PASSWORD.invokeExact(credential, 0);
            if (setStatus == 0) {
                int error = getLastError();
                throwException(error);
            }
            log.debug("Password stored in Windows Vault");
        } catch (KeyerException e) {
            throw e;
        } catch (Throwable t) {
            throw new KeyerException("Cannot set password", t);
        }
    }

    /**
     * Deletes a credential from Windows Credential Manager.
     * <p>
     * If the credential does not exist ({@link #NOT_FOUND}), the operation
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
            int delStatus = (int) DELETE_PASSWORD.invokeExact(allocateTargetName(arena, appId, alias), CRED_TYPE_GENERIC, 0);
            log.debug("Password deleting status: {}", delStatus);

            if (delStatus == 0) {
                int error = getLastError();
                if (error == NOT_FOUND) return;
                throwException(error);
            }
        } catch (KeyerException e) {
            throw e;
        } catch (Throwable t) {
            throw new KeyerException("Can't delete password", t);
        }
    }

    /**
     * Retrieves last error code from Windows API.
     *
     * @return last error code
     * @throws Throwable if native call fails
     */
    private static int getLastError() throws Throwable {
        return (int) GET_LAST_ERROR.invokeExact();
    }

    /**
     * Throws exception from status code
     *
     * @param status operation status code
     */
    private static void throwException(int status) {
        switch (status) {
            // not thorw, skip
            case 0, NOT_FOUND, ALREADY_EXISTS:
                break;
            case ACCESS_DENIED:
                throw new AccessDeniedException(KeyerStatusCode.ACCESS_DENIED);
            case INVALID_PARAMETER:
                throw new IllegalArgumentException();
            case CALL_NOT_IMPLEMENTED:
                throw new UnsupportedOperationException("Call not implemented");
            default:
                throw new KeyerException(String.format("Failed to get password (Code: %d)", status));
        }
    }

    private static MemorySegment allocateTargetName(@NonNull Arena arena, String appId, String alias) {
        return arena.allocateFrom(formTargetName(appId, alias), StandardCharsets.UTF_16LE);
    }

    /**
     * Formats credential target name required by Windows Credential Manager.
     *
     * @param appId   application identifier
     * @param alias credential alias name
     * @return formatted string
     */
    private static @NonNull String formTargetName(String appId, String alias) {
        return appId + ":" + alias;
    }
}