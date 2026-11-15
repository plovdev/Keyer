package example.plovdev.keyer;

import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.Keychain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class KeyerAuthMethodExample {
    private static final Logger log = LoggerFactory.getLogger(KeyerAuthMethodExample.class);

    static void main() {
        Keychain keychain = Keychain.getKeychain(KeyerAuthMethodExample.class);
        keychain.setPassword("app", "пароль\uD83D\uDD11".getBytes(StandardCharsets.UTF_16LE), AuthorizationMethod.PASSWORD);

        char[] chars = keychain.getPassword("app"); // On Windows, ask the password
        String password = new String(chars);
        log.info("Password: {}", password);
    }
}