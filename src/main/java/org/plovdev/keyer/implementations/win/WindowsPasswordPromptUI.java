package org.plovdev.keyer.implementations.win;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.plovdev.keyer.implementations.win.WindowsNativeUtils.*;
import static org.plovdev.keyer.utils.NativeUtils.findFunction;
import static org.plovdev.keyer.utils.NativeUtils.findVarHandle;

public final class WindowsPasswordPromptUI {
    private static final Logger log = LoggerFactory.getLogger(WindowsPasswordPromptUI.class);

    private static final int CRED_PACK_GENERIC_CREDENTIALS = 0x4;
    private static final int CREDUIWIN_GENERIC = 0x1;
    private static final int CREDUIWIN_IN_CRED_ONLY = 0x20;
    private static final int CREDUIWIN_ENUMERATE_CURRENT_USER = 0x200;
    private static final int CREDUIWIN_SECURE_PROMPT = 0x1000; // for future

    private static final SymbolLookup CRED_UI = SymbolLookup.libraryLookup("Credui", SHARED);

    private static final MethodHandle SHOW_INPUT_DIALOG = findFunction(CRED_UI, LINKER, "CredUIPromptForWindowsCredentialsW", FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            ADDRESS,
            JAVA_INT
    ));
    private static final MethodHandle PACK_AUTH_BUFFER = findFunction(CRED_UI, LINKER, "CredPackAuthenticationBufferW", FunctionDescriptor.of(
            JAVA_INT, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS
    ));

    private static final StructLayout CREDUI_INFO_STRUCT = MemoryLayout.structLayout(
            JAVA_INT.withName("cbSize"),
            MemoryLayout.paddingLayout(4),
            ADDRESS.withName("hwndParent"),
            ADDRESS.withName("pszMessageText"),
            ADDRESS.withName("pszCaptionText"),
            ADDRESS.withName("hbmBanner")
    );

    private static final MemorySegment CREDUI_INFO_SEGMENT = SHARED.allocate(CREDUI_INFO_STRUCT);

    private static final VarHandle CB_SIZE = findVarHandle(CREDUI_INFO_STRUCT, "cbSize");
    private static final VarHandle HWND_PARENT = findVarHandle(CREDUI_INFO_STRUCT, "hwndParent");
    private static final VarHandle PSZ_MESSAGE = findVarHandle(CREDUI_INFO_STRUCT, "pszMessageText");
    private static final VarHandle PSZ_CAPTION = findVarHandle(CREDUI_INFO_STRUCT, "pszCaptionText");
    private static final VarHandle HBM_BANNER = findVarHandle(CREDUI_INFO_STRUCT, "hbmBanner");

    static {
        CB_SIZE.set(CREDUI_INFO_SEGMENT, 0, (int) CREDUI_INFO_SEGMENT.byteSize());
        HWND_PARENT.set(CREDUI_INFO_SEGMENT, 0, MemorySegment.NULL);
        PSZ_MESSAGE.set(CREDUI_INFO_SEGMENT, 0, SHARED.allocateFrom("Enter password:", StandardCharsets.UTF_16LE));
        PSZ_CAPTION.set(CREDUI_INFO_SEGMENT, 0, SHARED.allocateFrom("Keyer", StandardCharsets.UTF_16LE));
        HBM_BANNER.set(CREDUI_INFO_SEGMENT, 0, MemorySegment.NULL);
    }

    private WindowsPasswordPromptUI() {
    }

    private static MemorySegment prepareAuthBuffer(@NonNull Arena arena) throws Throwable {
        MemorySegment userNameSegment = arena.allocateFrom(System.getProperty("user.name"), StandardCharsets.UTF_16LE);
        MemorySegment passwordSegment = arena.allocateFrom("", StandardCharsets.UTF_16LE);

        MemorySegment requiredSizeSegment = arena.allocate(JAVA_INT);
        int result1 = (int) PACK_AUTH_BUFFER.invokeExact(CRED_PACK_GENERIC_CREDENTIALS, userNameSegment, passwordSegment, MemorySegment.NULL, requiredSizeSegment);

        int requiredSize = requiredSizeSegment.get(JAVA_INT, 0);
        if (requiredSize <= 0) {
            throw new IllegalStateException("Failed to calculate auth buffer size");
        }

        MemorySegment authBuffer = arena.allocate(requiredSize);
        int result2 = (int) PACK_AUTH_BUFFER.invokeExact(CRED_PACK_GENERIC_CREDENTIALS, userNameSegment, passwordSegment, authBuffer, requiredSizeSegment);
        if (result2 == 0) {
            throw new IllegalStateException("CredPackAuthenticationBufferW failed");
        }

        return authBuffer;
    }

    public static int showInputPasswordDialog() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment authPackage = arena.allocate(JAVA_INT);
            MemorySegment outSegment = arena.allocate(ADDRESS);
            MemorySegment sizeSegment = arena.allocate(JAVA_INT);

            MemorySegment inAuthBuffer = prepareAuthBuffer(arena);
            int inSize = (int) inAuthBuffer.byteSize();

            int flags = CREDUIWIN_GENERIC | CREDUIWIN_IN_CRED_ONLY;
            int error = (int) SHOW_INPUT_DIALOG.invokeExact(CREDUI_INFO_SEGMENT, 0, authPackage, inAuthBuffer, inSize, outSegment, sizeSegment, MemorySegment.NULL, flags);

            checkErrorThrows(error);
            return error;
        } catch (Throwable e) {
            log.error("Error show dialog: ", e);
            return -1;
        }
    }
}