package test.plovdev.keyer;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.plovdev.keyer.utils.CharactersUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CharactersUtilsTest {
    @ParameterizedTest
    @ValueSource(strings = {"", "a", "password", "密码🔐пароль😀", "\\0\u0000x", "  ", "\t\n\r"})
    void utf8_roundTrip(@NonNull String s) {
        char[] chars = s.toCharArray();
        byte[] bytes = CharactersUtils.charsUTF_8ToBytes(chars);
        assertArrayEquals(chars, CharactersUtils.bytesToCharsUTF_8(bytes), "round-trip: " + s);
    }

    @Test
    void utf8_matchesStandard() {
        char[] chars = "hello".toCharArray();
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8),
                CharactersUtils.charsUTF_8ToBytes(chars));
    }

    @Test
    void utf8_nullInput() {
        assertThrows(NullPointerException.class, () -> CharactersUtils.charsUTF_8ToBytes(null));
        assertThrows(NullPointerException.class, () -> CharactersUtils.bytesToCharsUTF_8(null));
    }

    @Test
    void utf8_empty() {
        assertArrayEquals(new byte[0], CharactersUtils.charsUTF_8ToBytes(new char[0]));
        assertArrayEquals(new char[0], CharactersUtils.bytesToCharsUTF_8(new byte[0]));
    }

    @Test
    void utf8_malformedBytes_replaced() {
        byte[] broken = {(byte) 0xC3, (byte) 0x28};
        char[] result = CharactersUtils.bytesToCharsUTF_8(broken);
        assertEquals(2, result.length);
        assertEquals('\uFFFD', result[0]);
    }

    @Test
    void utf8_singleInvalid() {
        char[] result = CharactersUtils.bytesToCharsUTF_8(new byte[]{(byte) 0xFF});
        assertEquals(1, result.length);
        assertEquals('\uFFFD', result[0]);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "password", "密码🔐пароль😀", "\u0000", "😀"})
    void utf16le_roundTrip(@NonNull String s) {
        char[] chars = s.toCharArray();
        byte[] bytes = CharactersUtils.charsUTF_16LEToBytes(chars);
        assertArrayEquals(chars, CharactersUtils.bytesToCharsUTF_16LE(bytes), "round-trip: " + s);
    }

    @Test
    void utf16le_matchesStandard() {
        char[] chars = "hi".toCharArray();
        assertArrayEquals("hi".getBytes(StandardCharsets.UTF_16LE),
                CharactersUtils.charsUTF_16LEToBytes(chars));
    }

    @Test
    void utf16le_oddLength_replaced() {
        char[] result = CharactersUtils.bytesToCharsUTF_16LE(new byte[]{0x41});
        assertEquals(1, result.length);
        assertEquals('\uFFFD', result[0]);
    }

    @Test
    void utf16le_nullInput() {
        assertThrows(NullPointerException.class, () -> CharactersUtils.charsUTF_16LEToBytes(null));
        assertThrows(NullPointerException.class, () -> CharactersUtils.bytesToCharsUTF_16LE(null));
    }

    @Test
    void encodingsDiffer() {
        char[] chars = "é".toCharArray();
        assertFalse(java.util.Arrays.equals(CharactersUtils.charsUTF_8ToBytes(chars), CharactersUtils.charsUTF_16LEToBytes(chars)));
    }

    @Test
    void utf16le_surrogatePair() {
        char[] emoji = "😀".toCharArray();
        assertEquals(2, emoji.length);
        byte[] bytes = CharactersUtils.charsUTF_16LEToBytes(emoji);
        assertEquals(4, bytes.length);
        assertArrayEquals(emoji, CharactersUtils.bytesToCharsUTF_16LE(bytes));
    }

    @Test
    void constructors_private() throws Exception {
        var ctor = CharactersUtils.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        var ex = assertThrows(java.lang.reflect.InvocationTargetException.class, ctor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
    }
}