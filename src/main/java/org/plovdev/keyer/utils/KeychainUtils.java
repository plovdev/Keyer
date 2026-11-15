package org.plovdev.keyer.utils;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.exceptions.UnsupportedAuthorizationMethodException;

import java.util.Collection;
import java.util.Objects;

/**
 * Utility methods for common Keyer operations.
 * <p>
 * This class cannot be instantiated.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.7.1
 */
public final class KeychainUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private KeychainUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks that the given authorization method is supported.
     *
     * @param supported the collection of supported authorization methods
     * @param method    the authorization method to check
     * @throws UnsupportedAuthorizationMethodException if {@code method} is not in {@code supported}
     */
    public static void checkAuthorizationMethod(@NonNull Collection<AuthorizationMethod> supported, AuthorizationMethod method) {
        if (!supported.contains(Objects.requireNonNull(method))) {
            throw new UnsupportedAuthorizationMethodException(method);
        }
    }
}