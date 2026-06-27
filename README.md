# Keyer

**Keyer** — это современная и легковесная библиотека для работы с системными хранилищами паролей напрямую из Java.

![Java Version](https://img.shields.io/badge/Java-25%2B-blue)
![License](https://img.shields.io/badge/License-Public%20Domain-blue)

## Features

- **Project Panama**: Максимальная производительность нативных вызовов без тяжелых зависимостей.
- **Безопасность**: Работа с секретами через `char[]` или `byte[]` для минимизации следов в памяти JVM.
- **Кроссплатформенность**: Единый интерфейс для macOS, Windows и Linux.
- **Zero Dependencies**: Только стандартная JDK, SLF4J для логирования и JUnit для тестов.

## Доступно на Maven central

```xml

<dependency>
    <groupId>io.github.plovdev</groupId>
    <artifactId>keyer</artifactId>
    <version>1.7-beta</version> <!-- Latest version !-->
</dependency>
```

И Gradle:

```groovy
implementation("io.github.plovdev:keyer:1.7-beta")
```

## Быстрый старт

### Инициализация

Библиотека автоматически определит вашу ОС и выберет нужную реализацию:

```java
public class KeyerTest {
    static void main() {
        // get keychain instance
        Keychain keychain = Keychain.getKeychain("MyAwesomeApp");
        // or now, you can use classes to get keychain instance:
        Keychain newKeychain = Keychain.getKeychain(KeyerTest.class); // it's cool, isn't it?

        keychain.setPassword("alias", "123".toCharArray()); // set password
        char[] password = keychain.getPassword("alias"); // get password

        // Now, you can use 'raw passwords':
        keychain.setPasswordRaw("alias-raw", "123".getBytes()); // set password raw
        byte[] rawPassword = keychain.getRawPassword("alias-raw"); // get raw password
    }
}
```

### See also /examples and /tests

## Не забудьте добавить --enable-native-access={MODULE-NAME} или --enable-native-access=ALL-UNNAMED в VM-OPTIONS:)