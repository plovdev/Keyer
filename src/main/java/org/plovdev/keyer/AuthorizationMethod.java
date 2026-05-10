package org.plovdev.keyer;

/**
 * Defines the security level and user interaction required to access a keychain entry.
 *
 * @author Anton
 * @version 1.7
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
     * Falls back to the system password if biometrics are unavailable or fail.
     */
    BIOMETRY
}