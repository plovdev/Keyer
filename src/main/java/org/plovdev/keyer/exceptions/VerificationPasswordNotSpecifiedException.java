package org.plovdev.keyer.exceptions;

/**
 * Thrown when a password-based authorization is requested but no
 * verification password is configured on the system.
 * <p>
 * This typically occurs on machines where the user logs in without a password.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.8-beta
 */
public class VerificationPasswordNotSpecifiedException extends KeyerException {
    /**
     * Constructs a new VerificationPasswordNotSpecifiedException.
     */
    public VerificationPasswordNotSpecifiedException() {
    }

    /**
     * Constructs a new VerificationPasswordNotSpecifiedException with the specified detail message.
     *
     * @param message the detail message
     */
    public VerificationPasswordNotSpecifiedException(String message) {
        super(message);
    }

    /**
     * Constructs a new VerificationPasswordNotSpecifiedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public VerificationPasswordNotSpecifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new VerificationPasswordNotSpecifiedException with the specified cause.
     *
     * @param cause the underlying cause
     */
    public VerificationPasswordNotSpecifiedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new VerificationPasswordNotSpecifiedException with the specified detail message, cause,
     * suppression control, and writable stack trace control.
     *
     * @param message            the detail message
     * @param cause              the underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public VerificationPasswordNotSpecifiedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}