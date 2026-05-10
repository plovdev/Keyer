package org.plovdev.keyer.utils;

import org.jspecify.annotations.NonNull;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

/**
 * Utility class providing common operations for native interoperability,
 * primarily focused on character encoding conversions and native function linking.
 *
 * @author Anton
 * @version 1.7
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

    /**
     * Converts a char array to a UTF-8 byte array.
     */
    public static byte @NonNull [] charsUTF_8ToBytes(char[] chars) {
        ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        return bytes;
    }

    /**
     * Converts a UTF-8 byte array to a char array.
     */
    public static char @NonNull [] bytesToCharsUTF_8(byte[] bytes) {
        CharBuffer cb = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
        char[] chars = new char[cb.remaining()];
        cb.get(chars);
        return chars;
    }

    /**
     * Converts a char array to a UTF-16LE byte array.
     */
    public static byte @NonNull [] charsUTF_16LEToBytes(char[] chars) {
        ByteBuffer bb = StandardCharsets.UTF_16LE.encode(CharBuffer.wrap(chars));
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        return bytes;
    }

    /**
     * Converts a UTF-16LE byte array to a char array.
     */
    public static char @NonNull [] bytesToCharsUTF_16LE(byte[] bytes) {
        CharBuffer cb = StandardCharsets.UTF_16LE.decode(ByteBuffer.wrap(bytes));
        char[] chars = new char[cb.remaining()];
        cb.get(chars);
        return chars;
    }

    /**
     * Finds and links a native function by name.
     *
     * @param name native function name
     * @param desc function signature descriptor
     * @return linked MethodHandle
     * @throws NoSuchElementException if the function not found
     */
    public static @NonNull MethodHandle find(@NonNull SymbolLookup lookup, Linker linker, String name, FunctionDescriptor desc) {
        return lookup.find(name).map(s -> linker.downcallHandle(s, desc)).orElseThrow(() -> new NoSuchElementException("Function not found: " + name));
    }

    /**
     * Finds a native constant by name.
     *
     * @param name native constant name
     * @return native constant in MemorySegment
     * @throws NoSuchElementException if the constant not found
     */
    public static MemorySegment getConstant(@NonNull SymbolLookup lookup, String name) {
        return lookup.find(name).orElseThrow(() -> new NoSuchElementException("Constant not found: " + name)).reinterpret(8).get(ValueLayout.ADDRESS, 0);
    }
}