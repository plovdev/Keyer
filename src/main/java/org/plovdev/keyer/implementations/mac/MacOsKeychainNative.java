package org.plovdev.keyer.implementations.mac;

import org.jspecify.annotations.Nullable;
import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.exceptions.KeyerException;
import org.plovdev.keyer.utils.CharactersUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.plovdev.keyer.implementations.mac.MacOsNativeUtils.*;
import static org.plovdev.keyer.utils.NativeUtils.*;

/**
 * Low-level native bridge for macOS Keychain access.
 * <p>
 * This class interacts directly with {@code Security.framework} to perform
 * CRUD operations on Generic Password items.
 *
 * @author PlovDev
 * @version 1.8
 * @since 1.0
 */
public final class MacOsKeychainNative {
    private static final Logger log = LoggerFactory.getLogger(MacOsKeychainNative.class);

    private static final String ADD_PASSWORD_METHOD_NAME = "SecItemAdd";
    private static final String GET_PASSWORD_METHOD_NAME = "SecItemCopyMatching";
    private static final String UPDATE_PASSWORD_METHOD_NAME = "SecItemUpdate";
    private static final String DELETE_PASSWORD_METHOD_NAME = "SecItemDelete";

    private static final MethodHandle ADD_PASSWORD = findFunction(SECURITY, LINKER, ADD_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle UPDATE_PASSWORD = findFunction(SECURITY, LINKER, UPDATE_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle GET_PASSWORD = findFunction(SECURITY, LINKER, GET_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle DELETE_PASSWORD = findFunction(SECURITY, LINKER, DELETE_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

    private static final MethodHandle CF_DATA_CREATE = findFunction(SECURITY, LINKER, "CFDataCreate", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
    private static final MethodHandle CF_DATA_GET_LENGTH = findFunction(SECURITY, LINKER, "CFDataGetLength", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    private static final MethodHandle CF_DATA_GET_BYTE_PTR = findFunction(SECURITY, LINKER, "CFDataGetBytePtr", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle ACCESS_CONTROL_CREATE = findFunction(SECURITY, LINKER, "SecAccessControlCreateWithFlags", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    private static final MemorySegment CLASS = findConstant(SECURITY, "kSecClass");
    private static final MemorySegment CLASS_GENERIC_PASSWORD = findConstant(SECURITY, "kSecClassGenericPassword");
    private static final MemorySegment ATTR_SERVICE = findConstant(SECURITY, "kSecAttrService");
    private static final MemorySegment ATTR_ACCOUNT = findConstant(SECURITY, "kSecAttrAccount");
    private static final MemorySegment VALUE_DATA = findConstant(SECURITY, "kSecValueData");
    private static final MemorySegment RETURN_DATA = findConstant(SECURITY, "kSecReturnData");
    private static final MemorySegment BOOLEAN_TRUE = findConstant(SECURITY, "kCFBooleanTrue");

    private static final MemorySegment ACCESSIBLE = findConstant(SECURITY, "kSecAttrAccessible");
    private static final MemorySegment ACCESSIBLE_WHEN_UNLOCKED = findConstant(SECURITY, "kSecAttrAccessibleWhenUnlockedThisDeviceOnly");
    private static final long ACCESS_CONTROL_BIOMETRY_CURRENT_SET = 1L << 3;
    private static final long ACCESS_CONTROL_DEVICE_PASSCODE      = 1L << 4;

    private static final MemorySegment ACCESS_CONTROL = findConstant(SECURITY, "kSecAttrAccessControl");

    private MacOsKeychainNative() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves a password as a char array.
     * <p>
     * Converts the raw byte data from the Keychain to a char array using UTF-16LE
     * decoding. The raw byte array is zeroed immediately after conversion.
     *
     * @param app   the service name (application identifier)
     * @param alias the account name
     * @return password as char array, or {@code null} if not found
     * @throws KeyerException if a native call fails
     */
    public static char @Nullable [] getPassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        byte[] bytes = getRawPassword(app, alias);
        if (bytes == null) {
            return null;
        }

        try {
            return CharactersUtils.bytesToCharsUTF_16LE(bytes);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Retrieves a password as a byte array directly from the Keychain.
     *
     * @param app   the service name
     * @param alias the account name
     * @return password as byte array, or {@code null} if not found
     * @throws KeyerException if a native call fails unexpectedly
     */
    public static synchronized byte @Nullable [] getRawPassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            MemorySegment appStr = createCFString(arena, app);
            MemorySegment aliasStr = createCFString(arena, alias);
            MemorySegment[] keys = {CLASS, ATTR_SERVICE, ATTR_ACCOUNT, RETURN_DATA};
            MemorySegment[] vals = {CLASS_GENERIC_PASSWORD, appStr, aliasStr, BOOLEAN_TRUE};

            MemorySegment queryDict = createDict(arena, keys, vals);
            try {
                MemorySegment resultPtr = arena.allocate(ValueLayout.ADDRESS);

                int status = (int) GET_PASSWORD.invokeExact(queryDict, resultPtr);
                log.debug("Password getting status: {}", status);
                if (status == NOT_FOUND) return null;
                throwException(status);

                MemorySegment cfData = resultPtr.get(ValueLayout.ADDRESS, 0);
                try {
                    long length = (long) CF_DATA_GET_LENGTH.invokeExact(cfData);
                    MemorySegment bytePtr = (MemorySegment) CF_DATA_GET_BYTE_PTR.invokeExact(cfData);
                    return bytePtr.reinterpret(length).toArray(ValueLayout.JAVA_BYTE);
                } finally {
                    executeClear(cfData);
                }
            } finally {
                executeClear(appStr, aliasStr, queryDict);
            }
        } catch (Throwable t) {
            if (t instanceof KeyerException ke) {
                throw ke;
            } else {
                throw new KeyerException("Error to get password", t);
            }
        }
    }

    /**
     * Stores a password from a char array.
     * <p>
     * Converts the char array to bytes using UTF-16LE encoding and delegates
     * to {@link #setPassword(String, String, AuthorizationMethod, byte[])}.
     *
     * @param appId       the service name.
     * @param alias       the account name.
     * @param method      the authorization method to use.
     * @param newPassword the password to store.
     * @throws KeyerException if the operation fails
     */
    public static void setPassword(String appId, String alias, AuthorizationMethod method, char[] newPassword) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(method);
        Objects.requireNonNull(newPassword);

        byte[] passBytes = CharactersUtils.charsUTF_16LEToBytes(newPassword);
        try {
            setPassword(appId, alias, method, passBytes);
        } finally {
            Arrays.fill(passBytes, (byte) 0);
        }
    }

    /**
     * Stores or updates a password in the Keychain.
     * <p>
     * If an existing entry is found, it is updated. Otherwise, a new entry is created.
     * When using biometric authorization, the keychain item is secured with
     * {@code kSecAttrAccessControl} requiring biometric authentication.
     *
     * @param app         the service name.
     * @param alias       the account name.
     * @param method      the authorization method, see {@link AuthorizationMethod}.
     * @param newPassword the password as byte array.
     * @throws KeyerException if the operation fails.
     */
    public static synchronized void setPassword(String app, String alias, AuthorizationMethod method, byte[] newPassword) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(method);
        Objects.requireNonNull(newPassword);

        try (var arena = Arena.ofConfined()) {
            MemorySegment appStr = createCFString(arena, app);
            MemorySegment aliasStr = createCFString(arena, alias);
            MemorySegment passBlob = (newPassword.length > 0) ? arena.allocateFrom(ValueLayout.JAVA_BYTE, newPassword) : MemorySegment.NULL;
            MemorySegment passData = (MemorySegment) CF_DATA_CREATE.invokeExact(MemorySegment.NULL, passBlob, (long) newPassword.length);

            try {
                MemorySegment[] queryKeys = {CLASS, ATTR_SERVICE, ATTR_ACCOUNT};
                MemorySegment[] queryVals = {CLASS_GENERIC_PASSWORD, appStr, aliasStr};
                MemorySegment queryDict = createDict(arena, queryKeys, queryVals);

                List<MemorySegment> upKeys = new ArrayList<>();
                List<MemorySegment> upVals = new ArrayList<>();
                upKeys.add(VALUE_DATA);
                upVals.add(passData);

                switch (method) {
                    case BIOMETRY -> {
                        upKeys.add(ACCESS_CONTROL);
                        upVals.add(MacOsNativeUtils.createAccessControl(arena, ACCESS_CONTROL_CREATE, ACCESSIBLE_WHEN_UNLOCKED, ACCESS_CONTROL_BIOMETRY_CURRENT_SET));
                    }
                    case PASSWORD -> {
                        upKeys.add(ACCESS_CONTROL);
                        upVals.add(MacOsNativeUtils.createAccessControl(arena, ACCESS_CONTROL_CREATE, ACCESSIBLE_WHEN_UNLOCKED, ACCESS_CONTROL_DEVICE_PASSCODE));
                    }
                    case NONE -> {
                        upKeys.add(ACCESSIBLE);
                        upVals.add(ACCESSIBLE_WHEN_UNLOCKED);
                    }
                }

                MemorySegment updateDict = createDict(arena, upKeys.toArray(MemorySegment[]::new), upVals.toArray(MemorySegment[]::new));
                int status = (int) UPDATE_PASSWORD.invokeExact(queryDict, updateDict);

                if (status == NOT_FOUND) {
                    upKeys.add(CLASS);
                    upVals.add(CLASS_GENERIC_PASSWORD);
                    upKeys.add(ATTR_SERVICE);
                    upVals.add(appStr);
                    upKeys.add(ATTR_ACCOUNT);
                    upVals.add(aliasStr);
                    MemorySegment addDict = createDict(arena, upKeys.toArray(MemorySegment[]::new), upVals.toArray(MemorySegment[]::new));
                    status = (int) ADD_PASSWORD.invokeExact(addDict, MemorySegment.NULL);
                    executeClear(addDict);
                }

                executeClear(queryDict, updateDict);
                throwException(status);
            } finally {
                executeClear(appStr, aliasStr, passData);
                if (passBlob != MemorySegment.NULL) {
                    passBlob.fill((byte) 0);
                }
            }
        } catch (Throwable t) {
            if (t instanceof KeyerException ke) {
                throw ke;
            } else {
                throw new KeyerException("Can't set password", t);
            }
        }
    }

    /**
     * Deletes a password entry from the Keychain.
     * <p>
     * If the entry does not exist, the operation succeeds silently.
     *
     * @param app   the service name
     * @param alias the account name
     * @throws KeyerException if the item exists but cannot be deleted
     */
    public static synchronized void deletePassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            MemorySegment appStr = createCFString(arena, app);
            MemorySegment aliasStr = createCFString(arena, alias);

            MemorySegment[] keys = {CLASS, ATTR_SERVICE, ATTR_ACCOUNT};
            MemorySegment[] vals = {CLASS_GENERIC_PASSWORD, appStr, aliasStr};
            MemorySegment queryDict = createDict(arena, keys, vals);

            int status = (int) DELETE_PASSWORD.invokeExact(queryDict);
            log.debug("Password deletion status: {}", status);

            executeClear(appStr, aliasStr, queryDict);
            if (status != SUCCESS && status != NOT_FOUND) {
                throwException(status);
            }
        } catch (Throwable t) {
            if (t instanceof KeyerException ke) {
                throw ke;
            } else {
                throw new KeyerException("Error to delete password", t);
            }
        }
    }
}