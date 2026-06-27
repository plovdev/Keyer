package org.plovdev.keyer.implementations.win;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.keyer.exceptions.AccessDeniedException;
import org.plovdev.keyer.exceptions.KeyerException;
import org.plovdev.keyer.exceptions.KeyerStatusCode;
import org.plovdev.keyer.utils.NativeUtils;
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
 * @version 1.7
 * @since 1.0
 */
public final class WinOsKeychainNative {
    private static final Logger log = LoggerFactory.getLogger(WinOsKeychainNative.class);

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
    public char @Nullable [] getPassword(String appId, String alias) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);

        byte[] rawPassword = getRawPassword(appId, alias);
        if (rawPassword == null) {
            return null;
        }

        char[] password = NativeUtils.bytesToCharsUTF_16LE(rawPassword);
        Arrays.fill(rawPassword, (byte) 0);
        return password;
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
    public synchronized byte @Nullable [] getRawPassword(String appId, String alias) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            MemorySegment targetSegment = arena.allocateFrom(formTargetName(appId, alias), StandardCharsets.UTF_16LE);
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

            MemorySegment credential = itemRef.reinterpret(CREDENTIAL_LAYOUT.byteSize());
            int blobSize = credential.get(ValueLayout.JAVA_INT, CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("CredentialBlobSize")));
            if (blobSize <= 0) {
                CLEAN_PASSWORD.invokeExact(itemRef);
                return new byte[0];  // Empty password
            }

            MemorySegment blobPtr = credential.get(ValueLayout.ADDRESS, CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("CredentialBlob")));
            if (blobPtr == MemorySegment.NULL) {
                CLEAN_PASSWORD.invokeExact(itemRef);
                throw new KeyerException("Credential blob is NULL");
            }

            MemorySegment blob = blobPtr.reinterpret(blobSize);
            byte[] rawPassword = blob.toArray(ValueLayout.JAVA_BYTE);
            CLEAN_PASSWORD.invokeExact(itemRef);
            return rawPassword;
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
    public void setPassword(String appId, String alias, char[] newPassword) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        byte[] passBytes = NativeUtils.charsUTF_16LEToBytes(newPassword);
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
    public synchronized void setPassword(String appId, String alias, byte[] newPassword) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        try (var arena = Arena.ofConfined()) {
            String targetName = formTargetName(appId, alias);
            MemorySegment targetSegment = arena.allocateFrom(targetName, StandardCharsets.UTF_16LE);
            MemorySegment passwordSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, newPassword);

            MemorySegment credential = arena.allocate(CREDENTIAL_LAYOUT);
            credential.fill((byte) 0);

            credential.set(ValueLayout.JAVA_INT, CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Type")), CRED_TYPE_GENERIC);
            credential.set(ValueLayout.ADDRESS, CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("TargetName")), targetSegment);
            credential.set(ValueLayout.ADDRESS, CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("UserName")), targetSegment);
            credential.set(ValueLayout.JAVA_INT, CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("CredentialBlobSize")), newPassword.length);
            credential.set(ValueLayout.ADDRESS, CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("CredentialBlob")), passwordSegment);
            credential.set(ValueLayout.JAVA_INT, CREDENTIAL_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Persist")), CRED_PERSIST_LOCAL_MACHINE);

            int setStatus = (int) SET_PASSWORD.invokeExact(credential, 0);
            if (setStatus == 0) {
                int error = getLastError();
                throwException(error);
            }
            log.debug("Password stored in Windows Vault");
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
    public synchronized void deletePassword(String appId, String alias) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            String targetName = formTargetName(appId, alias);
            MemorySegment targetSegment = arena.allocateFrom(targetName, StandardCharsets.UTF_16LE);

            int delStatus = (int) DELETE_PASSWORD.invokeExact(targetSegment, CRED_TYPE_GENERIC, 0);
            log.debug("Password deleting status: {}", delStatus);

            if (delStatus == 0) {
                int error = getLastError();
                if (error == 1168) return;
                throwException(error);
            }
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
    private int getLastError() throws Throwable {
        return (int) GET_LAST_ERROR.invokeExact();
    }

    /**
     * Throws exception from status code
     *
     * @param status operation status code
     */
    private void throwException(int status) {
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

    /**
     * Formats credential target name required by Windows Credential Manager.
     *
     * @param app   application identifier
     * @param alias credential alias name
     * @return formatted string with null terminator
     */
    private static @NonNull String formTargetName(String app, String alias) {
        return app + ":" + alias + "\0";
    }
}