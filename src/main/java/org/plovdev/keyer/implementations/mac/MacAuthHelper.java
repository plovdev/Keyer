package org.plovdev.keyer.implementations.mac;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.exceptions.KeyerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.HashSet;
import java.util.Set;

/**
 * Internal helper for macOS authentication and biometric hardware detection.
 *
 * @author Anton
 * @version 1.7
 * @since 1.7
 */
public final class MacAuthHelper {
    private static final Logger log = LoggerFactory.getLogger(MacAuthHelper.class);

    private MacAuthHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * Detects hardware and software support for authentication methods.
     *
     * @param ctrl handle to SecAccessControlCreateWithFlags
     * @param wu   pointer to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
     * @return set of supported {@link AuthorizationMethod}
     */
    public static @NonNull Set<AuthorizationMethod> supportedAuthMethods(MethodHandle ctrl, MemorySegment wu) {
        Set<AuthorizationMethod> methods = new HashSet<>();
        methods.add(AuthorizationMethod.NONE);
        methods.add(AuthorizationMethod.PASSWORD);

        if (isBiometryAvailable(ctrl, wu)) {
            methods.add(AuthorizationMethod.BIOMETRY);
        }
        return methods;
    }

    /**
     * Checks if biometric sensors are present and accessible.
     */
    private static boolean isBiometryAvailable(MethodHandle controlCreate, MemorySegment whenUnlocked) {
        try {
            createAccessControl(Arena.ofConfined(), controlCreate, whenUnlocked);
            return true;
        } catch (Throwable ignored) {
            log.debug("Biometry is not available");
        }
        return false;
    }

    /**
     * Creates a native SecAccessControl object requiring User Presence (Biometrics/Password).
     *
     * @return pointer to SecAccessControlRef
     * @throws Throwable if native allocation or creation fails
     */
    public static @NonNull MemorySegment createAccessControl(@NonNull Arena arena, @NonNull MethodHandle controlCreate, MemorySegment whenUnlocked) throws Throwable {
        var errorPtr = arena.allocate(ValueLayout.ADDRESS);
        MemorySegment result = (MemorySegment) controlCreate.invokeExact(MemorySegment.NULL, whenUnlocked, 2L, errorPtr);
        if (result.address() == 0) {
            throw new KeyerException("Failed to create AccessControl");
        }
        return result;
    }
}