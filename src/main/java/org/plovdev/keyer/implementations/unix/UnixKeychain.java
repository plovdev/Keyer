package org.plovdev.keyer.implementations.unix;

import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.Keychain;

/**
 * Unix implementation of the {@link Keychain} interface.
 * <p>
 * Provides access to the system keychain via libsecret.
 *
 * @author Anton
 * @version 1.6
 * @since 1.5
 */
public class UnixKeychain implements Keychain {
    /**
     * Native bridge for Project Panama calls.
     */
    private static final UnixOsKeychainNative UNIX_OS_KEYCHAIN_NATIVE = new UnixOsKeychainNative();

    private final String appId;
    private volatile AuthorizationMethod authorizationMethod = AuthorizationMethod.NONE;

    /**
     * Constructs a UnixKeychain instance.
     *
     * @param appId application identifier used as attribute in secret service
     */
    public UnixKeychain(String appId) {
        this.appId = appId;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns {@code null} if the password is not found or an error occurs.
     */
    @Override
    public char[] getPassword(String alias) {
        try {
            return UNIX_OS_KEYCHAIN_NATIVE.getPassword(appId, alias);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException if the secret service operation fails
     */
    @Override
    public void setPassword(String alias, char[] newPassword) {
        UNIX_OS_KEYCHAIN_NATIVE.setPassword(appId, alias, newPassword);
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException if the secret service operation fails
     */
    @Override
    public void deletePassword(String alias) {
        UNIX_OS_KEYCHAIN_NATIVE.deletePassword(appId, alias);
    }

    @Override
    public synchronized void setAuthorizationMethod(AuthorizationMethod method) {
        this.authorizationMethod = method;
    }

    @Override
    public AuthorizationMethod getAuthorizationMethod() {
        return authorizationMethod;
    }
}