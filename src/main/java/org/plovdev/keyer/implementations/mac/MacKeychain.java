package org.plovdev.keyer.implementations.mac;

import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.Keychain;

import java.util.Set;

/**
 * macOS implementation of the {@link Keychain} interface.
 * <p>
 * This class serves as a thread-safe wrapper around {@link MacOsKeychainNative},
 * connecting the Java API to the Apple Security Framework.
 * </p>
 *
 * @author Anton
 * @version 1.8-beta
 * @since 1.0
 */
public class MacKeychain implements Keychain {
    /**
     * Supported authentication methods on macos implementation
     */
    private static final Set<AuthorizationMethod> SUPPORTED_METHODS = Set.of(AuthorizationMethod.NONE, AuthorizationMethod.PASSWORD, AuthorizationMethod.BIOMETRY);
    private final String appId;

    public MacKeychain(String appId) {
        this.appId = appId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char[] getPassword(String alias) {
        return MacOsKeychainNative.getPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawPassword(String alias) {
        return MacOsKeychainNative.getRawPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, char[] newPassword, AuthorizationMethod method) {
        MacOsKeychainNative.setPassword(appId, alias, method, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, byte[] newPassword, AuthorizationMethod method) {
        MacOsKeychainNative.setPassword(appId, alias, method, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePassword(String alias) {
        MacOsKeychainNative.deletePassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AuthorizationMethod> supportedAuthMethods() {
        return SUPPORTED_METHODS;
    }
}