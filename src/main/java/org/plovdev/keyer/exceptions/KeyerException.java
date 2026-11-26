package org.plovdev.keyer.exceptions;

/**
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.6
 */
public class KeyerException extends RuntimeException {
    public KeyerException() {
    }

    public KeyerException(String message) {
        super(message);
    }

    public KeyerException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyerException(Throwable cause) {
        super(cause);
    }

    public KeyerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}