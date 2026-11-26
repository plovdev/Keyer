package org.plovdev.keyer.implementations.win;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.exceptions.KeyerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.*;
import static org.plovdev.keyer.implementations.win.WindowsNativeUtils.*;
import static org.plovdev.keyer.utils.NativeUtils.findFunction;
import static org.plovdev.keyer.utils.NativeUtils.findVarHandle;

/**
 * Utility for showing the native Windows password input dialog.
 * <p>
 * Uses the {@code CredUIPromptForWindowsCredentialsW} function to prompt the
 * user for credentials and extracts the entered password.
 * <p>
 * This class cannot be instantiated.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.8-beta
 */
public final class WindowsPasswordPromptUI {
    private static final Logger log = LoggerFactory.getLogger(WindowsPasswordPromptUI.class);
    private static final Arena SHARED_UI_ARENA = Arena.ofShared();

    private static final int CRED_PACK_GENERIC_CREDENTIALS = 0x4;
    private static final int CREDUIWIN_GENERIC = 0x1;
    private static final int CREDUIWIN_IN_CRED_ONLY = 0x20;
    private static final int CREDUIWIN_ENUMERATE_CURRENT_USER = 0x200;
    private static final int CREDUIWIN_SECURE_PROMPT = 0x1000; // for future

    private static final SymbolLookup CRED_UI = SymbolLookup.libraryLookup("Credui", SHARED_UI_ARENA);

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
    ), GET_LAST_ERROR_OPTION);
    private static final MethodHandle UNPACK_AUTH_BUFFER = findFunction(CRED_UI, LINKER, "CredUnPackAuthenticationBufferW", FunctionDescriptor.of(
            JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS
    ), GET_LAST_ERROR_OPTION);

    private static final StructLayout CREDUI_INFO_STRUCT = MemoryLayout.structLayout(
            JAVA_INT.withName("cbSize"),
            MemoryLayout.paddingLayout(4),
            ADDRESS.withName("hwndParent"),
            ADDRESS.withName("pszMessageText"),
            ADDRESS.withName("pszCaptionText"),
            ADDRESS.withName("hbmBanner")
    );

    private static final MemorySegment CREDUI_INFO_SEGMENT = SHARED_UI_ARENA.allocate(CREDUI_INFO_STRUCT);

    private static final VarHandle CB_SIZE = findVarHandle(CREDUI_INFO_STRUCT, "cbSize");
    private static final VarHandle HWND_PARENT = findVarHandle(CREDUI_INFO_STRUCT, "hwndParent");
    private static final VarHandle PSZ_MESSAGE = findVarHandle(CREDUI_INFO_STRUCT, "pszMessageText");
    private static final VarHandle PSZ_CAPTION = findVarHandle(CREDUI_INFO_STRUCT, "pszCaptionText");
    private static final VarHandle HBM_BANNER = findVarHandle(CREDUI_INFO_STRUCT, "hbmBanner");

    /*
      Initializes the static {@code CREDUI_INFO} structure.
      <p>
      Sets the structure size, clears the parent window handle and banner,
      and fills the message and caption text with UTF-16LE encoded strings.
     */
    static {
        CB_SIZE.set(CREDUI_INFO_SEGMENT, 0, (int) CREDUI_INFO_SEGMENT.byteSize());
        HWND_PARENT.set(CREDUI_INFO_SEGMENT, 0, NULL);
        PSZ_MESSAGE.set(CREDUI_INFO_SEGMENT, 0, SHARED_UI_ARENA.allocateFrom("Enter password:", StandardCharsets.UTF_16LE));
        PSZ_CAPTION.set(CREDUI_INFO_SEGMENT, 0, SHARED_UI_ARENA.allocateFrom("Keyer", StandardCharsets.UTF_16LE));
        HBM_BANNER.set(CREDUI_INFO_SEGMENT, 0, NULL);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private WindowsPasswordPromptUI() {
        throw new UnsupportedOperationException();
    }

    /**
     * Packs the current username and an empty password into a credential
     * authentication buffer.
     * <p>
     * The buffer is first queried for the required size, then allocated and
     * filled with the packed credentials.
     *
     * @param arena the arena used to allocate native memory
     * @return the packed authentication buffer as a {@link MemorySegment}
     * @throws IllegalStateException if the required buffer size cannot be determined
     * @throws KeyerException        if the native call fails
     */
    private static MemorySegment packAuthBuffer(@NonNull Arena arena) throws Throwable {
        MemorySegment userNameSegment = arena.allocateFrom(System.getProperty("user.name"), StandardCharsets.UTF_16LE);
        MemorySegment passwordSegment = arena.allocateFrom("", StandardCharsets.UTF_16LE);

        MemorySegment requiredSizeSegment = arena.allocate(JAVA_INT);
        int result1 = (int) PACK_AUTH_BUFFER.invokeExact(/* IGNORE AT FIRST TIME */ arena.allocate(CAPTURED_STATE_LAYOUT), CRED_PACK_GENERIC_CREDENTIALS, userNameSegment, passwordSegment, NULL, requiredSizeSegment);

        int requiredSize = requiredSizeSegment.get(JAVA_INT, 0);
        if (requiredSize <= 0) {
            throw new IllegalStateException("Failed to calculate auth buffer size");
        }

        MemorySegment authBuffer = arena.allocate(requiredSize);
        MemorySegment lastError = arena.allocate(CAPTURED_STATE_LAYOUT);
        int result2 = (int) PACK_AUTH_BUFFER.invokeExact(lastError, CRED_PACK_GENERIC_CREDENTIALS, userNameSegment, passwordSegment, authBuffer, requiredSizeSegment);
        if (result2 == 0) {
            throwException(getLastError(lastError));
        }

        return authBuffer;
    }

    /**
     * Unpacks the password from a credential authentication buffer.
     * <p>
     * The required sizes are queried first, then the username and password are
     * extracted. The password is returned without the trailing null terminator.
     * All native buffers are cleared after use.
     *
     * @param arena          the arena used to allocate native memory
     * @param authBuffer     the authentication buffer to unpack
     * @param authBufferSize the size of the authentication buffer
     * @return the unpacked password characters
     * @throws IllegalStateException if the required sizes cannot be determined
     * @throws KeyerException        if the native call fails
     */
    private static char @NonNull [] unpackAuthBuffer(@NonNull Arena arena, MemorySegment authBuffer, int authBufferSize) throws Throwable {
        try {
            MemorySegment userNameSize = arena.allocate(JAVA_INT);
            MemorySegment passwordSize = arena.allocate(JAVA_INT);

            int result1 = (int) UNPACK_AUTH_BUFFER.invokeExact(/* IGNORE AT FIRST TIME */ arena.allocate(CAPTURED_STATE_LAYOUT), 0, authBuffer, authBufferSize, NULL, userNameSize, NULL, NULL, NULL, passwordSize);

            int usernameCharsCount = userNameSize.get(JAVA_INT, 0);
            int passwordCharsCount = passwordSize.get(JAVA_INT, 0);
            if (usernameCharsCount <= 0 || passwordCharsCount <= 0) {
                throw new IllegalStateException("Failed to calculate required sizes");
            }

            MemorySegment usernameSegment = arena.allocate(JAVA_CHAR, usernameCharsCount);
            MemorySegment passwordSegment = arena.allocate(JAVA_CHAR, passwordCharsCount);
            try {
                MemorySegment lastError = arena.allocate(CAPTURED_STATE_LAYOUT);
                int result2 = (int) UNPACK_AUTH_BUFFER.invokeExact(lastError, 0, authBuffer, authBufferSize, usernameSegment, userNameSize, NULL, NULL, passwordSegment, passwordSize);
                if (result2 == 0) {
                    throwException(getLastError(lastError));
                }

                int length = passwordCharsCount - 1;
                char[] passwordChars = new char[length];
                MemorySegment.copy(passwordSegment, JAVA_CHAR, 0, passwordChars, 0, length);
                return passwordChars;
            } finally {
                usernameSegment.fill((byte) 0);
                passwordSegment.fill((byte) 0);
            }
        } finally {
            authBuffer.reinterpret(authBufferSize).fill((byte) 0);
            freeMemory(authBuffer);
        }
    }

    /**
     * Shows the native Windows password input dialog.
     * <p>
     * If no password is configured on the system, the dialog is not shown and
     * an empty {@link Optional} is returned.
     *
     * @return an {@link Optional} containing the entered password, or empty if
     * no password is configured on the system
     * @throws org.plovdev.keyer.exceptions.AuthenticationCanceledException if the user cancels the dialog
     * @throws IllegalArgumentException                                     if the entered password is empty
     * @throws KeyerException                                               if the dialog cannot be shown or a native call fails
     */
    public static @NonNull Optional<char[]> showInputPasswordDialog() {
        if (WindowsPasswordChecker.isPasswordInstalled()) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment authPackage = arena.allocate(JAVA_INT);
                MemorySegment outSegment = arena.allocate(ADDRESS);
                MemorySegment sizeSegment = arena.allocate(JAVA_INT);

                MemorySegment inAuthBuffer = packAuthBuffer(arena);
                int inSize = (int) inAuthBuffer.byteSize();

                int flags = CREDUIWIN_GENERIC | CREDUIWIN_IN_CRED_ONLY;
                int error = (int) SHOW_INPUT_DIALOG.invokeExact(CREDUI_INFO_SEGMENT, 0, authPackage, inAuthBuffer, inSize, outSegment, sizeSegment, NULL, flags);

                checkErrorThrows(error);
                char[] password = unpackAuthBuffer(arena, outSegment.get(ADDRESS, 0), sizeSegment.get(JAVA_INT, 0));
                //noinspection ConstantValue
                if (password == null || password.length == 0) {
                    throw new IllegalArgumentException("Password is empty");
                }

                return Optional.of(password);
            } catch (KeyerException | IllegalStateException | IllegalArgumentException e) {
                throw e;
            } catch (Throwable e) {
                throw new KeyerException("Unable to show input password dialog", e);
            }
        } else {
            log.debug("Password not installed, skip");
            return Optional.empty();
        }
    }
}