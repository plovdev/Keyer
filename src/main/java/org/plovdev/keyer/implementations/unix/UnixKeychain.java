package org.plovdev.keyer.implementations.unix;

import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.Keychain;

import java.util.Set;

/**
 * Unix implementation of the {@link Keychain} interface.
 * <p>
 * Provides access to the system keychain via libsecret.
 *
 * @author Anton
 * @version 1.7
 * @since 1.5
 */
public class UnixKeychain implements Keychain {
    /**
     * Native bridge for Project Panama calls.
     */
    private final UnixOsKeychainNative UNIX_OS_KEYCHAIN_NATIVE = new UnixOsKeychainNative();
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
        return UNIX_OS_KEYCHAIN_NATIVE.getPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawPassword(String alias) {
        return UNIX_OS_KEYCHAIN_NATIVE.getRawPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, char[] newPassword, AuthorizationMethod method) {
        UNIX_OS_KEYCHAIN_NATIVE.setPassword(appId, alias, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, byte[] newPassword, AuthorizationMethod method) {
        UNIX_OS_KEYCHAIN_NATIVE.setPassword(appId, alias, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePassword(String alias) {
        UNIX_OS_KEYCHAIN_NATIVE.deletePassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AuthorizationMethod> supportedAuthMethods() {
        return Set.of(AuthorizationMethod.NONE);
    }
}