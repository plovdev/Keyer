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
    public AccessDeniedException() {
    }

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessDeniedException(Throwable cause) {
        super(cause);
    }

    public AccessDeniedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}