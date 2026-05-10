package org.plovdev.keyer.exceptions;

/**
 * Base runtime exception for Keyer library operations.
 * <p>
 * Carries a platform-independent {@link KeyerStatusCode} that maps to native
 * error codes from Windows (GetLastError), macOS (OSStatus), or Unix (libsecret).
 * <p>
 * All constructors default to {@link KeyerStatusCode#UNKNOWN_ERROR} when no
 * status code is explicitly provided.
 *
 * @author Anton
 * @version 1.6
 * @see KeyerStatusCode
 * @since 1.6
 */
public class KeyerException extends RuntimeException {
    protected final KeyerStatusCode statusCode;

    /**
     * Constructs a new exception with {@link KeyerStatusCode#UNKNOWN_ERROR}.
     */
    public KeyerException() {
        this.statusCode = KeyerStatusCode.UNKNOWN_ERROR;
    }

    /**
     * Constructs a new exception with the specified status code.
     *
     * @param statusCode platform-independent error code
     */
    public KeyerException(KeyerStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new exception with the specified detail message
     * and {@link KeyerStatusCode#UNKNOWN_ERROR}.
     *
     * @param message detail message
     */
    public KeyerException(String message) {
        this.statusCode = KeyerStatusCode.UNKNOWN_ERROR;
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and status code.
     *
     * @param message    detail message
     * @param statusCode platform-independent error code
     */
    public KeyerException(String message, KeyerStatusCode statusCode) {
        this.statusCode = statusCode;
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause,
     * and {@link KeyerStatusCode#UNKNOWN_ERROR}.
     *
     * @param message detail message
     * @param cause   underlying cause
     */
    public KeyerException(String message, Throwable cause) {
        this.statusCode = KeyerStatusCode.UNKNOWN_ERROR;
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message, status code, and cause.
     *
     * @param message    detail message
     * @param statusCode platform-independent error code
     * @param cause      underlying cause
     */
    public KeyerException(String message, KeyerStatusCode statusCode, Throwable cause) {
        this.statusCode = statusCode;
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause
     * and {@link KeyerStatusCode#UNKNOWN_ERROR}.
     *
     * @param cause underlying cause
     */
    public KeyerException(Throwable cause) {
        this.statusCode = KeyerStatusCode.UNKNOWN_ERROR;
        super(cause);
    }

    /**
     * Constructs a new exception with the specified status code and cause.
     *
     * @param statusCode platform-independent error code
     * @param cause      underlying cause
     */
    public KeyerException(KeyerStatusCode statusCode, Throwable cause) {
        this.statusCode = statusCode;
        super(cause);
    }

    /**
     * Constructs a new exception with full control over suppression and stack trace,
     * and {@link KeyerStatusCode#UNKNOWN_ERROR}.
     *
     * @param message            detail message
     * @param cause              underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether stack trace is writable
     */
    public KeyerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        this.statusCode = KeyerStatusCode.UNKNOWN_ERROR;
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructs a new exception with full control over suppression and stack trace.
     *
     * @param message            detail message
     * @param statusCode         platform-independent error code
     * @param cause              underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether stack trace is writable
     */
    public KeyerException(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        this.statusCode = statusCode;
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Returns the platform-independent status code associated with this exception.
     *
     * @return status code (never {@code null}, defaults to {@link KeyerStatusCode#UNKNOWN_ERROR})
     */
    public KeyerStatusCode getStatusCode() {
        return statusCode;
    }
}