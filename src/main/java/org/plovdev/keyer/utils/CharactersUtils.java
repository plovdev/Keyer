package org.plovdev.keyer.utils;

import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Utility methods for converting between {@code char[]} and {@code byte[]}
 * using UTF-8 and UTF-16LE charsets.
 * <p>
 * This class cannot be instantiated.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.8-beta
 */
public final class CharactersUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private CharactersUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts a char array to a UTF-8 byte array.
     *
     * @param chars the characters to encode
     * @return the encoded bytes
     */
    public static byte @NonNull [] charsUTF_8ToBytes(char[] chars) {
        ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        return bytes;
    }

    /**
     * Converts a UTF-8 byte array to a char array.
     *
     * @param bytes the bytes to decode
     * @return the decoded characters
     */
    public static char @NonNull [] bytesToCharsUTF_8(byte[] bytes) {
        CharBuffer cb = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
        char[] chars = new char[cb.remaining()];
        cb.get(chars);
        return chars;
    }

    /**
     * Converts a char array to a UTF-16LE byte array.
     *
     * @param chars the characters to encode
     * @return the encoded bytes
     */
    public static byte @NonNull [] charsUTF_16LEToBytes(char[] chars) {
        ByteBuffer bb = StandardCharsets.UTF_16LE.encode(CharBuffer.wrap(chars));
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        return bytes;
    }

    /**
     * Converts a UTF-16LE byte array to a char array.
     *
     * @param bytes the bytes to decode
     * @return the decoded characters
     */
    public static char @NonNull [] bytesToCharsUTF_16LE(byte[] bytes) {
        CharBuffer cb = StandardCharsets.UTF_16LE.decode(ByteBuffer.wrap(bytes));
        char[] chars = new char[cb.remaining()];
        cb.get(chars);
        return chars;
    }
}