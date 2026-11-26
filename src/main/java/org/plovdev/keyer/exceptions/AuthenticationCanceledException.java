package org.plovdev.keyer.exceptions;

/**
 * Thrown when an authentication process is explicitly canceled by the user.
 * <p>
 * This typically occurs when a user clicks the "Cancel" button in a
 * system dialog.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.7
 */
public class AuthenticationCanceledException extends AccessDeniedException {
    /**
     * Constructs a new AuthenticationCanceledException with no detail message.
     */
    public AuthenticationCanceledException() {
    }

    /**
     * Constructs a new AuthenticationCanceledException with the specified detail message.
     *
     * @param message the detail message
     */
    public AuthenticationCanceledException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuthenticationCanceledException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public AuthenticationCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AuthenticationCanceledException with the specified cause.
     *
     * @param cause the underlying cause
     */
    public AuthenticationCanceledException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new AuthenticationCanceledException with the specified detail message, cause,
     * suppression control, and writable stack trace control.
     *
     * @param message            the detail message
     * @param cause              the underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public AuthenticationCanceledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}