package org.plovdev.keyer.exceptions;

/**
 * Thrown when the application lacks the required entitlements to access Keychain features.
 *
 * @author Anton
 * @version 1.7
 * @since 1.7
 */
public class MissingEntitlement extends AccessDeniedException {
    public MissingEntitlement() {
    }

    /**
     * @param statusCode the specific keyer status code
     */
    public MissingEntitlement(KeyerStatusCode statusCode) {
        super(statusCode);
    }

    /**
     * @param message the detail message
     */
    public MissingEntitlement(String message) {
        super(message);
    }

    /**
     * @param message    the detail message
     * @param statusCode the specific keyer status code
     */
    public MissingEntitlement(String message, KeyerStatusCode statusCode) {
        super(message, statusCode);
    }

    /**
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public MissingEntitlement(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public MissingEntitlement(String message, KeyerStatusCode statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    /**
     * {@inheritDoc}
     */
    public MissingEntitlement(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public MissingEntitlement(KeyerStatusCode statusCode, Throwable cause) {
        super(statusCode, cause);
    }

    /**
     * {@inheritDoc}
     */
    public MissingEntitlement(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * {@inheritDoc}
     */
    public MissingEntitlement(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, statusCode, cause, enableSuppression, writableStackTrace);
    }
}