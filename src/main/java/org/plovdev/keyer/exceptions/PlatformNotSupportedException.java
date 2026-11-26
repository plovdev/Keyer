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

    public PlatformNotSupportedException(Platform platform) {
        this.platform = platform;
    }

    public PlatformNotSupportedException(String message, Platform platform) {
        super(message);
        this.platform = platform;
    }

    public PlatformNotSupportedException(String message, Throwable cause, Platform platform) {
        super(message, cause);
        this.platform = platform;
    }

    public PlatformNotSupportedException(Throwable cause, Platform platform) {
        super(cause);
        this.platform = platform;
    }

    public PlatformNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Platform platform) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.platform = platform;
    }

    public PlatformNotSupportedException() {
    }

    public PlatformNotSupportedException(String message) {
        super(message);
    }

    public PlatformNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlatformNotSupportedException(Throwable cause) {
        super(cause);
    }

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