package example.plovdev.keyer;

import org.plovdev.keyer.Keychain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A basic example demonstrating how to use the Keyer library.
 * <p>
 * This example shows the standard lifecycle of password management:
 * <ol>
 *     <li>Obtaining a platform-specific {@link Keychain} instance</li>
 *     <li>Storing a secret password using an alias</li>
 *     <li>Retrieving the stored password back from the native store</li>
 * </ol>
 *
 * <p>Note: This example uses the simplified {@code void main()} entry point
 * available in Java 25 versions.</p>
 *
 * @author Anton
 * @version 1.6
 * @since 1.0
 */
public class KeyerExample {
    private static final Logger log = LoggerFactory.getLogger(KeyerExample.class);

    /**
     * Executes the keychain demonstration.
     * <p>
     * <b>Warning:</b> In a real production environment, you should clear the
     * password character array using {@code Arrays.fill(password, '\0')}
     * after use to ensure maximum security.
     */
    static void main() {
        // 1. Get the keychain instance for your application
        Keychain keychain = Keychain.getKeychain(KeyerExample.class);
        String alias = "wallet6";

        // 2. Set a new password
        keychain.setPassword(alias, "123".getBytes(StandardCharsets.UTF_8));

        // 3. Retrieve the password
        byte[] password = keychain.getRawPassword(alias);

        // 4. Log the result
        log.info("Getted password: {}", new String(Objects.requireNonNull(password)));
    }
}