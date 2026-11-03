package org.plovdev.keyer.implementations.unix;

import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.Keychain;
import org.plovdev.keyer.utils.KeychainUtils;

import java.util.Set;

/**
 * Unix implementation of the {@link Keychain} interface.
 * <p>
 * Provides access to the system keychain via libsecret.
 *
 * @author Anton
 * @version 1.8-beta
 * @since 1.5
 */
public class UnixKeychain implements Keychain {
    /**
     * Supported authentication methods on unix implementation
     */
    private static final Set<AuthorizationMethod> SUPPORTED_METHODS = Set.of(AuthorizationMethod.NONE);
    private final String appId;

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
     */
    @Override
    public char[] getPassword(String alias) {
        return UnixOsKeychainNative.getPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawPassword(String alias) {
        return UnixOsKeychainNative.getRawPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, char[] newPassword, AuthorizationMethod method) {
        KeychainUtils.checkAuthorizationMethod(supportedAuthMethods(), method);
        UnixOsKeychainNative.setPassword(appId, alias, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, byte[] newPassword, AuthorizationMethod method) {
        KeychainUtils.checkAuthorizationMethod(supportedAuthMethods(), method);
        UnixOsKeychainNative.setPassword(appId, alias, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePassword(String alias) {
        UnixOsKeychainNative.deletePassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AuthorizationMethod> supportedAuthMethods() {
        return SUPPORTED_METHODS;
    }
}