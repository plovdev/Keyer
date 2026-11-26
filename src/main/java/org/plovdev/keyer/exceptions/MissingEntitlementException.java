package org.plovdev.keyer.exceptions;

/**
 * Thrown when the application lacks the required entitlements to access Keychain features.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.7
 */
public class MissingEntitlementException extends AccessDeniedException {
    /**
     * Constructs a new MissingEntitlementException with no detail message.
     */
    public MissingEntitlementException() {
    }

    /**
     * Constructs a new MissingEntitlementException with the specified detail message.
     *
     * @param message the detail message
     */
    public MissingEntitlementException(String message) {
        super(message);
    }

    /**
     * Constructs a new MissingEntitlementException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public MissingEntitlementException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new MissingEntitlementException with the specified cause.
     *
     * @param cause the underlying cause
     */
    public MissingEntitlementException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new MissingEntitlementException with the specified detail message, cause,
     * suppression control, and writable stack trace control.
     *
     * @param message            the detail message
     * @param cause              the underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public MissingEntitlementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}