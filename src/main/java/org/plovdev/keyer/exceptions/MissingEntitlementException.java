package org.plovdev.keyer.exceptions;

/**
 * Thrown when the application lacks the required entitlements to access Keychain features.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.7
 */
public class MissingEntitlementException extends AccessDeniedException {
    public MissingEntitlementException() {
    }

    public MissingEntitlementException(String message) {
        super(message);
    }

    public MissingEntitlementException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingEntitlementException(Throwable cause) {
        super(cause);
    }

    public MissingEntitlementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}