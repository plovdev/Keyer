package test.plovdev.keyer;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.plovdev.keyer.Keychain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleKeyerTest {
    private static final String APP_ID = "KeyerApp";
    private static final String ALIAS = "Keyer";

    private final Keychain keychain = Keychain.getKeychain(APP_ID);

    @Order(1)
    @Test
    void testSetPassword() {
        assertDoesNotThrow(() -> keychain.setPassword(ALIAS, "password".toCharArray()));
    }

    @Order(2)
    @Test
    void testGetPassword() {
        char[] password = keychain.getPassword(ALIAS);
        assertArrayEquals("password".toCharArray(), password);
    }

    @Order(3)
    @Test
    void testDeletePassword() {
        assertDoesNotThrow(() -> keychain.deletePassword(ALIAS));
    }
}