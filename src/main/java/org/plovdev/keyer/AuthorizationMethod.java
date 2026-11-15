package org.plovdev.keyer;

import org.jspecify.annotations.NonNull;

/**
 * Defines the security level and user interaction required to access a keychain entry.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.7
 */
public enum AuthorizationMethod {
    /**
     * No additional authorization required.
     * The item is accessible as long as the system keychain is unlocked.
     */
    NONE,

    /**
     * Requires the user to provide their system or account password.
     */
    PASSWORD,

    /**
     * Requires biometric authentication (e.g., Touch ID, Face ID, or Windows Hello).
     */
    BIOMETRY;

    /**
     * Returns the numeric value associated with the given authorization method.
     * Values are used for mapping to platform-specific authorization flags.
     *
     * @param method the authorization method to map to a numeric value
     * @return the numeric value of the given method (0 for NONE, 1 for PASSWORD, 2 for BIOMETRY)
     */
    public static int getMethodNumber(@NonNull AuthorizationMethod method) {
        return switch (method) {
            case NONE -> 0;
            case PASSWORD -> 1;
            case BIOMETRY -> 2;
        };
    }
}