package org.plovdev.keyer.exceptions;

import org.plovdev.keyer.AuthorizationMethod;

public class UnsupportedAuthorizationMethodException extends UnsupportedOperationException {
    private AuthorizationMethod method;

    public UnsupportedAuthorizationMethodException() {
    }

    public UnsupportedAuthorizationMethodException(AuthorizationMethod method) {
        super("Unsupported authorization method");
        this.method = method;
    }

    public UnsupportedAuthorizationMethodException(String message) {
        super(message);
    }

    public UnsupportedAuthorizationMethodException(String message, AuthorizationMethod method) {
        super(message);
        this.method = method;
    }

    public UnsupportedAuthorizationMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedAuthorizationMethodException(String message, AuthorizationMethod method, Throwable cause) {
        super(message, cause);
        this.method = method;
    }

    public AuthorizationMethod getMethod() {
        return method;
    }
}