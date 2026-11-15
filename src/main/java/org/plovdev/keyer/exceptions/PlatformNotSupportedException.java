package org.plovdev.keyer.exceptions;

import org.plovdev.keyer.Platform;

/**
 * Thrown when the current operating system has no Keyer backend.
 * <p>
 * Carries the unsupported {@link Platform} when it is known.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.5
 */
public class PlatformNotSupportedException extends KeyerException {
    private Platform platform;

    /**
     * Constructs a new PlatformNotSupportedException .
     */
    public PlatformNotSupportedException() {
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified status code.
     *
     * @param statusCode platform-independent error code
     */
    public PlatformNotSupportedException(KeyerStatusCode statusCode) {
        super(statusCode);
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified detail message.
     *
     * @param message the detail message
     */
    public PlatformNotSupportedException(String message) {
        super(message);
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified detail message and platform.
     *
     * @param message    the detail message
     * @param platform   the platform that is not supported
     * @param statusCode the error code
     */
    public PlatformNotSupportedException(String message, Platform platform, KeyerStatusCode statusCode) {
        super(message, statusCode);
        this.platform = platform;
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public PlatformNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified detail message, status code, and cause.
     *
     * @param message    the detail message
     * @param statusCode the error code
     * @param cause      the underlying cause
     */
    public PlatformNotSupportedException(String message, KeyerStatusCode statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified cause.
     *
     * @param cause the underlying cause
     */
    public PlatformNotSupportedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified status code and cause.
     *
     * @param statusCode the error code
     * @param cause      the underlying cause
     */
    public PlatformNotSupportedException(KeyerStatusCode statusCode, Throwable cause) {
        super(statusCode, cause);
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified detail message, cause,
     * suppression control, and writable stack trace control.
     *
     * @param message            the detail message
     * @param cause              the underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public PlatformNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructs a new PlatformNotSupportedException with full control over the detail message,
     * status code, cause, suppression, and stack trace.
     *
     * @param message            the detail message
     * @param statusCode         the error code
     * @param cause              the underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public PlatformNotSupportedException(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, statusCode, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Returns the platform that is not supported.
     *
     * @return the unsupported platform, or {@code null} if it was not specified
     */
    public Platform getPlatform() {
        return platform;
    }
}