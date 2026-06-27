package org.plovdev.keyer.exceptions;

/**
 * Platform-independent error codes.
 *
 * @author Anton
 * @version 1.7
 * @since 1.6
 */
public enum KeyerStatusCode {
    /**
     * User denied access
     */
    ACCESS_DENIED,

    /**
     * Keychain is locked
     */
    ACCESS_LOCKED,

    /**
     * Credential doesn't exist
     */
    ITEM_NOT_FOUND,

    /**
     * Duplicate on create operation
     */
    ITEM_ALREADY_EXISTS,

    /**
     * User cancel authentication
     */
    AUTH_CANCELED,

    /**
     * Missing required entitlement
     */
    MISSING_ENTITLEMENT,

    /**
     * Stored data is corrupted
     */
    ITEM_CORRUPTED,

    /**
     * Failed to load/link native functions
     */
    INITIALIZATION_ERROR,

    /**
     * Wrong string encoding
     */
    INVALID_ENCODING,

    /**
     * Password exceeds platform limit
     */
    DATA_TOO_LARGE,

    /**
     * Platform not supported
     */
    PLATFORM_NOT_SUPPORTED,

    /**
     * Unknown
     */
    UNKNOWN_ERROR
}