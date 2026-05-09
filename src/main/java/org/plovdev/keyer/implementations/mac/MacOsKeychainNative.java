package org.plovdev.keyer.implementations.mac;

import org.jspecify.annotations.Nullable;
import org.plovdev.keyer.exceptions.AccessDeniedException;
import org.plovdev.keyer.exceptions.KeyerException;
import org.plovdev.keyer.exceptions.KeyerStatusCode;
import org.plovdev.keyer.utils.NativeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Objects;

import static org.plovdev.keyer.utils.NativeUtils.find;

/**
 * Low-level native bridge for macOS Keychain access.
 * <p>
 * This class interacts directly with {@code Security.framework} to perform
 * CRUD operations on Generic Password items.
 *
 * @author Anton
 * @version 1.6
 * @since 1.7
 */
public final class MacOsKeychainNative {
    private static final Logger log = LoggerFactory.getLogger(MacOsKeychainNative.class);

    private static final String ADD_PASSWORD_METHOD_NAME = "SecKeychainAddGenericPassword";
    private static final String GET_PASSWORD_METHOD_NAME = "SecKeychainFindGenericPassword";
    private static final String DELETE_PASSWORD_METHOD_NAME = "SecKeychainItemDelete";
    private static final String CLEAN_PASSWORD_METHOD_NAME = "SecKeychainItemFreeContent";

    private static final Arena SHARED = Arena.ofAuto();
    private static final Linker LINKER = Linker.nativeLinker();

    /**
     * Path to the binary inside the Security framework.
     */
    private static final SymbolLookup SECURITY = SymbolLookup.libraryLookup("/System/Library/Frameworks/Security.framework/Versions/A/Security", SHARED);

