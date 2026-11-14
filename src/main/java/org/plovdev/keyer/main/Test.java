package org.plovdev.keyer.main;

import org.plovdev.keyer.implementations.win.WindowsPasswordChecker;
import org.plovdev.keyer.implementations.win.WindowsPasswordPromptUI;

public class Test {
    static void main() {
        WindowsPasswordPromptUI.showInputPasswordDialog().ifPresent(WindowsPasswordChecker::checkPassword);
    }
}