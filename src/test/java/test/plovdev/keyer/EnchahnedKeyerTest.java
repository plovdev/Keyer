package test.plovdev.keyer;

import org.junit.jupiter.api.*;
import org.plovdev.keyer.Keychain;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnchahnedKeyerTest {
    private static final String APP_ID = "KeyerApp";
    private static final String ALIAS = "Keyer";

    private static final Keychain keychain = Keychain.getKeychain(APP_ID);

    @Order(1)
    @Test
    void testInitialState() {
        keychain.deletePassword(ALIAS);
        assertNull(keychain.getPassword(ALIAS), "Password must be empty");
    }

    @Order(2)
    @Test
    void testSetAndOverwritePassword() {
        char[] firstPass = "first_pass".toCharArray();
        char[] secondPass = "second_pass".toCharArray();

        assertDoesNotThrow(() -> keychain.setPassword(ALIAS, firstPass));
        assertDoesNotThrow(() -> keychain.setPassword(ALIAS, secondPass));

        assertArrayEquals(secondPass, keychain.getPassword(ALIAS));
    }

    @Order(3)
    @Test
    void testDeleteAndVerify() {
        assertDoesNotThrow(() -> keychain.deletePassword(ALIAS));
        assertNull(keychain.getPassword(ALIAS));
    }

    @Order(4)
    @Test
    void testDeleteNonExistent() {
        assertDoesNotThrow(() -> keychain.deletePassword(ALIAS));
    }

    @AfterAll
    static void cleanup() {
        keychain.deletePassword(ALIAS);
    }
}