package org.plovdev.keyer.implementations.mac;

import org.plovdev.keyer.AuthorizationMethod;
import org.plovdev.keyer.Keychain;

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
    private static final MacOsKeychainNative MAC_OS_KEYCHAIN_NATIVE = new MacOsKeychainNative();

    private final String appId;
    private volatile AuthorizationMethod authorizationMethod = AuthorizationMethod.NONE;

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
     * <p>Overwrites the password if the alias already exists for this application.</p>
     */
    @Override
    public void setPassword(String alias, char[] newPassword) {
        MAC_OS_KEYCHAIN_NATIVE.setPassword(appId, alias, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePassword(String alias) {
        MAC_OS_KEYCHAIN_NATIVE.deletePassword(appId, alias);
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