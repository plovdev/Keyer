package org.plovdev.keyer.implementations.mac;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.exceptions.KeyerException;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * Internal helper for creating macOS access control objects used
 * for biometric and password authorization.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.7
 */
public final class MacAuthHelper {
    /**
     * Private constructor to prevent instantiation.
     */
    private MacAuthHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a native SecAccessControl object requiring User Presence (Biometrics/Password).
     *
     * @param arena         the arena used to allocate native memory
     * @param controlCreate the native SecAccessControlCreateWithFlags function handle
     * @param whenUnlocked  the accessibility constant the control is bound to
     * @return a pointer to the created SecAccessControlRef as a {@link MemorySegment}
     * @throws KeyerException if the native SecAccessControl object cannot be created
     * @throws Throwable      if a native call fails
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