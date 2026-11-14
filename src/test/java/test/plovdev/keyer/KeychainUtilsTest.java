package test.plovdev.keyer;

import org.junit.jupiter.api.Test;
import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.utils.KeychainUtils;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KeychainUtilsTest {

    @Test
    void supported_passes() {
        assertDoesNotThrow(() -> KeychainUtils.checkAuthorizationMethod(Set.of(AuthorizationMethod.NONE, AuthorizationMethod.PASSWORD), AuthorizationMethod.NONE));
    }

    @Test
    void unsupported_throwsIAE() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> KeychainUtils.checkAuthorizationMethod(Set.of(AuthorizationMethod.NONE), AuthorizationMethod.BIOMETRY));
        assertTrue(ex.getMessage().contains("BIOMETRY"));
    }

    @Test
    void emptySet_throwsIAE() {
        assertThrows(IllegalArgumentException.class, () -> KeychainUtils.checkAuthorizationMethod(Set.of(), AuthorizationMethod.NONE));
    }

    @Test
    void nullMethod() {
        Exception ex = assertThrows(Exception.class, () -> KeychainUtils.checkAuthorizationMethod(Set.of(AuthorizationMethod.NONE), null));
        assertTrue(ex instanceof IllegalArgumentException || ex instanceof NullPointerException);
    }

    @Test
    void allEnumValues_supported() {
        assertDoesNotThrow(() -> KeychainUtils.checkAuthorizationMethod(EnumSet.allOf(AuthorizationMethod.class), AuthorizationMethod.BIOMETRY));
    }
}