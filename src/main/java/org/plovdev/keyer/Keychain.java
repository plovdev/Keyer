package org.plovdev.keyer;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.implementations.mac.MacKeychain;
import org.plovdev.keyer.implementations.unix.UnixKeychain;
import org.plovdev.keyer.implementations.win.WindowsKeychain;

import java.util.Set;

/**
 * A unified interface for accessing native system keychains.
 * <p>
 * This library provides a secure way to store, retrieve, and delete passwords
 * using platform-specific backends:
 * <ul>
 *     <li><b>macOS:</b> Apple Keychain</li>
 *     <li><b>Windows:</b> Windows Credential Manager</li>
 *     <li><b>Unix:</b> Secret Service API</li>
 * </ul>
 * <p>
 * It uses the Project Panama for high-performance
 * native calls without external dependencies.
 *
 * @author Anton
 * @version 1.7
 * @since 1.0
 */
public interface Keychain {
    /**
     * Factory method to obtain the appropriate Keychain instance for the current platform.
     * <p>
     * The returned instance is automatically initialized with the provided {@code cls}.
     *
     * @param cls class which use this Keychain service.
     * @return a thread-safe {@link Keychain} instance for the detected OS.
     * @throws IllegalArgumentException if the current platform is not supported.
     */
    static @NonNull Keychain getKeychain(@NonNull Class<?> cls) {
        return getKeychain(cls.getName());
    }

    /**
     * Factory method to obtain the appropriate Keychain instance for the current platform.
     * <p>
     * The returned instance is automatically initialized with the provided {@code appId}.
     *
     * @param appId a unique identifier for the application (e.g., "com.myapp.service").
     *              Used as the 'Service' attribute in native stores.
     * @return a thread-safe {@link Keychain} instance for the detected OS.
     * @throws IllegalArgumentException if the current platform is not supported.
     */
    static @NonNull Keychain getKeychain(String appId) {
        Platform platform = Platform.guessPlatform();
        return switch (platform) {
            case WINDOWS -> new WindowsKeychain(appId);
            case MAC -> new MacKeychain(appId);
            case UNIX -> new UnixKeychain(appId);
            default -> throw new IllegalArgumentException("Platform " + platform.name() + " not supported");
        };
    }

    /**
     * Retrieves a password from the native store.
     * <p>
     * Returns the secret as a {@code char[]} to allow for manual clearing
     * from memory after use, enhancing security compared to {@link String}.
     *
     * @param alias the unique name or account identifier associated with the password.
     * @return a {@code char[]} containing the password, or {@code null} if the
     * alias was not found or an error occurred.
     */
    char[] getPassword(String alias);

    byte[] getRawPassword(String alias);

    /**
     * Saves or updates a password in the native store.
     * <p>
     * If an entry with the same alias already exists, it will be overwritten.
     *
     * @param alias       the name or account identifier to associate the password with.
     * @param newPassword the password to be stored.
     */
    void setPassword(String alias, char[] newPassword);

    void setPasswordRaw(String alias, byte[] password);

    /**
     * Deletes a password from the native store.
     * <p>
     * If the password does not exist, the operation is considered successful.
     *
     * @param alias the name or account identifier of the password to remove.
     */
    void deletePassword(String alias);

    /**
     * Sets the authorization method to be used for subsequent password saving operations.
     * <p>
     * The selected method will be applied during calls to {@link #setPassword(String, char[])}.
     * Changing this method does not affect already existing entries in the keychain.
     *
     * @param method the desired {@link AuthorizationMethod} to be used
     */
    void setAuthorizationMethod(AuthorizationMethod method);

    /**
     * Returns the authorization method currently active in this keychain instance.
     *
     * @return the current {@link AuthorizationMethod}
     */
    AuthorizationMethod currentAuthorizationMethod();

    /**
     * Returns a set of authorization methods supported by the current platform.
     * <p>
     * The result depends on the operating system capabilities and available hardware.
     * </p>
     *
     * @return an unmodifiable set of supported {@link AuthorizationMethod}s
     */
    Set<AuthorizationMethod> supportedAuthMethods();
}