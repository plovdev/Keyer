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
 * @version 1.7
 * @since 1.0
 */
public class MacKeychain implements Keychain {
    /**
     * Native bridge for Project Panama calls.
     */
    private final MacOsKeychainNative MAC_OS_KEYCHAIN_NATIVE = new MacOsKeychainNative();
    private final String appId;

    public MacKeychain(String appId) {
        this.appId = appId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char[] getPassword(String alias) {
        return MAC_OS_KEYCHAIN_NATIVE.getPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawPassword(String alias) {
        return MAC_OS_KEYCHAIN_NATIVE.getRawPassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, char[] newPassword, AuthorizationMethod method) {
        MAC_OS_KEYCHAIN_NATIVE.setPassword(appId, alias, method, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String alias, byte[] newPassword, AuthorizationMethod method) {
        MAC_OS_KEYCHAIN_NATIVE.setPassword(appId, alias, method, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePassword(String alias) {
        MAC_OS_KEYCHAIN_NATIVE.deletePassword(appId, alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AuthorizationMethod> supportedAuthMethods() {
        return MAC_OS_KEYCHAIN_NATIVE.getAvailAuthMethods();
    }
}