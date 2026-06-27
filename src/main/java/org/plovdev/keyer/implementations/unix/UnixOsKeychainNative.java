package org.plovdev.keyer.implementations.unix;

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
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.plovdev.keyer.utils.NativeUtils.find;

/**
 * Unix/Linux native implementation using libsecret (Secret Service API).
 * <p>
 * Provides low-level access to GNOME Keyring, KDE Wallet, and other
 * secret service providers through the libsecret library.
 *
 * @author Anton
 * @version 1.7
 * @since 1.5
 */
public final class UnixOsKeychainNative {
    private static final Logger log = LoggerFactory.getLogger(UnixOsKeychainNative.class);
    private static final String SCHEMA_NAME = UnixNativeUtils.determineSchemeName();

    private static final Arena SHARED = Arena.ofAuto();
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup SECRET = UnixNativeUtils.loadLibrary(SHARED);

    // Function descriptors for libsecret API
    private static final MethodHandle SECRET_PASSWORD_LOOKUP_SYNC = find(SECRET, LINKER, "secret_password_lookup_sync", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle SECRET_PASSWORD_STORE_SYNC = find(SECRET, LINKER, "secret_password_store_sync", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle SECRET_PASSWORD_CLEAR_SYNC = find(SECRET, LINKER, "secret_password_clear_sync", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle SECRET_SCHEMA_NEW = find(SECRET, LINKER, "secret_schema_new", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle SECRET_SCHEMA_UNREF = find(SECRET, LINKER, "secret_schema_unref", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    private static final MethodHandle G_FREE = find(SymbolLookup.libraryLookup("libglib-2.0.so", SHARED), LINKER, "g_free", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    private static final MemorySegment schemaRef;

    static {
        try {
            schemaRef = (MemorySegment) SECRET_SCHEMA_NEW.invokeExact(SHARED.allocateFrom(SCHEMA_NAME), 0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    //====ERROR CODES====\\
    private static final int SECRET_ERROR_PROTOCOL = 1;
    private static final int SECRET_IS_LOCKED = 2;
    private static final int NO_SUCH_OBJECT = 3;
    private static final int SECRET_ALREADY_EXISTS = 4;

    /**
     * Retrieves a password as a char array.
     * <p>
     * Converts the raw byte data from the Keychain to a char array using UTF-8
     * decoding. The raw byte array is zeroed immediately after conversion.
     *
     * @param app   the service name (application identifier)
     * @param alias the account name
     * @return password as char array, or {@code null} if not found
     */
    public char @Nullable [] getPassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        byte[] rawBytes = getRawPassword(app, alias);
        if (rawBytes == null) {
            return null;
        }

        char[] password = NativeUtils.bytesToCharsUTF_8(rawBytes);
        Arrays.fill(rawBytes, (byte) 0);
        return password;
    }

    /**
     * Retrieves a password as a byte array directly from the Keychain.
     *
     * @param app   the service name
     * @param alias the account name
     * @return password as byte array, or {@code null} if not found
     * @throws KeyerException if a native call fails unexpectedly
     */
    public synchronized byte @Nullable [] getRawPassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            MemorySegment errorPtr = arena.allocate(ValueLayout.ADDRESS);
            MemorySegment passwordSegment = (MemorySegment) SECRET_PASSWORD_LOOKUP_SYNC.invokeExact(
                    schemaRef,
                    MemorySegment.NULL,
                    errorPtr,
                    arena.allocateFrom("service"),
                    arena.allocateFrom(app),
                    arena.allocateFrom("account"),
                    arena.allocateFrom(alias),
                    MemorySegment.NULL
            );

            MemorySegment errorSegment = errorPtr.get(ValueLayout.ADDRESS, 0);
            if (errorSegment.address() != 0) {
                String errorMessage = readGError(errorSegment);
                int errorCode = getGErrorCode(errorSegment);
                if (errorCode == NO_SUCH_OBJECT) {
                    return null;
                }
                throwException(errorMessage, errorCode);
            }

            if (passwordSegment.address() == 0) {
                return null;
            }

            long size = 0;
            while (passwordSegment.get(ValueLayout.JAVA_BYTE, size) != 0) {
                size++;
            }
            MemorySegment readable = passwordSegment.reinterpret(size);
            byte[] rawBytes = new byte[(int) size];
            MemorySegment.copy(readable, ValueLayout.JAVA_BYTE, 0, rawBytes, 0, (int) size);

            G_FREE.invokeExact(passwordSegment);
            return rawBytes;
        } catch (Throwable t) {
            throw new KeyerException("Error getting password", t);
        }
    }

    /**
     * Stores a password from a char array.
     * <p>
     * Converts the char array to bytes using UTF-8 encoding and delegates
     * to {@link #setPassword(String, String, byte[])}.
     * The char array is zeroed after conversion.
     *
     * @param app         the service name.
     * @param alias       the account name.
     * @param newPassword the password to store.
     */
    public void setPassword(String app, String alias, char[] newPassword) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        byte[] passBytes = NativeUtils.charsUTF_8ToBytes(newPassword);
        try {
            setPassword(app, alias, passBytes);
        } finally {
            Arrays.fill(passBytes, (byte) 0);
        }
    }

    /**
     * Stores or updates a password in the secret service.
     * <p>
     * If a password with the same service/account already exists,
     * it will be overwritten. The password is stored with a descriptive
     * label in the format {@code "service - account"}.
     *
     * @param app         the service name.
     * @param alias       the account name.
     * @param newPassword the password as byte array.
     * @throws KeyerException        if the operation fails.
     * @throws AccessDeniedException if the secret service is locked.
     */
    public synchronized void setPassword(String app, String alias, byte[] newPassword) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        try (var arena = Arena.ofConfined()) {
            MemorySegment errorPtr = arena.allocate(ValueLayout.ADDRESS);
            MemorySegment passwordSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, newPassword);

            String label = formLabel(app, alias);
            boolean success = (boolean) SECRET_PASSWORD_STORE_SYNC.invokeExact(
                    schemaRef,
                    MemorySegment.NULL,
                    arena.allocateFrom(label),
                    passwordSegment,
                    MemorySegment.NULL,
                    errorPtr,
                    arena.allocateFrom("service"),
                    arena.allocateFrom(app),
                    arena.allocateFrom("account"),
                    arena.allocateFrom(alias),
                    MemorySegment.NULL
            );

            MemorySegment errorSegment = errorPtr.get(ValueLayout.ADDRESS, 0);
            if (errorSegment.address() != 0 || !success) {
                String errorMsg = readGError(errorSegment);
                int errorCode = getGErrorCode(errorSegment);
                throwException(errorMsg, errorCode);
            }
            log.debug("Password stored successfully");
        } catch (Throwable t) {
            throw new KeyerException("Cannot set password", t);
        }
    }

    /**
     * Deletes a password from the secret service.
     * <p>
     * If the password does not exist, the operation succeeds silently.
     *
     * @param app   the service name
     * @param alias the account name
     * @throws KeyerException        if the operation fails.
     * @throws AccessDeniedException if the secret service is locked.
     */
    public synchronized void deletePassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            MemorySegment errorPtr = arena.allocate(ValueLayout.ADDRESS);
            boolean success = (boolean) SECRET_PASSWORD_CLEAR_SYNC.invokeExact(
                    schemaRef,
                    MemorySegment.NULL,
                    errorPtr,
                    arena.allocateFrom("service"),
                    arena.allocateFrom(app),
                    arena.allocateFrom("account"),
                    arena.allocateFrom(alias),
                    MemorySegment.NULL
            );

            log.debug("Deleting success: {}", success);

            if (!success) {
                MemorySegment errorSegment = errorPtr.get(ValueLayout.ADDRESS, 0);
                if (errorSegment.address() != 0) {
                    String errorMsg = readGError(errorSegment);
                    int errorCode = getGErrorCode(errorSegment);
                    if (errorCode == NO_SUCH_OBJECT) return;
                    throwException(errorMsg, errorCode);
                }
            }
        } catch (Throwable t) {
            throw new KeyerException("Error deleting password", t);
        }
    }

    /**
     * Reads GLib error message from GError structure.
     *
     * @param errorPtr pointer to GError
     * @return error message string
     */
    private static String readGError(@NonNull MemorySegment errorPtr) {
        MemorySegment messagePtr = errorPtr.get(ValueLayout.ADDRESS, 0);
        if (messagePtr.address() != 0) {
            return messagePtr.getString(0);
        }
        return "Unknown GLib error";
    }

    /**
     * Extracts error code from GError structure.
     *
     * @param errorPtr pointer to GError
     * @return error code, or -1 if extraction fails
     */
    private static int getGErrorCode(MemorySegment errorPtr) {
        try {
            return errorPtr.get(ValueLayout.JAVA_INT, 8);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Formats label for secret item.
     *
     * @param app   service name
     * @param alias account name
     * @return formatted label
     */
    private @NonNull String formLabel(String app, String alias) {
        return String.format("%s - %s", app, alias);
    }

    /**
     * Throws appropriate exception based on GLib error code.
     *
     * @param str    error message
     * @param status GLib error code
     * @throws NoSuchElementException if status is NO_SUCH_OBJECT
     * @throws AccessDeniedException  if status is SECRET_IS_LOCKED
     * @throws KeyerException         for all other errors
     */
    private void throwException(String str, int status) {
        switch (status) {
            case NO_SUCH_OBJECT:
                throw new NoSuchElementException(str);
            case SECRET_IS_LOCKED:
                throw new AccessDeniedException(str, KeyerStatusCode.ACCESS_LOCKED);
            case SECRET_ALREADY_EXISTS:
                throw new KeyerException("Secret already exists", KeyerStatusCode.ITEM_ALREADY_EXISTS);
            default:
                throw new KeyerException(String.format("Failed to get password (Message: %s, Code: %d)", str, status));
        }
    }
}