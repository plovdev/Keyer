package org.plovdev.keyer.implementations.unix;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.Keychain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;

/**
 * Utility class for Unix native operations.
 * <p>
 * Provides libsecret library loading and schema name determination
 * based on the application's main class package.
 *
 * @author Anton
 * @version 1.6
 * @since 1.6
 */
public final class UnixNativeUtils {
    private static final Logger log = LoggerFactory.getLogger(UnixNativeUtils.class);
    private static final String BASE_SCHEME_NAME = Keychain.class.getPackageName();
    private static final String[] POSSIBLE_LIBSECRET_PATHS = {"libsecret-1.so", "libsecret-1.so.0", "libsecret-1.so.0.0.0", "/usr/lib/libsecret-1.so", "/usr/lib/x86_64-linux-gnu/libsecret-1.so", "/usr/lib/x86_64-linux-gnu/libsecret-1.so.0", "/usr/lib/x86_64-linux-gnu/libsecret-1.so.0.0.0", "/usr/lib64/libsecret-1.so", "/usr/lib/aarch64-linux-gnu/libsecret-1.so"};

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
    public static @NonNull SymbolLookup loadLibrary(Arena arena) {
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
    public static @NonNull String determineSchemeName() {
        String mainClass = System.getProperty("sun.java.command");
        if (mainClass != null && !mainClass.isEmpty()) {
            mainClass = mainClass.split(" ")[0];
            int lastDot = mainClass.lastIndexOf('.');
            String packageName = lastDot > 0 ? mainClass.substring(0, lastDot) : mainClass;
            return BASE_SCHEME_NAME + "." + packageName;
        }
        return BASE_SCHEME_NAME;
    }
}