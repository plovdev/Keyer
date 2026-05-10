package test.plovdev.keyer;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.plovdev.keyer.Keychain;
import org.plovdev.keyer.Platform;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EdgeCasesTest {
    private static final String APP_ID = "KeyerApp";
    private static final String ALIAS = "Keyer";
    private static final Keychain keychain = Keychain.getKeychain(APP_ID);

    @Test
    @Order(1)
    void testEmptyPassword() {
        keychain.deletePassword(ALIAS);
        char[] emptyPass = new char[0];
        assertDoesNotThrow(() -> keychain.setPassword(ALIAS, emptyPass));
        assertArrayEquals(emptyPass, keychain.getPassword(ALIAS));
    }

    @Test
    void testVeryLongPassword() {
        char[] longPass = new char[Platform.guessPlatform() == Platform.MAC ? 1000000 : 500];
        Arrays.fill(longPass, 'a');
        assertDoesNotThrow(() -> keychain.setPassword(ALIAS, longPass));
        assertArrayEquals(longPass, keychain.getPassword(ALIAS));
    }

    @Test
    void testSpecialCharactersInAlias() {
        String specialAlias = "user@example.com:8080/path?q=1#fragment";
        assertDoesNotThrow(() -> keychain.setPassword(specialAlias, "pass".toCharArray()));
        assertArrayEquals("pass".toCharArray(), keychain.getPassword(specialAlias));
        keychain.deletePassword(specialAlias);
    }

    @Test
    void testUnicodeInPassword() {
        char[] unicodePass = "密码🔐пароль😀".toCharArray();
        assertDoesNotThrow(() -> keychain.setPassword(ALIAS, unicodePass));
        assertArrayEquals(unicodePass, keychain.getPassword(ALIAS));
    }

    @Test
    void testBinaryDataAsPassword() {
        char[] binaryLike = new char[256];
        for (int i = 0; i < binaryLike.length; i++) {
            binaryLike[i] = (char) i;
        }
        assertDoesNotThrow(() -> keychain.setPassword(ALIAS, binaryLike));
        assertArrayEquals(binaryLike, keychain.getPassword(ALIAS));
    }

    @Test
    void testMultipleAppsIsolation() {
        Keychain app1 = Keychain.getKeychain("App1");
        Keychain app2 = Keychain.getKeychain("App2");

        app1.setPassword(ALIAS, "secret1".toCharArray());
        app2.setPassword(ALIAS, "secret2".toCharArray());

        assertArrayEquals("secret1".toCharArray(), app1.getPassword(ALIAS));
        assertArrayEquals("secret2".toCharArray(), app2.getPassword(ALIAS));

        app1.deletePassword(ALIAS);
        app2.deletePassword(ALIAS);
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void testGetNonExistentReturnsNull() {
        keychain.deletePassword(ALIAS);
        assertNull(keychain.getPassword("non_existent_alias"));
    }

    @Test
    void testDeleteNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> keychain.deletePassword("non_existent"));
    }

    @Test
    void testSetNullPassword() {
        assertThrows(NullPointerException.class, () -> keychain.setPassword(ALIAS, null));
    }

    @AfterAll
    static void cleanup() {
        keychain.deletePassword(ALIAS);
    }
}