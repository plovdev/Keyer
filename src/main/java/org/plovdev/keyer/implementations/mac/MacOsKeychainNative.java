package org.plovdev.keyer.implementations.mac;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.exceptions.*;
import org.plovdev.keyer.utils.NativeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.*;

import static org.plovdev.keyer.utils.NativeUtils.find;
import static org.plovdev.keyer.utils.NativeUtils.getConstant;

/**
 * Low-level native bridge for macOS Keychain access.
 * <p>
 * This class interacts directly with {@code Security.framework} to perform
 * CRUD operations on Generic Password items.
 *
 * @author Anton
 * @version 1.7
 * @since 1.6
 */
public final class MacOsKeychainNative {
    private static final Logger log = LoggerFactory.getLogger(MacOsKeychainNative.class);

    private static final String ADD_PASSWORD_METHOD_NAME = "SecItemAdd";
    private static final String GET_PASSWORD_METHOD_NAME = "SecItemCopyMatching";
    private static final String UPDATE_PASSWORD_METHOD_NAME = "SecItemUpdate";
    private static final String DELETE_PASSWORD_METHOD_NAME = "SecItemDelete";

    private static final Arena SHARED = Arena.ofAuto();
    private static final Linker LINKER = Linker.nativeLinker();

    /**
     * Path to the binary inside the Security framework.
     */
    private static final SymbolLookup SECURITY = SymbolLookup.libraryLookup("/System/Library/Frameworks/Security.framework/Versions/A/Security", SHARED);

