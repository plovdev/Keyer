package org.plovdev.keyer.implementations.win;

import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.Keychain;
import org.plovdev.keyer.utils.KeychainUtils;

import java.util.Set;

/**
 * Windows implementation of the {@link Keychain} interface.
 * <p>
 * Provides access to Windows Credential Manager via native calls to Advapi32.dll.
 * Stores generic credentials using the Windows Vault.
 *
 * @author Anton
 * @version 1.8
 * @since 1.0
 */
public class WindowsKeychain implements Keychain {
    /**
     * Supported authentication methods on Windows implementation
     */
    private static final Set<AuthorizationMethod> SUPPORTED_METHODS = Set.of(AuthorizationMethod.NONE, AuthorizationMethod.PASSWORD);
    private final String appId;

    /**
     * Constructs a WindowsKeychain instance.
     *
     * @param appId application identifier used as credential target prefix
     */
    public WindowsKeychain(String appId) {
        this.appId = appId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char[] getPassword(String alias) {
        return WindowsKeychainNative.getPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawPassword(String alias) {
        return WindowsKeychainNative.getRawPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, char[] newPassword, AuthorizationMethod method) {
        KeychainUtils.checkAuthorizationMethod(SUPPORTED_METHODS, method);
        WindowsKeychainNative.setPassword(appId, alias, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, byte[] newPassword, AuthorizationMethod method) {
        KeychainUtils.checkAuthorizationMethod(SUPPORTED_METHODS, method);
        WindowsKeychainNative.setPassword(appId, alias, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePassword(String alias) {
        WindowsKeychainNative.deletePassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AuthorizationMethod> supportedAuthMethods() {
        return SUPPORTED_METHODS;
    }
}