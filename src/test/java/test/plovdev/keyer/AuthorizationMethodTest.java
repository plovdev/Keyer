package test.plovdev.keyer;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.plovdev.keyer.AuthorizationMethod;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationMethodTest {

    @ParameterizedTest
    @EnumSource(AuthorizationMethod.class)
    void valuesMatchOrdinalAndValue(@NonNull AuthorizationMethod m) {
        assertEquals(m.ordinal(), AuthorizationMethod.getMethodNumber(m));
    }

    @Test
    void values() {
        assertEquals(0, AuthorizationMethod.getMethodNumber(AuthorizationMethod.NONE));
        assertEquals(1, AuthorizationMethod.getMethodNumber(AuthorizationMethod.PASSWORD));
        assertEquals(2, AuthorizationMethod.getMethodNumber(AuthorizationMethod.BIOMETRY));
    }

    @Test
    void valueOf() {
        assertEquals(AuthorizationMethod.NONE, AuthorizationMethod.valueOf("NONE"));
        assertThrows(IllegalArgumentException.class, () -> AuthorizationMethod.valueOf("none"));
    }
}