package example.plovdev.keyer;

import org.plovdev.keyer.Keychain;

public class ReadmeExample {
    static void main() {
        // Or now, you can use classes to get keychain instance:
        Keychain keychain = Keychain.getKeychain(ReadmeExample.class); // It's cool, isn't it?

        keychain.setPassword("alias", "123".toCharArray()); // Set password
        char[] password = keychain.getPassword("alias"); // Get password
        System.out.println(new String(password));

        // Now, you can use 'raw passwords':
        keychain.setPassword("alias-raw", "123".getBytes()); // Set raw password
        byte[] rawPassword = keychain.getRawPassword("alias-raw"); // Get raw password
        System.out.println(new String(rawPassword));
    }
}