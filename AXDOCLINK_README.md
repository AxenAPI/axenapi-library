# @AxDocLink Annotation

## Описание

Аннотация `@AxDocLink` позволяет добавлять ссылки на файлы документации к методам контроллеров. При обработке аннотации, Swagger автоматически добавляет расширение `x-documentation-file-links` в спецификацию OpenAPI для соответствующего endpoint'а.

## Использование

### 1. Добавьте аннотацию к методу контроллера

```java
import pro.axenix_innovation.axenapi.annotation.AxDocLink;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MyController {
    
    @PostMapping("/users")
    @AxDocLink("docs/users/create-user.md")
    public User createUser(@RequestBody UserDto userDto) {
        // implementation
    }
    
    @GetMapping("/users/{id}")
    @AxDocLink("docs/users/get-user.md")
    public User getUser(@PathVariable Long id) {
        // implementation
    }
}
```

### 2. Результат в Swagger/OpenAPI

После обработки аннотации, в спецификации OpenAPI для endpoint'а будет добавлено расширение:

```yaml
paths:
  /api/users:
    post:
      x-documentation-file-links: "docs/users/create-user.md"
      # ... остальные параметры endpoint'а
  /api/users/{id}:
    get:
      x-documentation-file-links: "docs/users/get-user.md"
      # ... остальные параметры endpoint'а
```

## Параметры

- `value` (обязательный) - относительный путь к файлу документации

## Компоненты

### 1. AxDocLink.java
Аннотация, которая применяется к методам контроллеров.

### 2. AxDocLinkProcessor.java
Annotation processor, который валидирует корректность использования аннотации во время компиляции.

### 3. AxDocLinkConfiguration.java
Spring конфигурация, которая добавляет расширение `x-documentation-file-links` в Swagger/OpenAPI спецификацию во время выполнения.

## Требования

- Spring Boot
- SpringDoc OpenAPI (для Swagger)
- Java 8+

## Примечания

- Аннотация может быть применена только к методам
- Значение аннотации не должно быть пустым
- Путь к файлу должен быть относительным
- Аннотация работает с любыми методами контроллеров (не только с Kafka)
