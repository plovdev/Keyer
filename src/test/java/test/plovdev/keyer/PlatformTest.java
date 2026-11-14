package test.plovdev.keyer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.plovdev.keyer.Platform;

import static org.junit.jupiter.api.Assertions.*;

class PlatformTest {
    private String originalOsName;

    @BeforeEach
    void save() {
        originalOsName = System.getProperty("os.name");
    }

    @AfterEach
    void restore() {
        if (originalOsName != null) System.setProperty("os.name", originalOsName);
        else System.clearProperty("os.name");
    }

    @ParameterizedTest
    @CsvSource({
            "Windows 10,               WINDOWS",
            "Windows 11,               WINDOWS",
            "Windows Server 2019,      WINDOWS",
            "Mac OS X,                 MAC",
            "macOS,                    MAC",
            "Linux,                    UNIX",
            "AIX,                      UNIX",
            "SunOS,                    OTHER",
            "Solaris,                  OTHER"
    })
    void guessPlatform_known(String osName, Platform expected) {
        System.setProperty("os.name", osName);
        assertEquals(expected, Platform.guessPlatform(), "os.name=" + osName);
    }

    @Test
    void guessPlatform_darwin_isMac() {
        System.setProperty("os.name", "Darwin");
        assertEquals(Platform.MAC, Platform.guessPlatform());
    }

    @Test
    void guessPlatform_nullOsName() {
        System.clearProperty("os.name");
        assertThrows(NullPointerException.class, Platform::guessPlatform);
    }

    @Test
    void guessPlatform_trimAndCase() {
        System.setProperty("os.name", "  WINDOWS 10  ");
        assertEquals(Platform.WINDOWS, Platform.guessPlatform());
    }

    @Test
    void guessPlatform_noThrow() {
        assertDoesNotThrow(Platform::guessPlatform);
    }
}