    private static final MethodHandle ADD_PASSWORD = find(SECURITY, LINKER, ADD_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle UPDATE_PASSWORD = find(SECURITY, LINKER, "SecKeychainItemModifyAttributesAndData", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle GET_PASSWORD = find(SECURITY, LINKER, GET_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle DELETE_PASSWORD = find(SECURITY, LINKER, DELETE_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle CLEAN_PASSWORD = find(SECURITY, LINKER, CLEAN_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle CF_RELEASE = find(SECURITY, LINKER, "CFRelease", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    //====ERROR CODES====\\
    private static final int SUCCESS = 0;
    private static final int NOT_FOUND = -25300;
    private static final int ACCESS_DENIED = -25293;
    private static final int ACCESS_LOCKED = -25308;


    /**
     * Fetches a password from the Keychain.
     *
     * @param app   the service name (appId)
     * @param alias the account name
     * @return password as char array, or null if not found
     * @throws RuntimeException if a native call fails unexpectedly
     */
    public char @Nullable [] getPassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            MemorySegment appSegment = arena.allocateFrom(app);
            MemorySegment aliasSegment = arena.allocateFrom(alias);
            MemorySegment lenPtr = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment dataPtr = arena.allocate(ValueLayout.ADDRESS);

            int status = (int) GET_PASSWORD.invokeExact(MemorySegment.NULL, (int) appSegment.byteSize() - 1, appSegment, (int) aliasSegment.byteSize() - 1, aliasSegment, lenPtr, dataPtr, MemorySegment.NULL);
            log.debug("Password getting status: {}", status);

            // error handling
            if (status == NOT_FOUND) return null;
            throwException(status);

            MemorySegment passwordData = dataPtr.get(ValueLayout.ADDRESS, 0).reinterpret(lenPtr.get(ValueLayout.JAVA_INT, 0));
            byte[] bytes = passwordData.toArray(ValueLayout.JAVA_BYTE);
            char[] password = NativeUtils.bytesToCharsUTF_8(bytes);

            int cleanStatus = (int) CLEAN_PASSWORD.invokeExact(MemorySegment.NULL, passwordData);
            if (cleanStatus != SUCCESS) {
                log.warn("CredFree returned non-zero status: {}", cleanStatus);
            }

            Arrays.fill(bytes, (byte) 0);
            return password;
        } catch (Throwable t) {
            throw new KeyerException("Error to get password", t);
        }
    }

    /**
     * Saves a password. Attempts to delete any existing entry first to prevent duplicates.
     *
     * @param app         the service name
     * @param alias       the account name
     * @param newPassword password to save
     * @throws RuntimeException if the save operation fails
     */
    public synchronized void setPassword(String app, String alias, char[] newPassword) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        byte[] passBytes = NativeUtils.charsUTF_8ToBytes(newPassword);
        try (var arena = Arena.ofConfined()) {
            var appSegment = arena.allocateFrom(app);
            var aliasSegment = arena.allocateFrom(alias);
            var passwordSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, passBytes);
            var itemRefPtr = arena.allocate(ValueLayout.ADDRESS);

            int findStatus = (int) GET_PASSWORD.invokeExact(MemorySegment.NULL, (int) appSegment.byteSize() - 1, appSegment, (int) aliasSegment.byteSize() - 1, aliasSegment, MemorySegment.NULL, MemorySegment.NULL, itemRefPtr);
            MemorySegment itemRef = null;
            int status;

            try {
                if (findStatus == SUCCESS) {
                    itemRef = itemRefPtr.get(ValueLayout.ADDRESS, 0);
                    status = (int) UPDATE_PASSWORD.invokeExact(itemRef, MemorySegment.NULL, passBytes.length, passwordSegment);
                } else if (findStatus == NOT_FOUND) {
                    status = (int) ADD_PASSWORD.invokeExact(MemorySegment.NULL, (int) appSegment.byteSize() - 1, appSegment, (int) aliasSegment.byteSize() - 1, aliasSegment, passBytes.length, passwordSegment, MemorySegment.NULL);
                } else {
                    throw new KeyerException("Find failed: " + findStatus);
                }
                throwException(status);
            } finally {
                if (itemRef != null && itemRef.address() != 0) {
                    CF_RELEASE.invokeExact(itemRef);
                }
            }
        } catch (Throwable t) {
            throw new KeyerException("Cann't set password", t);
        } finally {
            Arrays.fill(passBytes, (byte) 0);
        }
    }

    /**
     * Deletes a specific keychain item.
     *
     * @param app   the service name
     * @param alias the account name
     * @throws RuntimeException if the item exists but cannot be deleted
     */
    public void deletePassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            var appSegment = arena.allocateFrom(app);
            var aliasSegment = arena.allocateFrom(alias);
            var itemRefPtr = arena.allocate(ValueLayout.ADDRESS);

            int status = (int) GET_PASSWORD.invokeExact(MemorySegment.NULL, (int) appSegment.byteSize() - 1, appSegment, (int) aliasSegment.byteSize() - 1, aliasSegment, MemorySegment.NULL, MemorySegment.NULL, itemRefPtr);
            if (status == NOT_FOUND) return;
            if (status != SUCCESS) throwException(status);

            MemorySegment itemRef = itemRefPtr.get(ValueLayout.ADDRESS, 0);
            try {
                int delStatus = (int) DELETE_PASSWORD.invokeExact(itemRef);
                throwException(delStatus);
            } finally {
                CF_RELEASE.invokeExact(itemRef);
            }
        } catch (Throwable t) {
            throw new KeyerException("Error to delete password", t);
        }
    }

    /**
     * Throws exception from status code
     *
     * @param status operation status code
     */
    private void throwException(int status) {
        switch (status) {
            case SUCCESS, NOT_FOUND:
                break;
            case ACCESS_DENIED:
                throw new AccessDeniedException(KeyerStatusCode.ACCESS_DENIED);
            case ACCESS_LOCKED:
                throw new AccessDeniedException(KeyerStatusCode.ACCESS_LOCKED);
            default:
                throw new KeyerException(String.format("Failed to get password (Code: %d)", status));
        }
    }
}