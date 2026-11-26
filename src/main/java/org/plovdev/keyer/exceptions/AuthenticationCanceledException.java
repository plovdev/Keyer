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
    public AuthenticationCanceledException() {
    }

    public AuthenticationCanceledException(String message) {
        super(message);
    }

    public AuthenticationCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationCanceledException(Throwable cause) {
        super(cause);
    }

    public AuthenticationCanceledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}