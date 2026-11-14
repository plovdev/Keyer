package org.plovdev.keyer.implementations.win;

import org.plovdev.keyer.exceptions.KeyerException;
import org.plovdev.keyer.exceptions.VerificationFailedException;
import org.plovdev.keyer.exceptions.VerificationPasswordNotSpecified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.lang.foreign.ValueLayout.*;
import static org.plovdev.keyer.implementations.win.WindowsNativeUtils.*;
import static org.plovdev.keyer.utils.NativeUtils.findFunction;

public final class WindowsPasswordChecker {
    private static final int LOGON_NETWORK = 3;
    private static final int LOGON_PROVIDER = 0;

    private static final MethodHandle LOGON_USER = findFunction(ADVAPI32, LINKER, "LogonUserW", FunctionDescriptor.of(
            JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, ADDRESS
    ), GET_LAST_ERROR_OPTION);
    private static final MethodHandle CLOSE_HANDLE = findFunction(KERNEL32, LINKER, "CloseHandle", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final Logger log = LoggerFactory.getLogger(WindowsPasswordChecker.class);

    private WindowsPasswordChecker() {
    }

    public static boolean isPasswordInstalled() {
        try {
            return !checkPassword(new char[0]);
        } catch (VerificationPasswordNotSpecified e) {
            return false;
        } catch (VerificationFailedException e) {
            return true;
        }
    }

    public static boolean checkPassword(char[] passwordChars) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment userNameSegment = arena.allocateFrom(System.getProperty("user.name"), StandardCharsets.UTF_16LE);
            MemorySegment domainSegment = arena.allocateFrom(".", StandardCharsets.UTF_16LE);
            int passwordLength = passwordChars.length + 1;
            MemorySegment passwordSegment = arena.allocate(JAVA_CHAR, passwordLength);
            for (int i = 0; i < passwordChars.length; i++) {
                passwordSegment.setAtIndex(JAVA_CHAR, i, passwordChars[i]);
            }
            passwordSegment.setAtIndex(JAVA_CHAR, passwordLength - 1, '\u0000');

            MemorySegment phTokenSegment = arena.allocate(ADDRESS);
            try {
                MemorySegment lastError = arena.allocate(CAPTURED_STATE_LAYOUT);
                int verifyResult = (int) LOGON_USER.invokeExact(lastError, userNameSegment, domainSegment, passwordSegment, LOGON_NETWORK, LOGON_PROVIDER, phTokenSegment);
                if (verifyResult == 0) {
                    throwException(getLastError(lastError));
                    return false; // if not throws
                }

                return true;
            } finally {
                passwordSegment.fill((byte) 0);
                MemorySegment phToken = phTokenSegment.get(ADDRESS, 0);
                if (!phToken.equals(MemorySegment.NULL)) {
                    int closeResult = (int) CLOSE_HANDLE.invokeExact(phToken);
                    log.debug("Close token handle result: {}", closeResult);
                }
            }
        } catch (KeyerException | IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Throwable e) {
            throw new KeyerException("Unable to verify password", e);
        } finally {
            Arrays.fill(passwordChars, '\u0000');
        }
    }
}