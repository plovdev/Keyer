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
 * @author PlovDev
 * @version 1.8
 * @since 1.6
 */
public class AccessDeniedException extends KeyerException {
    /**
     * Constructs a new AccessDeniedException with no detail message.
     */
    public AccessDeniedException() {
    }

    /**
     * Constructs a new AccessDeniedException with the specified detail message.
     *
     * @param message the detail message
     */
    public AccessDeniedException(String message) {
        super(message);
    }

    /**
     * Constructs a new AccessDeniedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AccessDeniedException with the specified cause.
     *
     * @param cause the underlying cause
     */
    public AccessDeniedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new AccessDeniedException with the specified detail message, cause,
     * suppression control, and writable stack trace control.
     *
     * @param message            the detail message
     * @param cause              the underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public AccessDeniedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}