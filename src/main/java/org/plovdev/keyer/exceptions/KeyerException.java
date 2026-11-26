package org.plovdev.keyer.exceptions;

/**
 * Base runtime exception for Keyer library operations.
 * <p>
 * This exception is thrown when a native keychain operation fails
 * for a reason that does not map to a more specific exception.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.6
 */
public class KeyerException extends RuntimeException {
    /**
     * Constructs a new KeyerException with no detail message.
     */
    public KeyerException() {
    }

    /**
     * Constructs a new KeyerException with the specified detail message.
     *
     * @param message the detail message
     */
    public KeyerException(String message) {
        super(message);
    }

    /**
     * Constructs a new KeyerException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public KeyerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new KeyerException with the specified cause.
     *
     * @param cause the underlying cause
     */
    public KeyerException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new KeyerException with the specified detail message, cause,
     * suppression control, and writable stack trace control.
     *
     * @param message            the detail message
     * @param cause              the underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public KeyerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}