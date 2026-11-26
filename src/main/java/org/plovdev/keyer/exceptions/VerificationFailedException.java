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
    public VerificationFailedException() {
    }

    public VerificationFailedException(String message) {
        super(message);
    }

    public VerificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public VerificationFailedException(Throwable cause) {
        super(cause);
    }

    public VerificationFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}