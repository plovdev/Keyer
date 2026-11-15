package org.plovdev.keyer.exceptions;

import org.jspecify.annotations.Nullable;
import org.plovdev.keyer.AuthorizationMethod;

/**
 * Thrown when an authorization method is not supported by the current platform
 * or keychain implementation.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.8-beta
 */
public class UnsupportedAuthorizationMethodException extends UnsupportedOperationException {
    private AuthorizationMethod method;

    /**
     * Constructs a new UnsupportedAuthorizationMethodException.
     */
    public UnsupportedAuthorizationMethodException() {
    }

    /**
     * Constructs a new UnsupportedAuthorizationMethodException with the specified unsupported method.
     *
     * @param method the authorization method that is not supported
     */
    public UnsupportedAuthorizationMethodException(AuthorizationMethod method) {
        super("Unsupported authorization method");
        this.method = method;
    }

    /**
     * Constructs a new UnsupportedAuthorizationMethodException with the specified detail message.
     *
     * @param message the detail message
     */
    public UnsupportedAuthorizationMethodException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnsupportedAuthorizationMethodException with the specified detail message
     * and unsupported method.
     *
     * @param message the detail message
     * @param method  the authorization method that is not supported
     */
    public UnsupportedAuthorizationMethodException(String message, AuthorizationMethod method) {
        super(message);
        this.method = method;
    }

    /**
     * Constructs a new UnsupportedAuthorizationMethodException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public UnsupportedAuthorizationMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new UnsupportedAuthorizationMethodException with the specified detail message,
     * unsupported method, and cause.
     *
     * @param message the detail message
     * @param method  the authorization method that is not supported
     * @param cause   the underlying cause
     */
    public UnsupportedAuthorizationMethodException(String message, AuthorizationMethod method, Throwable cause) {
        super(message, cause);
        this.method = method;
    }

    /**
     * Returns the authorization method that is not supported.
     *
     * @return the unsupported authorization method
     */
    public @Nullable AuthorizationMethod getMethod() {
        return method;
    }
}