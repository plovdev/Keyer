package org.plovdev.keyer.exceptions;

import org.plovdev.keyer.Platform;

public class PlatformNotSupportedException extends KeyerException {
    private Platform platform;

    public PlatformNotSupportedException() {
    }

    public PlatformNotSupportedException(KeyerStatusCode statusCode) {
        super(statusCode);
    }

    public PlatformNotSupportedException(String message) {
        super(message);
    }

    public PlatformNotSupportedException(String message, Platform platform, KeyerStatusCode statusCode) {
        super(message, statusCode);
        this.platform = platform;
    }

    public PlatformNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlatformNotSupportedException(String message, KeyerStatusCode statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    public PlatformNotSupportedException(Throwable cause) {
        super(cause);
    }

    public PlatformNotSupportedException(KeyerStatusCode statusCode, Throwable cause) {
        super(statusCode, cause);
    }

    public PlatformNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PlatformNotSupportedException(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, statusCode, cause, enableSuppression, writableStackTrace);
    }

    public Platform getPlatform() {
        return platform;
    }
}