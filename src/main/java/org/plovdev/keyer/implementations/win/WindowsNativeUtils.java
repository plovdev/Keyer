package org.plovdev.keyer.implementations.win;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.exceptions.AccessDeniedException;
import org.plovdev.keyer.exceptions.AuthenticationCanceledException;
import org.plovdev.keyer.exceptions.KeyerException;
import org.plovdev.keyer.exceptions.KeyerStatusCode;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

import static org.plovdev.keyer.utils.NativeUtils.findFunction;
import static org.plovdev.keyer.utils.NativeUtils.findVarHandle;

public final class WindowsNativeUtils {
    static final Arena SHARED = Arena.ofAuto();
    static final Linker LINKER = Linker.nativeLinker();
    static final SymbolLookup ADVAPI32 = SymbolLookup.libraryLookup("Advapi32", SHARED);
    static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("Kernel32", SHARED);
    static final SymbolLookup OLE32 = SymbolLookup.libraryLookup("Ole32", SHARED);

    static final MethodHandle GET_LAST_ERROR = findFunction(KERNEL32, LINKER, "GetLastError", FunctionDescriptor.of(ValueLayout.JAVA_INT));
    static final MethodHandle CO_TASK_MEM_FREE = findFunction(OLE32, LINKER, "CoTaskMemFree", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    static final Linker.Option GET_LAST_ERROR_OPTION = Linker.Option.captureCallState("GetLastError");
    static final StructLayout CAPTURED_STATE_LAYOUT = Linker.Option.captureStateLayout();
    static final VarHandle CAPTURED_LAST_ERRROR = findVarHandle(CAPTURED_STATE_LAYOUT, "GetLastError");

    //====ERROR CODES====\\
    static final int NOT_FOUND = 1168;
    static final int ACCESS_DENIED = 5;
    static final int INVALID_PARAMETER = 87;
    static final int ALREADY_EXISTS = 1310;
    static final int CALL_NOT_IMPLEMENTED = 120;

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

    static void freeMemory(MemorySegment pointer) {
        try {
            CO_TASK_MEM_FREE.invokeExact(pointer);
        } catch (Throwable e) {
            throw new KeyerException("Unable to free memory: ", e);
        }
    }

    /**
     * Retrieves last error code from Windows API.
     *
     * @return last error code
     * @throws Throwable if native call fails
     */
    //TODO: remove this
    @Deprecated(forRemoval = true)
    static int getLastErrorLegacy() throws Throwable {
        return (int) GET_LAST_ERROR.invokeExact();
    }

    /**
     * Retrieves last error code from Windows API.
     *
     * @return last error code
     */
    static int getLastError(MemorySegment lastError) {
        return (int) CAPTURED_LAST_ERRROR.get(lastError, 0L);
    }

    /**
     * Throws exception from status code
     *
     * @param status operation status code
     */
    static void throwException(int status) {
        switch (status) {
            // not thorw, skip
            case 0, NOT_FOUND, ALREADY_EXISTS:
                break;
            case ACCESS_DENIED:
                throw new AccessDeniedException(KeyerStatusCode.ACCESS_DENIED);
            case INVALID_PARAMETER:
                throw new IllegalArgumentException();
            case CALL_NOT_IMPLEMENTED:
                throw new UnsupportedOperationException("Call not implemented");
            default:
                throw new KeyerException(String.format("Failed to get password (Code: %d)", status));
        }
    }
}