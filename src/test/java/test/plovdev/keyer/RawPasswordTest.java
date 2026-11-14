package test.plovdev.keyer;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.plovdev.keyer.Keychain;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RawPasswordTest {
    private static final String ALIAS = "Keyer-raw";

    private static final Keychain KEYCHAIN = Keychain.getKeychain(RawPasswordTest.class);

    @Order(1)
    @Test
    void testSetPassword() {
        assertDoesNotThrow(() -> KEYCHAIN.setPassword(ALIAS, "password".getBytes(StandardCharsets.UTF_16LE)));
    }

    @Order(2)
    @Test
    void testGetPassword() {
        byte[] password = KEYCHAIN.getRawPassword(ALIAS);
        assertArrayEquals("password".getBytes(StandardCharsets.UTF_16LE), password);
    }

    @Order(3)
    @Test
    void testDeletePassword() {
        assertDoesNotThrow(() -> KEYCHAIN.deletePassword(ALIAS));
        assertNull(KEYCHAIN.getRawPassword(ALIAS));
    }
}