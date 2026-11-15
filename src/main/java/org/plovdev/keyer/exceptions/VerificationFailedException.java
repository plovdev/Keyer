package org.plovdev.keyer.exceptions;

/**
 * Thrown when verification of user credentials fails.
 * <p>
 * This typically occurs when biometric authentication does not match
 * the enrolled data, or when an incorrect system password is entered.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.8-beta
 */
public class VerificationFailedException extends KeyerException {
    /**
     * Constructs a new VerificationFailedException.
     */
    public VerificationFailedException() {
    }

    /**
     * Constructs a new VerificationFailedException with the specified status code.
     *
     * @param statusCode the error code
     */
    public VerificationFailedException(KeyerStatusCode statusCode) {
        super(statusCode);
    }

    /**
     * Constructs a new VerificationFailedException with the specified detail message.
     *
     * @param message the detail message
     */
    public VerificationFailedException(String message) {
        super(message);
    }

    /**
     * Constructs a new VerificationFailedException with the specified detail message and status code.
     *
     * @param message    the detail message
     * @param statusCode the error code
     */
    public VerificationFailedException(String message, KeyerStatusCode statusCode) {
        super(message, statusCode);
    }

    /**
     * Constructs a new VerificationFailedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public VerificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new VerificationFailedException with the specified detail message, status code, and cause.
     *
     * @param message    the detail message
     * @param statusCode the error code
     * @param cause      the underlying cause
     */
    public VerificationFailedException(String message, KeyerStatusCode statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    /**
     * Constructs a new VerificationFailedException with the specified cause.
     *
     * @param cause the underlying cause
     */
    public VerificationFailedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new VerificationFailedException with the specified status code and cause.
     *
     * @param statusCode the error code
     * @param cause      the underlying cause
     */
    public VerificationFailedException(KeyerStatusCode statusCode, Throwable cause) {
        super(statusCode, cause);
    }

    /**
     * Constructs a new VerificationFailedException with the specified detail message, cause,
     * suppression control, and writable stack trace control.
     *
     * @param message            the detail message
     * @param cause              the underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public VerificationFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructs a new VerificationFailedException with full control over the detail message,
     * status code, cause, suppression, and stack trace.
     *
     * @param message            the detail message
     * @param statusCode         the error code
     * @param cause              the underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public VerificationFailedException(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, statusCode, cause, enableSuppression, writableStackTrace);
    }
}