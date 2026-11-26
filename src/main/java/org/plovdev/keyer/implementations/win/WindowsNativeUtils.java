package org.plovdev.keyer.implementations.win;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.exceptions.*;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

import static org.plovdev.keyer.utils.NativeUtils.*;

/**
 * Low-level native utilities for Windows Credential Manager access.
 * <p>
 * Provides function and constant lookup for Advapi32, Kernel32 and Ole32,
 * as well as error code mapping to Keyer exceptions.
 * <p>
 * This class cannot be instantiated.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.8-beta
 */
public final class WindowsNativeUtils {
    static final Arena SHARED = Arena.ofAuto();
    static final SymbolLookup ADVAPI32 = SymbolLookup.libraryLookup("Advapi32", SHARED);
    static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("Kernel32", SHARED);
    static final SymbolLookup OLE32 = SymbolLookup.libraryLookup("Ole32", SHARED);

    static final MethodHandle GET_LAST_ERROR = findFunction(KERNEL32, LINKER, "GetLastError", FunctionDescriptor.of(ValueLayout.JAVA_INT));
    static final MethodHandle CO_TASK_MEM_FREE = findFunction(OLE32, LINKER, "CoTaskMemFree", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    static final Linker.Option GET_LAST_ERROR_OPTION = Linker.Option.captureCallState("GetLastError");
    static final StructLayout CAPTURED_STATE_LAYOUT = Linker.Option.captureStateLayout();
    static final VarHandle CAPTURED_LAST_ERROR = findVarHandle(CAPTURED_STATE_LAYOUT, "GetLastError");

    //====ERROR CODES====\\
    static final int NOT_FOUND = 1168;
    static final int ACCESS_DENIED = 5;
    static final int INVALID_PARAMETER = 87;
    static final int ALREADY_EXISTS = 1310;
    static final int CALL_NOT_IMPLEMENTED = 120;
    static final int LOGON_FAILURE = 1326;
    static final int ACCOUNT_RESTRICTION = 1327;

    /**
     * Private constructor to prevent instantiation.
     */
    private WindowsNativeUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Builds a credential target name in the form {@code appId:alias}.
     *
     * @param arena the arena used to allocate native memory
     * @param appId the application identifier
     * @param alias the account name
     * @return the encoded target name as a {@link MemorySegment}
     */
    static MemorySegment allocateTargetName(@NonNull Arena arena, String appId, String alias) {
        return arena.allocateFrom(appId + ":" + alias, StandardCharsets.UTF_16LE);
    }

    /**
     * Validates a Windows error code and throws the corresponding exception.
     *
     * @param error the Windows error code to check
     * @throws IllegalArgumentException        if the error indicates illegal flags
     * @throws AuthenticationCanceledException if the user canceled authentication
     * @throws KeyerException                  for any other non-zero error code
     */
    static void checkErrorThrows(int error) {
        if (error != 0) {
            switch (error) {
                case 87 -> throw new IllegalArgumentException("Illegal flags in function");
                case 1223 -> throw new AuthenticationCanceledException("User has cancelled the authentication");
                default -> throw new KeyerException("Unknown error code: " + error);
            }
        }
    }

    /**
     * Frees native memory allocated by the Windows API.
     *
     * @param pointer the pointer to free
     * @throws KeyerException if the memory cannot be freed
     */
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
     * @param lastError the captured call state segment
     * @return last error code
     */
    static int getLastError(MemorySegment lastError) {
        return (int) CAPTURED_LAST_ERROR.get(lastError, 0L);
    }

    /**
     * Throws the appropriate exception for the given Windows error code.
     * <p>
     * Codes {@code 0}, {@code NOT_FOUND} and {@code ALREADY_EXISTS} are treated
     * as successful and do not throw.
     *
     * @param status operation status code
     * @throws AccessDeniedException                     if access is denied
     * @throws IllegalArgumentException                  if the parameter is invalid
     * @throws UnsupportedOperationException             if the call is not implemented
     * @throws VerificationFailedException               if verification fails
     * @throws VerificationPasswordNotSpecifiedException if no verification password is configured
     * @throws KeyerException                            for any other unknown error code
     */
    static void throwException(int status) {
        switch (status) {
            // not throw, skip
            case 0, NOT_FOUND, ALREADY_EXISTS:
                break;
            case ACCESS_DENIED:
                throw new AccessDeniedException();
            case INVALID_PARAMETER:
                throw new IllegalArgumentException();
            case CALL_NOT_IMPLEMENTED:
                throw new UnsupportedOperationException("Call not implemented");
            case LOGON_FAILURE:
                throw new VerificationFailedException("Verification password is incorrect");
            case ACCOUNT_RESTRICTION:
                throw new VerificationPasswordNotSpecifiedException();
            default:
                throw new KeyerException(String.format("Failed to get password (Code: %d)", status));
        }
    }
}