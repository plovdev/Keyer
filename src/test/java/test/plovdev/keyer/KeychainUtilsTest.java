package test.plovdev.keyer;

import org.junit.jupiter.api.Test;
import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.exceptions.UnsupportedAuthorizationMethodException;
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
    void emptySet_throwsIAE() {
        assertThrows(UnsupportedAuthorizationMethodException.class, () -> KeychainUtils.checkAuthorizationMethod(Set.of(), AuthorizationMethod.NONE));
    }

    @Test
    void nullMethod() {
        Exception ex = assertThrows(Exception.class, () -> KeychainUtils.checkAuthorizationMethod(Set.of(AuthorizationMethod.NONE), null));
        assertInstanceOf(NullPointerException.class, ex);
    }

    @Test
    void allEnumValues_supported() {
        assertDoesNotThrow(() -> KeychainUtils.checkAuthorizationMethod(EnumSet.allOf(AuthorizationMethod.class), AuthorizationMethod.BIOMETRY));
    }
}