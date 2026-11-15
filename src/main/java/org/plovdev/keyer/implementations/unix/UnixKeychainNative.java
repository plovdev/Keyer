package org.plovdev.keyer.implementations.unix;

import org.jspecify.annotations.Nullable;
import org.plovdev.keyer.exceptions.AccessDeniedException;
import org.plovdev.keyer.exceptions.KeyerException;
import org.plovdev.keyer.utils.CharactersUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Objects;

import static org.plovdev.keyer.implementations.unix.UnixNativeUtils.*;
import static org.plovdev.keyer.utils.NativeUtils.findFunction;

/**
 * Unix/Linux native implementation using libsecret.
 * <p>
 * Provides low-level access to Libsecret.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.5
 */
public final class UnixKeychainNative {
    private static final Logger log = LoggerFactory.getLogger(UnixKeychainNative.class);
    private static final String SCHEMA_NAME = UnixNativeUtils.determineSchemaName();

    private static final Arena SHARED = Arena.ofAuto();
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup SECRET = UnixNativeUtils.loadLibrary(SHARED);

    // Function descriptors for libsecret API
    private static final MethodHandle SECRET_PASSWORD_LOOKUP_SYNC = findFunction(SECRET, LINKER, "secret_password_lookup_sync", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle SECRET_PASSWORD_STORE_SYNC = findFunction(SECRET, LINKER, "secret_password_store_sync", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle SECRET_PASSWORD_CLEAR_SYNC = findFunction(SECRET, LINKER, "secret_password_clear_sync", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle SECRET_SCHEMA_NEW = findFunction(SECRET, LINKER, "secret_schema_new", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle G_FREE = findFunction(SymbolLookup.libraryLookup("libglib-2.0.so", SHARED), LINKER, "g_free", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    private static final MemorySegment schemaRef;

    /*
    Initializes the libsecret schema reference used by all operations.
    The schema is created once for the current application.
    */
    static {
        try {
            schemaRef = (MemorySegment) SECRET_SCHEMA_NEW.invokeExact(SHARED.allocateFrom(SCHEMA_NAME), 0, MemorySegment.NULL);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a password as a char array.
     * <p>
     * Converts the raw byte data from the Keychain to a char array using UTF-16LE
     * decoding. The raw byte array is zeroed immediately after conversion.
     *
     * @param app   the service name (application identifier)
     * @param alias the account name
     * @return password as char array, or {@code null} if not found
     * @throws KeyerException if a native call fails
     */
    public static char @Nullable [] getPassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        byte[] rawBytes = getRawPassword(app, alias);
        if (rawBytes == null) {
            return null;
        }
        try {
            return CharactersUtils.bytesToCharsUTF_16LE(rawBytes);
        } finally {
            Arrays.fill(rawBytes, (byte) 0);
        }
    }

    /**
     * Retrieves a password as a byte array directly from the Keychain.
     *
     * @param app   the service name
     * @param alias the account name
     * @return password as byte array, or {@code null} if not found
     * @throws KeyerException if a native call fails unexpectedly
     */
    public static synchronized byte @Nullable [] getRawPassword(String app, String alias) {
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
            try {
                byte[] rawBytes = new byte[(int) size];
                MemorySegment.copy(readable, ValueLayout.JAVA_BYTE, 0, rawBytes, 0, (int) size);
                return rawBytes;
            } finally {
                readable.fill((byte) 0);
                G_FREE.invokeExact(passwordSegment);
            }
        } catch (Throwable t) {
            throw new KeyerException("Error getting password", t);
        }
    }

    /**
     * Stores a password from a char array.
     * <p>
     * Converts the char array to bytes using UTF-16LE encoding and delegates
     * to {@link #setPassword(String, String, byte[])}.
     * The char array is zeroed after conversion.
     *
     * @param app         the service name.
     * @param alias       the account name.
     * @param newPassword the password to store.
     * @throws KeyerException if the operation fails
     */
    public static void setPassword(String app, String alias, char[] newPassword) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        byte[] passBytes = CharactersUtils.charsUTF_16LEToBytes(newPassword);
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
    public static synchronized void setPassword(String app, String alias, byte[] newPassword) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(newPassword);

        try (var arena = Arena.ofConfined()) {
            MemorySegment errorPtr = arena.allocate(ValueLayout.ADDRESS);
            MemorySegment passwordSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, newPassword);

            boolean success = (boolean) SECRET_PASSWORD_STORE_SYNC.invokeExact(
                    schemaRef,
                    MemorySegment.NULL,
                    formLabel(arena, app, alias),
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
    public static synchronized void deletePassword(String app, String alias) {
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
}