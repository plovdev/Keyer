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
        assertEquals(m.ordinal(), m.getValue());
    }

    @Test
    void values() {
        assertEquals(0, AuthorizationMethod.NONE.getValue());
        assertEquals(1, AuthorizationMethod.PASSWORD.getValue());
        assertEquals(2, AuthorizationMethod.BIOMETRY.getValue());
    }

    @Test
    void valueOf() {
        assertEquals(AuthorizationMethod.NONE, AuthorizationMethod.valueOf("NONE"));
        assertThrows(IllegalArgumentException.class, () -> AuthorizationMethod.valueOf("none"));
    }
}