package org.plovdev.keyer.implementations.unix;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.Keychain;
import org.plovdev.keyer.exceptions.AccessDeniedException;
import org.plovdev.keyer.exceptions.KeyerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.util.NoSuchElementException;

/**
 * Utility class for Unix native operations.
 * <p>
 * Provides libsecret library loading and schema name determination
 * based on the application's main class package.
 * <p>
 * This class cannot be instantiated.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.6
 */
public final class UnixNativeUtils {
    private static final Logger log = LoggerFactory.getLogger(UnixNativeUtils.class);
    private static final String BASE_SCHEMA_NAME = Keychain.class.getPackageName();
    private static final String[] POSSIBLE_LIBSECRET_PATHS = {"libsecret-1.so", "libsecret-1.so.0", "libsecret-1.so.0.0.0", "/usr/lib/libsecret-1.so", "/usr/lib/x86_64-linux-gnu/libsecret-1.so", "/usr/lib/x86_64-linux-gnu/libsecret-1.so.0", "/usr/lib/x86_64-linux-gnu/libsecret-1.so.0.0.0", "/usr/lib64/libsecret-1.so", "/usr/lib/aarch64-linux-gnu/libsecret-1.so"};

    //====ERROR CODES====\\
    // private static final int SECRET_ERROR_PROTOCOL = 1;
    static final int SECRET_IS_LOCKED = 2;
    static final int NO_SUCH_OBJECT = 3;

    /**
     * Private constructor to prevent instantiation.
     */
    private UnixNativeUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Loads libsecret shared library from system paths.
     * <p>
     * Searches through common library paths including:
     * <ul>
     *     <li>Standard library names (libsecret-1.so, libsecret-1.so.0, etc.)</li>
     *     <li>Distribution-specific paths (/usr/lib/, /usr/lib64/, /usr/lib/x86_64-linux-gnu/, etc.)</li>
     * </ul>
     *
     * @param arena memory arena for library lifetime management
     * @return SymbolLookup for libsecret
     * @throws UnsatisfiedLinkError if libsecret cannot be loaded from any known path
     */
    static @NonNull SymbolLookup loadLibrary(Arena arena) {
        SymbolLookup lookup = null;
        for (String path : POSSIBLE_LIBSECRET_PATHS) {
            try {
                lookup = SymbolLookup.libraryLookup(path, arena);
                break;
            } catch (Exception e) {
                log.error("Failed to load libsecret from {}:", path, e);
            }
        }
        if (lookup == null) {
            throw new UnsatisfiedLinkError("Unable to load libsecret");
        }

        return lookup;
    }

    /**
     * Determines unique schema name for the secret service.
     * <p>
     * Uses {@code sun.java.command} system property to extract the main class package.
     * Format: {@code org.plovdev.keyer.{mainClassPackage}}
     * <p>
     * Example: main class {@code com.myapp.Main} → {@code org.plovdev.keyer.com.myapp}
     *
     * @return schema name for libsecret
     */
    static @NonNull String determineSchemaName() {
        String mainClass = System.getProperty("sun.java.command");
        if (mainClass != null && !mainClass.isEmpty()) {
            mainClass = mainClass.split(" ")[0];
            int lastDot = mainClass.lastIndexOf('.');
            String packageName = lastDot > 0 ? mainClass.substring(0, lastDot) : mainClass;
            return BASE_SCHEMA_NAME + "." + packageName;
        }
        return BASE_SCHEMA_NAME;
    }

    /**
     * Reads GLib error message from GError structure.
     *
     * @param errorPtr pointer to GError
     * @return error message string
     */
    static String readGError(@NonNull MemorySegment errorPtr) {
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
    static int getGErrorCode(MemorySegment errorPtr) {
        try {
            return errorPtr.get(ValueLayout.JAVA_INT, 8);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Formats label for secret item.
     *
     * @param arena the arena used to allocate the label
     * @param app   service name
     * @param alias account name
     * @return formatted label
     */
    static @NonNull MemorySegment formLabel(@NonNull Arena arena, String app, String alias) {
        return arena.allocateFrom(String.format("%s - %s", app, alias));
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
    static void throwException(String str, int status) {
        switch (status) {
            case NO_SUCH_OBJECT:
                throw new NoSuchElementException(str);
            case SECRET_IS_LOCKED:
                throw new AccessDeniedException(str);
            default:
                throw new KeyerException(String.format("Failed to get password (Message: %s, Code: %d)", str, status));
        }
    }
}