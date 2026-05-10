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
    private final AuthorizationMethod authorizationMethod = AuthorizationMethod.NONE;

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
        try {
            return UNIX_OS_KEYCHAIN_NATIVE.getPassword(appId, alias);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, char[] newPassword) {
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
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void setAuthorizationMethod(AuthorizationMethod method) {
        throw new UnsupportedOperationException("Unix-systems(and Keyer) not support any authorization methods. Only NONE(default)");
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
        return Set.of(AuthorizationMethod.NONE);
    }
}