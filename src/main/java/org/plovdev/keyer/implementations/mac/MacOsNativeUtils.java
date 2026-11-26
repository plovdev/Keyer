package org.plovdev.keyer.implementations.mac;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.exceptions.AccessDeniedException;
import org.plovdev.keyer.exceptions.AuthenticationCanceledException;
import org.plovdev.keyer.exceptions.KeyerException;
import org.plovdev.keyer.exceptions.MissingEntitlementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import static org.plovdev.keyer.utils.NativeUtils.*;

/**
 * Internal helper for creating macOS access control objects used
 * for biometric and password authorization.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.7
 */
public final class MacOsNativeUtils {
    private static final Arena ARENA = Arena.ofShared();

    /**
     * Path to the binary inside the Security framework.
     */
    static final SymbolLookup SECURITY = SymbolLookup.libraryLookup("/System/Library/Frameworks/Security.framework/Versions/A/Security", ARENA);

    private static final MethodHandle CF_DICT_CREATE = findFunction(SECURITY, LINKER, "CFDictionaryCreate", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle CF_STR_CREATE = findFunction(SECURITY, LINKER, "CFStringCreateWithCharacters", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
    private static final MethodHandle CF_RELEASE = findFunction(SECURITY, LINKER, "CFRelease", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    private static final MemorySegment KEY_CALLBACKS = findConstant(SECURITY, "kCFTypeDictionaryKeyCallBacks");
    private static final MemorySegment VAL_CALLBACKS = findConstant(SECURITY, "kCFTypeDictionaryValueCallBacks");

    //====ERROR CODES====\\
    static final int SUCCESS = 0;
    static final int NOT_FOUND = -25300;
    static final int AUTH_CANCELED = -128;
    static final int MISSING_ENTITLEMENT = -34018;
    static final int ACCESS_DENIED = -25293;
    static final int ACCESS_LOCKED = -25308;
    private static final Logger log = LoggerFactory.getLogger(MacOsNativeUtils.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private MacOsNativeUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a native SecAccessControl object requiring the specified access method.
     *
     * @param arena         the arena used to allocate native memory
     * @param controlCreate the native SecAccessControlCreateWithFlags function handle
     * @param whenUnlocked  the accessibility constant the control is bound to
     * @param method        the access control flags (e.g., biometrics or device passcode)
     * @return a pointer to the created SecAccessControlRef as a {@link MemorySegment}
     * @throws KeyerException if the native SecAccessControl object cannot be created
     * @throws Throwable      if a native call fails
     */
    static @NonNull MemorySegment createAccessControl(@NonNull Arena arena, @NonNull MethodHandle controlCreate, MemorySegment whenUnlocked, long method) throws Throwable {
        var errorPtr = arena.allocate(ValueLayout.ADDRESS);
        MemorySegment result = (MemorySegment) controlCreate.invokeExact(MemorySegment.NULL, whenUnlocked, method, errorPtr);
        if (result.address() == 0) {
            executeClear(errorPtr.get(ValueLayout.ADDRESS, 0));
            throw new KeyerException("Failed to create AccessControl");
        }
        return result.reinterpret(arena, MacOsNativeUtils::executeClear);
    }

    /**
     * Throws exception from status code
     *
     * @param status operation status code
     */
    static void throwException(int status) {
        switch (status) {
            case SUCCESS, NOT_FOUND:
                break;
            case AUTH_CANCELED:
                throw new AuthenticationCanceledException();
            case MISSING_ENTITLEMENT:
                throw new MissingEntitlementException("Missing entitlement. Code " + status);
            case ACCESS_DENIED, ACCESS_LOCKED:
                throw new AccessDeniedException();
            default:
                throw new KeyerException(String.format("Keyer operation failed (Code: %d)", status));
        }
    }

    /**
     * Creates a native CFString object from a Java string using UTF-16LE encoding.
     *
     * @param arena the arena used to allocate native memory
     * @param str   the Java string to convert
     * @return a pointer to the created CFStringRef as a {@link MemorySegment}
     * @throws KeyerException if the native CFString object cannot be created
     * @throws Throwable      if a native call fails
     */
    static @NonNull MemorySegment createCFString(@NonNull Arena arena, @NonNull String str) throws Throwable {
        MemorySegment chars = arena.allocateFrom(str, StandardCharsets.UTF_16LE);
        MemorySegment ref = (MemorySegment) CF_STR_CREATE.invokeExact(MemorySegment.NULL, chars, (long) str.length());
        if (ref.address() == 0) throw new KeyerException("Failed to create CFString");
        return ref;
    }

    /**
     * Creates a native CFDictionary object from the given keys and values.
     *
     * @param arena  the arena used to allocate native memory
     * @param keys   the array of dictionary keys
     * @param values the array of dictionary values
     * @return a pointer to the created CFDictionaryRef as a {@link MemorySegment}
     * @throws KeyerException if the native CFDictionary object cannot be created
     * @throws Throwable      if a native call fails
     */
    static @NonNull MemorySegment createDict(@NonNull Arena arena, MemorySegment @NonNull [] keys, MemorySegment @NonNull [] values) throws Throwable {
        var keysPtr = arena.allocate(ValueLayout.ADDRESS, keys.length);
        var valsPtr = arena.allocate(ValueLayout.ADDRESS, values.length);
        for (int i = 0; i < keys.length; i++) {
            keysPtr.setAtIndex(ValueLayout.ADDRESS, i, keys[i]);
            valsPtr.setAtIndex(ValueLayout.ADDRESS, i, values[i]);
        }

        MemorySegment dict = (MemorySegment) CF_DICT_CREATE.invokeExact(MemorySegment.NULL, keysPtr, valsPtr, (long) keys.length, KEY_CALLBACKS, VAL_CALLBACKS);
        if (dict.address() == 0) throw new KeyerException("Failed to create dict");
        return dict;
    }

    /**
     * Releases native CoreFoundation objects.
     * <p>
     * Null pointers are safely ignored.
     *
     * @param toClear the native objects to release
     */
    static void executeClear(MemorySegment @NonNull ... toClear) {
        for (MemorySegment segment : toClear) {
            try {
                if (!segment.equals(MemorySegment.NULL) && segment.address() != 0) {
                    CF_RELEASE.invokeExact(segment);
                }
            } catch (Throwable e) {
                log.error("Error clear object: ", e);
            }
        }
    }
}