    private static final MethodHandle ADD_PASSWORD = find(SECURITY, LINKER, ADD_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle UPDATE_PASSWORD = find(SECURITY, LINKER, UPDATE_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle GET_PASSWORD = find(SECURITY, LINKER, GET_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle DELETE_PASSWORD = find(SECURITY, LINKER, DELETE_PASSWORD_METHOD_NAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

    private static final MethodHandle CF_DICT_CREATE = find(SECURITY, LINKER, "CFDictionaryCreate", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle CF_STR_CREATE = find(SECURITY, LINKER, "CFStringCreateWithCharacters", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
    private static final MethodHandle CF_DATA_CREATE = find(SECURITY, LINKER, "CFDataCreate", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
    private static final MethodHandle CF_DATA_GET_LENGTH = find(SECURITY, LINKER, "CFDataGetLength", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    private static final MethodHandle CF_DATA_GET_BYTE_PTR = find(SECURITY, LINKER, "CFDataGetBytePtr", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle CF_RELEASE = find(SECURITY, LINKER, "CFRelease", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    private static final MethodHandle ACCESS_CONTROL_CREATE = find(SECURITY, LINKER, "SecAccessControlCreateWithFlags", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    private static final MemorySegment CLASS = getConstant(SECURITY, "kSecClass");
    private static final MemorySegment CLASS_GENERIC_PASSWORD = getConstant(SECURITY, "kSecClassGenericPassword");
    private static final MemorySegment ATTR_SERVICE = getConstant(SECURITY, "kSecAttrService");
    private static final MemorySegment ATTR_ACCOUNT = getConstant(SECURITY, "kSecAttrAccount");
    private static final MemorySegment VALUE_DATA = getConstant(SECURITY, "kSecValueData");
    private static final MemorySegment RETURN_DATA = getConstant(SECURITY, "kSecReturnData");
    private static final MemorySegment BOOLEAN_TRUE = getConstant(SECURITY, "kCFBooleanTrue");

    private static final MemorySegment ACCESSIBLE = getConstant(SECURITY, "kSecAttrAccessible");
    private static final MemorySegment ACCESSIBLE_ALWAYS = getConstant(SECURITY, "kSecAttrAccessibleAlways");
    private static final MemorySegment ACCESSIBLE_WHEN_UNLOCKED = getConstant(SECURITY, "kSecAttrAccessibleWhenUnlockedThisDeviceOnly");
    private static final MemorySegment ACCESS_CONTROL = getConstant(SECURITY, "kSecAttrAccessControl");

    //====ERROR CODES====\\
    private static final int SUCCESS = 0;
    private static final int NOT_FOUND = -25300;
    private static final int AUTH_CANCELED = -128;
    private static final int MISSING_ENTITLEMENT = -34018;
    private static final int ACCESS_DENIED = -25293;
    private static final int ACCESS_LOCKED = -25308;

    /**
     * Retrieves a password as a char array.
     * <p>
     * Converts the raw byte data from the Keychain to a char array using UTF-8
     * decoding. The raw byte array is zeroed immediately after conversion.
     *
     * @param app   the service name (application identifier)
     * @param alias the account name
     * @return password as char array, or {@code null} if not found
     */
    public char @Nullable [] getPassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        byte[] bytes = getRawPassword(app, alias);
        if (bytes == null) {
            return null;
        }

        char[] password = NativeUtils.bytesToCharsUTF_8(bytes);
        Arrays.fill(bytes, (byte) 0);
        return password;
    }

    /**
     * Retrieves a password as a byte array directly from the Keychain.
     *
     * @param app   the service name
     * @param alias the account name
     * @return password as byte array, or {@code null} if not found
     * @throws KeyerException if a native call fails unexpectedly
     */
    public synchronized byte @Nullable [] getRawPassword(String app, String alias) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);

        try (var arena = Arena.ofConfined()) {
            MemorySegment appStr = createCFString(arena, app);
            MemorySegment aliasStr = createCFString(arena, alias);
            MemorySegment[] keys = {CLASS, ATTR_SERVICE, ATTR_ACCOUNT, RETURN_DATA};
            MemorySegment[] vals = {CLASS_GENERIC_PASSWORD, appStr, aliasStr, BOOLEAN_TRUE};

            MemorySegment queryDict = createDict(arena, keys, vals);
            MemorySegment resultPtr = arena.allocate(ValueLayout.ADDRESS);

            int status = (int) GET_PASSWORD.invokeExact(queryDict, resultPtr);
            log.debug("Password getting status: {}", status);
            if (status == NOT_FOUND) return null;
            throwException(status);

            MemorySegment cfData = resultPtr.get(ValueLayout.ADDRESS, 0);
            long length = (long) CF_DATA_GET_LENGTH.invokeExact(cfData);
            MemorySegment bytePtr = (MemorySegment) CF_DATA_GET_BYTE_PTR.invokeExact(cfData);

            byte[] bytes = bytePtr.reinterpret(length).toArray(ValueLayout.JAVA_BYTE);
            executeClear(cfData, appStr, aliasStr, queryDict);
            return bytes;
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
     * Converts the char array to bytes using UTF-8 encoding and delegates
     * to {@link #setPassword(String, String, AuthorizationMethod, byte[])}.
     * The char array is zeroed after conversion.
     *
     * @param appId       the service name.
     * @param alias       the account name.
     * @param method      the authorization method to use.
     * @param newPassword the password to store.
     */
    public void setPassword(String appId, String alias, AuthorizationMethod method, char[] newPassword) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(method);
        Objects.requireNonNull(newPassword);

        byte[] passBytes = NativeUtils.charsUTF_8ToBytes(newPassword);
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
     * @param method      the authorization method (NONE, BIOMETRY).
     * @param newPassword the password as byte array.
     * @throws KeyerException if the operation fails.
     */
    public synchronized void setPassword(String app, String alias, AuthorizationMethod method, byte[] newPassword) {
        Objects.requireNonNull(app);
        Objects.requireNonNull(alias);
        Objects.requireNonNull(method);
        Objects.requireNonNull(newPassword);

        try (var arena = Arena.ofConfined()) {
            MemorySegment appStr = createCFString(arena, app);
            MemorySegment aliasStr = createCFString(arena, alias);
            MemorySegment passBlob = (newPassword.length > 0) ? arena.allocateFrom(ValueLayout.JAVA_BYTE, newPassword) : MemorySegment.NULL;
            MemorySegment passData = (MemorySegment) CF_DATA_CREATE.invokeExact(MemorySegment.NULL, passBlob, (long) newPassword.length);

            MemorySegment[] queryKeys = {CLASS, ATTR_SERVICE, ATTR_ACCOUNT};
            MemorySegment[] queryVals = {CLASS_GENERIC_PASSWORD, appStr, aliasStr};
            MemorySegment queryDict = createDict(arena, queryKeys, queryVals);

            List<MemorySegment> upKeys = new ArrayList<>();
            List<MemorySegment> upVals = new ArrayList<>();
            upKeys.add(VALUE_DATA);
            upVals.add(passData);

            if (method == AuthorizationMethod.BIOMETRY) {
                upKeys.add(ACCESS_CONTROL);
                upVals.add(MacAuthHelper.createAccessControl(arena, ACCESS_CONTROL_CREATE, ACCESSIBLE_WHEN_UNLOCKED));
            } else {
                upKeys.add(ACCESSIBLE);
                upVals.add((method == AuthorizationMethod.NONE) ? ACCESSIBLE_ALWAYS : ACCESSIBLE_WHEN_UNLOCKED);
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
                CF_RELEASE.invokeExact(addDict);
            }

            executeClear(appStr, aliasStr, passData, queryDict, updateDict);
            throwException(status);
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
    public synchronized void deletePassword(String app, String alias) {
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

    /**
     * Returns the set of authorization methods available on this platform.
     *
     * @return a set of supported authorization methods
     */
    public @NonNull Set<AuthorizationMethod> getAvailAuthMethods() {
        return MacAuthHelper.supportedAuthMethods(ACCESS_CONTROL_CREATE, ACCESSIBLE_WHEN_UNLOCKED);
    }

    /**
     * Throws exception from status code
     *
     * @param status operation status code
     */
    private void throwException(int status) {
        switch (status) {
            case SUCCESS, NOT_FOUND:
                break;
            case AUTH_CANCELED:
                throw new AuthenticationCanceledException(KeyerStatusCode.AUTH_CANCELED);
            case MISSING_ENTITLEMENT:
                throw new MissingEntitlement(KeyerStatusCode.MISSING_ENTITLEMENT);
            case ACCESS_DENIED:
                throw new AccessDeniedException(KeyerStatusCode.ACCESS_DENIED);
            case ACCESS_LOCKED:
                throw new AccessDeniedException(KeyerStatusCode.ACCESS_LOCKED);
            default:
                throw new KeyerException(String.format("Keyer operation failed (Code: %d)", status));
        }
    }

    private @NonNull MemorySegment createCFString(@NonNull Arena arena, @NonNull String str) throws Throwable {
        MemorySegment chars = arena.allocateFrom(ValueLayout.JAVA_CHAR, str.toCharArray());
        MemorySegment ref = (MemorySegment) CF_STR_CREATE.invokeExact(MemorySegment.NULL, chars, (long) str.length());
        if (ref.address() == 0) throw new KeyerException("Failed to create CFString");
        return ref;
    }

    private MemorySegment createDict(@NonNull Arena arena, MemorySegment @NonNull [] keys, MemorySegment @NonNull [] values) throws Throwable {
        var keysPtr = arena.allocate(ValueLayout.ADDRESS, keys.length);
        var valsPtr = arena.allocate(ValueLayout.ADDRESS, values.length);
        for (int i = 0; i < keys.length; i++) {
            keysPtr.setAtIndex(ValueLayout.ADDRESS, i, keys[i]);
            valsPtr.setAtIndex(ValueLayout.ADDRESS, i, values[i]);
        }
        return (MemorySegment) CF_DICT_CREATE.invokeExact(MemorySegment.NULL, keysPtr, valsPtr, (long) keys.length, MemorySegment.NULL, MemorySegment.NULL);
    }

    private void executeClear(MemorySegment... toClear) {
        Arrays.stream(toClear).forEach(segment -> {
            try {
                CF_RELEASE.invokeExact(segment);
            } catch (Throwable e) {
                log.error("Error clear object: ", e);
            }
        });
    }
}