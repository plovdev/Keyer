package org.plovdev.keyer.exceptions;

/**
 * Thrown when an authentication process is explicitly canceled by the user.
 * <p>
 * This typically occurs when a user clicks the "Cancel" button in a
 * system dialog.
 *
 * @author Anton
 * @version 1.7
 * @since 1.7
 */
public class AuthenticationCanceledException extends AccessDeniedException {
    public AuthenticationCanceledException() {
    }

    /**
     * @param statusCode specific status code related to the cancellation
     */
    public AuthenticationCanceledException(KeyerStatusCode statusCode) {
        super(statusCode);
    }

    /**
     * @param message detailed error message
     */
    public AuthenticationCanceledException(String message) {
        super(message);
    }

    /**
     * @param message detailed error message and specific status code related to the cancellation
     */
    public AuthenticationCanceledException(String message, KeyerStatusCode statusCode) {
        super(message, statusCode);
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationCanceledException(String message, KeyerStatusCode statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationCanceledException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationCanceledException(KeyerStatusCode statusCode, Throwable cause) {
        super(statusCode, cause);
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationCanceledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationCanceledException(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, statusCode, cause, enableSuppression, writableStackTrace);
    }
}