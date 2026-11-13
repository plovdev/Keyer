package org.plovdev.keyer.implementations.win;

import org.jspecify.annotations.NonNull;

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