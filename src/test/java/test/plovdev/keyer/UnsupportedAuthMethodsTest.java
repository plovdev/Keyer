package test.plovdev.keyer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.Keychain;
import org.plovdev.keyer.exceptions.UnsupportedAuthorizationMethodException;

import java.nio.charset.StandardCharsets;

public class UnsupportedAuthMethodsTest {
    private static final String APP_TOKEN = "APPLICATION";
    private static final String alias = "key-1";

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testUnsupportedAuthMethodsWindows() {
        Keychain keychain = Keychain.getKeychain(APP_TOKEN);
        Assertions.assertThrows(UnsupportedAuthorizationMethodException.class, () -> keychain.setPassword(alias, "123".getBytes(StandardCharsets.UTF_16LE), AuthorizationMethod.BIOMETRY));
    }

    @ParameterizedTest
    @EnabledOnOs({OS.AIX, OS.FREEBSD, OS.OPENBSD, OS.SOLARIS, OS.LINUX})
    @EnumSource(value = AuthorizationMethod.class, names = {"PASSWORD", "BIOMETRY"})
    void testUnsupportedAuthMethodsUnix(AuthorizationMethod method) {
        Keychain keychain = Keychain.getKeychain(APP_TOKEN);
        Assertions.assertThrows(UnsupportedAuthorizationMethodException.class, () -> keychain.setPassword(alias, "123".getBytes(StandardCharsets.UTF_16LE), method));
    }
}