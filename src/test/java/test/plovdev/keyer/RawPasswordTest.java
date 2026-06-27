package test.plovdev.keyer;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.plovdev.keyer.Keychain;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RawPasswordTest {
    private static final String APP_ID = "KeyerApp";
    private static final String ALIAS = "Keyer-raw";

    private static final Keychain KEYCHAIN = Keychain.getKeychain(RawPasswordTest.class);

    @Order(1)
    @Test
    void testSetPassword() {
        assertDoesNotThrow(() -> KEYCHAIN.setPasswordRaw(ALIAS, "password".getBytes()));
    }

    @Order(2)
    @Test
    void testGetPassword() {
        byte[] password = KEYCHAIN.getRawPassword(ALIAS);
        assertArrayEquals("password".getBytes(), password);
    }

    @Order(3)
    @Test
    void testDeletePassword() {
        assertDoesNotThrow(() -> KEYCHAIN.deletePassword(ALIAS));
        assertNull(KEYCHAIN.getRawPassword(ALIAS));
    }
}