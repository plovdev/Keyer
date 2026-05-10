package org.plovdev.keyer.implementations.win;

import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.Keychain;

import java.util.Set;

/**
 * Windows implementation of the {@link Keychain} interface.
 * <p>
 * Provides access to Windows Credential Manager via native calls to Advapi32.dll.
 * Stores generic credentials using the Windows Vault.
 *
 * @author Anton
 * @version 1.7
 * @since 1.0
 */
public class WindowsKeychain implements Keychain {
    /**
     * Native bridge for Project Panama calls.
     */
    private final WinOsKeychainNative WIN_OS_KEYCHAIN_NATIVE = new WinOsKeychainNative();

    private final String appId;
    private volatile AuthorizationMethod authorizationMethod = AuthorizationMethod.NONE;

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
        return WIN_OS_KEYCHAIN_NATIVE.getPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, char[] newPassword) {
        WIN_OS_KEYCHAIN_NATIVE.setPassword(appId, alias, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePassword(String alias) {
        WIN_OS_KEYCHAIN_NATIVE.deletePassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setAuthorizationMethod(AuthorizationMethod method) {
        this.authorizationMethod = method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationMethod currentAuthorizationMethod() {
        return authorizationMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AuthorizationMethod> supportedAuthMethods() {
        return Set.of();
    }
}