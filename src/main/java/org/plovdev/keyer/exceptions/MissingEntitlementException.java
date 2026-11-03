package org.plovdev.keyer.exceptions;

/**
 * Thrown when the application lacks the required entitlements to access Keychain features.
 *
 * @author Anton
 * @version 1.7
 * @since 1.7
 */
public class MissingEntitlementException extends AccessDeniedException {
    public MissingEntitlementException() {
    }

    /**
     * @param statusCode the specific keyer status code
     */
    public MissingEntitlementException(KeyerStatusCode statusCode) {
        super(statusCode);
    }

    /**
     * @param message the detail message
     */
    public MissingEntitlementException(String message) {
        super(message);
    }

    /**
     * @param message    the detail message
     * @param statusCode the specific keyer status code
     */
    public MissingEntitlementException(String message, KeyerStatusCode statusCode) {
        super(message, statusCode);
    }

    /**
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public MissingEntitlementException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public MissingEntitlementException(String message, KeyerStatusCode statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    /**
     * {@inheritDoc}
     */
    public MissingEntitlementException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public MissingEntitlementException(KeyerStatusCode statusCode, Throwable cause) {
        super(statusCode, cause);
    }

    /**
     * {@inheritDoc}
     */
    public MissingEntitlementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * {@inheritDoc}
     */
    public MissingEntitlementException(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, statusCode, cause, enableSuppression, writableStackTrace);
    }
}