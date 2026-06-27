# Keyer

**Keyer** — это современная и легковесная библиотека для работы с системными хранилищами паролей напрямую из Java.

![Java Version](https://img.shields.io/badge/Java%2025-green)
![License](https://img.shields.io/badge/Public%20Domain-blue)

## Features

- **Project Panama** — максимальная производительность нативных вызовов без тяжёлых зависимостей.
- **Security-first** — работа с секретами через `char[]` и `byte[]` для минимизации следов в памяти JVM.
- **Cross-platform** — единый интерфейс для macOS, Windows и Linux.
- **Minimum Dependencies, Maximum Value** — только JSpecify, SLF4J и JUnit.

## Доступно на Maven Central

```xml

<dependency>
    <groupId>io.github.plovdev</groupId>
    <artifactId>keyer</artifactId>
    <version>1.7.1-beta</version> <!-- Latest version -->
</dependency>
```

И Gradle:

```groovy
implementation("io.github.plovdev:keyer:1.7.1-beta")
```

## Быстрый старт

### Инициализация

Библиотека автоматически определит вашу ОС и выберет нужную реализацию:

```java
public class KeyerTest {
    static void main() {
        // Get Keychain instance
        Keychain keychain = Keychain.getKeychain("MyAwesomeApp");
        // Or now, you can use classes to get keychain instance:
        Keychain newKeychain = Keychain.getKeychain(KeyerTest.class); // It's cool, isn't it?

        keychain.setPassword("alias", "123".toCharArray()); // Set password
        char[] password = keychain.getPassword("alias"); // Get password

        // Now, you can use 'raw passwords':
        keychain.setPassword("alias-raw", "123".getBytes()); // Set raw password
        byte[] rawPassword = keychain.getRawPassword("alias-raw"); // Get raw password
    }
}
```

### See also /examples and /tests

## Тесты Keyer

Keyer тестируется через GitHub Actions при каждом пуше в тег.
Тестируются следующие платформы:

- macOs 14
- macOs 15
- Windows Server 2022
- Windows Server Latest

Локально тестируется на реальном железе:

- macOs 10.15
- Windows 10
- Windows 11

Все тесты успешно проходят. Если вы обнаружите ошибку, пожалуйста, сообщите о ней
в [Issues](https://github.com/plovdev/Keyer/issues).

Так же, начиная с версии 1.7-beta Keyer проверяется Qodana.

### Тестирование на Unix-подобных системах

На данный момент тестирование на Linux и других Unix-подобных системах **не проводится**, так как у нас нет доступа к
таким платформам, а для запуска тестов в CI требуется разблокировка `libsecret` через GUI.

Буду очень признателен, если кто-то сможет протестировать проект на Unix-системах и поделится результатом!

## Не забудьте добавить --enable-native-access={MODULE-NAME} или --enable-native-access=ALL-UNNAMED в VM-OPTIONS:)