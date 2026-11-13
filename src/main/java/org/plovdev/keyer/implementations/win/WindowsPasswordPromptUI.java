package org.plovdev.keyer.implementations.win;

import org.plovdev.keyer.utils.NativeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;
import static org.plovdev.keyer.implementations.win.WindowsNativeUtils.LINKER;
import static org.plovdev.keyer.implementations.win.WindowsNativeUtils.SHARED;

public final class WindowsPasswordPromptUI {
    private static final Logger log = LoggerFactory.getLogger(WindowsPasswordPromptUI.class);

    private static final SymbolLookup CRED_UI = SymbolLookup.libraryLookup("Credui", SHARED);
    private static final MethodHandle SHOW_INPUT_DIALOG = NativeUtils.findFunction(CRED_UI, LINKER, "CredUIPromptForWindowsCredentialsW", FunctionDescriptor.of(
            JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_INT
    ));
    private static final int CREDUIWIN_GENERIC = 0x1;

    private WindowsPasswordPromptUI() {
    }

    public static int showInputPasswordDialog() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment outSegment = arena.allocate(ADDRESS);
            MemorySegment sizeSegment = arena.allocate(JAVA_INT.byteSize());

            int error = (int) SHOW_INPUT_DIALOG.invokeExact(MemorySegment.NULL, 0, MemorySegment.NULL, MemorySegment.NULL, 0, outSegment, sizeSegment, MemorySegment.NULL, CREDUIWIN_GENERIC);
            return error;
        } catch (Throwable e) {
            log.error("Error show dialog: ", e);
            return -1;
        }
    }
}