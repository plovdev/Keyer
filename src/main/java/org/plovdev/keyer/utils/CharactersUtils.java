package org.plovdev.keyer.utils;

import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public final class CharactersUtils {
    private CharactersUtils() {
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
}