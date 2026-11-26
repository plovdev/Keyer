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
    /**
     * The platform that is not supported, or {@code null} if not specified.
     */
    private Platform platform;

    /**
     * Constructs a new PlatformNotSupportedException for the specified platform.
     *
     * @param platform the unsupported platform
     */
    public PlatformNotSupportedException(Platform platform) {
        this.platform = platform;
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified detail message and platform.
     *
     * @param message  the detail message
     * @param platform the unsupported platform
     */
    public PlatformNotSupportedException(String message, Platform platform) {
        super(message);
        this.platform = platform;
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified detail message, cause, and platform.
     *
     * @param message  the detail message
     * @param cause    the underlying cause
     * @param platform the unsupported platform
     */
    public PlatformNotSupportedException(String message, Throwable cause, Platform platform) {
        super(message, cause);
        this.platform = platform;
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified cause and platform.
     *
     * @param cause    the underlying cause
     * @param platform the unsupported platform
     */
    public PlatformNotSupportedException(Throwable cause, Platform platform) {
        super(cause);
        this.platform = platform;
    }

    /**
     * Constructs a new PlatformNotSupportedException with the specified detail message, cause,
     * suppression control, writable stack trace control, and platform.
     *
     * @param message            the detail message
     * @param cause              the underlying cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     * @param platform           the unsupported platform
     */
    public PlatformNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Platform platform) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.platform = platform;
    }

    /**
     * Constructs a new PlatformNotSupportedException with no detail message.
     */
    public PlatformNotSupportedException() {
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
     * Constructs a new PlatformNotSupportedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public PlatformNotSupportedException(String message, Throwable cause) {
        super(message, cause);
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
     * Returns the platform that is not supported.
     *
     * @return the unsupported platform, or {@code null} if it was not specified
     */
    public Platform getPlatform() {
        return platform;
    }
}