package org.plovdev.keyer.utils;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.AuthorizationMethod;

import java.util.Collection;

public final class KeychainUtils {
    private KeychainUtils() {
        throw new UnsupportedOperationException();
    }

    public static void checkAuthorizationMethod(@NonNull Collection<AuthorizationMethod> supported, AuthorizationMethod method) {
        if (!supported.contains(method)) {
            throw new IllegalArgumentException("Unsupported authorization method: " + method.name());
        }
    }
}