package org.plovdev.keyer;

/**
 * Supported operating system platforms for native keychain access.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.0
 */
public enum Platform {
    /**
     * Microsoft Windows operating systems.
     * Uses Windows Credential Manager as the backend.
     */
    WINDOWS,

    /**
     * Apple macOS operating systems.
     * Uses macOS Keychain as the backend.
     */
    MAC,

    /**
     * Linux and Unix-systems.
     * Typically, uses Libsecret as the backend.
     */
    UNIX,

    /**
     * Any other operating system that is not officially supported by Keyer.
     */
    OTHER;

    /**
     * Detects the current operating system based on the {@code "os.name"} system property.
     * <p>
     * Supported platforms include:
     * <ul>
     *     <li>{@link Platform#MAC} - for macOS systems</li>
     *     <li>{@link Platform#WINDOWS} - for Windows systems</li>
     *     <li>{@link Platform#UNIX} - for Linux and other Unix-systems</li>
     * </ul>
     *
     * @return the detected {@link Platform} enum value, or {@link Platform#OTHER}
     * if the current operating system is not recognized
     */
    public static Platform guessPlatform() {
        String osName = System.getProperty("os.name").strip().toLowerCase();
        if (osName.contains("mac") || osName.equals("darwin")) {
            return Platform.MAC;
        } else if (osName.contains("win")) {
            return Platform.WINDOWS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix") || osName.contains("bsd")) {
            return Platform.UNIX;
        }
        return Platform.OTHER;
    }
}