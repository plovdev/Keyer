package org.plovdev.keyer.implementations.win;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.exceptions.AuthenticationCanceledException;
import org.plovdev.keyer.exceptions.KeyerException;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import static org.plovdev.keyer.utils.NativeUtils.findFunction;

public final class WindowsNativeUtils {
    static final Arena SHARED = Arena.ofAuto();
    static final Linker LINKER = Linker.nativeLinker();
    static final SymbolLookup ADVAPI32 = SymbolLookup.libraryLookup("Advapi32", SHARED);
    static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("Kernel32", SHARED);
    private static final MethodHandle GET_LAST_ERROR = findFunction(KERNEL32, LINKER, "GetLastError", FunctionDescriptor.of(ValueLayout.JAVA_INT));

    private WindowsNativeUtils() {
        throw new UnsupportedOperationException();
    }

    static MemorySegment allocateTargetName(@NonNull Arena arena, String appId, String alias) {
        return arena.allocateFrom(appId + ":" + alias, StandardCharsets.UTF_16LE);
    }

    static void checkErrorThrows(int error) {
        if (error != 0) {
            switch (error) {
                case 87 -> throw new IllegalArgumentException("Illegal flags in function");
                case 1223 -> throw new AuthenticationCanceledException("User has cancelled the authentication");
                default -> throw new KeyerException("Unknown error code: " + error);
            }
        }
    }

    static boolean checkError(int error) {
        return error == 0;
    }

    /**
     * Retrieves last error code from Windows API.
     *
     * @return last error code
     * @throws Throwable if native call fails
     */
    static int getLastError() throws Throwable {
        return (int) GET_LAST_ERROR.invokeExact();
    }
}