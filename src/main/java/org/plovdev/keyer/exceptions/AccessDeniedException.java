package org.plovdev.keyer.exceptions;

/**
 * Exception thrown when access to the keychain is denied.
 * <p>
 * This can happen due to:
 * <ul>
 *     <li>User denied access in the system dialog</li>
 *     <li>Incorrect keychain password (macOS)</li>
 *     <li>Insufficient privileges (Windows ERROR_ACCESS_DENIED)</li>
 *     <li>Keychain is locked (macOS errSecInteractionNotAllowed)</li>
 * </ul>
 *
 * @author Anton
 * @version 1.6
 * @since 1.6
 */
public class AccessDeniedException extends KeyerException {
    /**
     * Constructs a new AccessDeniedException with {@link KeyerStatusCode#UNKNOWN_ERROR}.
     */
    public AccessDeniedException() {
    }

    /**
     * Constructs a new AccessDeniedException with the specified status code.
     *
     * @param statusCode error code
     */
    public AccessDeniedException(KeyerStatusCode statusCode) {
        super(statusCode);
    }

    /**
     * Constructs a new AccessDeniedException with the specified detail message
     * and {@link KeyerStatusCode#UNKNOWN_ERROR}.
     *
     * @param message detail message
     */
    public AccessDeniedException(String message) {
        super(message);
    }

    /**
     * Constructs a new AccessDeniedException with the specified detail message and status code.
     *
     * @param message    detail message
     * @param statusCode error code
     */
    public AccessDeniedException(String message, KeyerStatusCode statusCode) {
        super(message, statusCode);
    }

    /**
     * Constructs a new AccessDeniedException with the specified detail message and cause.
     *
     * @param message detail message
     * @param cause   underlying cause
     */
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AccessDeniedException with the specified detail message, status code and cause.
     *
     * @param message    detail message
     * @param statusCode error code
     * @param cause      underlying cause
     */
    public AccessDeniedException(String message, KeyerStatusCode statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    /**
     * Constructs a new AccessDeniedException with the specified cause.
     *
     * @param cause underlying cause
     */
    public AccessDeniedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new AccessDeniedException with the specified status code and cause.
     *
     * @param statusCode platform-independent error code
     * @param cause      underlying cause
     */
    public AccessDeniedException(KeyerStatusCode statusCode, Throwable cause) {
        super(statusCode, cause);
    }

    /**
     * Constructs a new AccessDeniedException with full control over suppression and stack trace.
     *
     * @param message            detail message
     * @param cause              underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether stack trace is writable
     */
    public AccessDeniedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructs a new AccessDeniedException with full control over suppression and stack trace.
     *
     * @param message            detail message
     * @param statusCode         error code
     * @param cause              underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether stack trace is writable
     */
    public AccessDeniedException(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, statusCode, cause, enableSuppression, writableStackTrace);
    }
}