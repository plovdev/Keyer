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
     * Constructs a new VerificationFailedException with no detail message.
     */
    public VerificationFailedException() {
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
     * Constructs a new VerificationFailedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public VerificationFailedException(String message, Throwable cause) {
        super(message, cause);
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
}