package org.plovdev.keyer.utils;

import org.jspecify.annotations.NonNull;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.NoSuchElementException;

/**
 * Utility class providing common operations for native interoperability,
 * primarily focused on native function linking.
 *
 * @author Anton
 * @version 1.8
 * @since 1.0
 */
public final class NativeUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always.
     */
    private NativeUtils() {
        throw new UnsupportedOperationException();
    }

    public static @NonNull VarHandle findVarHandle(@NonNull StructLayout layout, @NonNull String name) {
        return layout.varHandle(MemoryLayout.PathElement.groupElement(name));
    }

    /**
     * Finds and links a native function by name.
     *
     * @param name native function name
     * @param desc function signature descriptor
     * @return linked MethodHandle
     * @throws NoSuchElementException if the function not found
     */
    public static @NonNull MethodHandle findFunction(@NonNull SymbolLookup lookup, Linker linker, String name, FunctionDescriptor desc, Linker.Option... options) {
        return lookup.find(name).map(s -> linker.downcallHandle(s, desc, options)).orElseThrow(() -> new NoSuchElementException("Function not found: " + name));
    }

    /**
     * Finds a native constant by name.
     *
     * @param name native constant name
     * @return native constant in MemorySegment
     * @throws NoSuchElementException if the constant not found
     */
    public static MemorySegment findConstant(@NonNull SymbolLookup lookup, String name) {
        return lookup.find(name).orElseThrow(() -> new NoSuchElementException("Constant not found: " + name)).reinterpret(8).get(ValueLayout.ADDRESS, 0);
    }
}