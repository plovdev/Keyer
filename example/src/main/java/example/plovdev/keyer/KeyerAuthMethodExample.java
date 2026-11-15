package example.plovdev.keyer;

import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.Keychain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Example demonstrating password-based authorization with Keyer.
 * <p>
 * Shows how to store a password using {@link AuthorizationMethod#PASSWORD}
 * and retrieve it, which prompts the OS password dialog on Windows.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.8
 */
public class KeyerAuthMethodExample {
    private static final Logger log = LoggerFactory.getLogger(KeyerAuthMethodExample.class);

    /**
     * Demonstrates storing a password with password-based authorization and
     * retrieving it back.
     * <p>
     * The password is encoded using UTF-16LE, matching the encoding used by
     * {@link Keychain#getPassword(String)}.
     */
    static void main() {
        Keychain keychain = Keychain.getKeychain(KeyerAuthMethodExample.class);
        keychain.setPassword("app", "пароль\uD83D\uDD11".getBytes(StandardCharsets.UTF_16LE), AuthorizationMethod.PASSWORD);

        char[] chars = keychain.getPassword("app"); // On Windows, ask the password
        String password = new String(chars);
        log.info("Password: {}", password);
    }
}