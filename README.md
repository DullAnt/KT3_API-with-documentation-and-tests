# Ktor API с документацией и тестами

REST API на Ktor с Swagger UI, OpenAPI, тестами и middleware логированием.

## Технологии

- **Ktor** 2.3.7 - веб-фреймворк
- **Swagger UI** - интерактивная документация
- **OpenAPI 3.0** - спецификация API
- **JWT** - аутентификация
- **BCrypt** - хеширование паролей
- **CallLogging** - middleware логирования
- **StatusPages** - обработка ошибок
- **JUnit** - тестирование

## Запуск

```bash
./gradlew run
```

Сервер: http://localhost:8080

## Документация

### Swagger UI (интерактивная)
```
http://localhost:8080/swagger
```

### OpenAPI спецификация
```
http://localhost:8080/openapi
```

## Запуск тестов

```bash
./gradlew test
```

Тесты находятся в `src/test/kotlin/com/example/`:
- `AuthTest.kt` - тесты аутентификации (6 тестов)
- `TaskTest.kt` - тесты CRUD задач (12 тестов)

### Покрытие тестами:

**AuthTest:**
- ✅ Регистрация - успех
- ✅ Регистрация - дубликат username (409)
- ✅ Регистрация - короткий пароль (400)
- ✅ Вход - успех
- ✅ Вход - неверный пароль (401)
- ✅ Вход - несуществующий пользователь (401)

**TaskTest:**
- ✅ Запрос без токена (401)
- ✅ Создание задачи - успех
- ✅ Создание задачи - пустой title (400)
- ✅ Получение всех задач
- ✅ Получение по ID - успех
- ✅ Получение по ID - не найдено (404)
- ✅ Обновление задачи (PUT)
- ✅ Удаление задачи (DELETE)
- ✅ Фильтр по completed (query)
- ✅ Поиск по тексту (query)
- ✅ Изоляция данных пользователей

## API Endpoints

### Аутентификация

| Метод | URL | Описание |
|-------|-----|----------|
| POST | /api/auth/register | Регистрация |
| POST | /api/auth/login | Вход |

### Задачи (требуют JWT)

| Метод | URL | Описание |
|-------|-----|----------|
| GET | /api/tasks | Все задачи |
| GET | /api/tasks?completed=true | Фильтр (query) |
| GET | /api/tasks?search=text | Поиск (query) |
| GET | /api/tasks/{id} | По ID (path) |
| POST | /api/tasks | Создать |
| PUT | /api/tasks/{id} | Обновить |
| DELETE | /api/tasks/{id} | Удалить |

## Примеры (PowerShell)

```powershell
# Регистрация
$reg = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -ContentType "application/json" -Body '{"username":"test","email":"test@mail.com","password":"1234"}'
$token = $reg.token

# Создать задачу
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/tasks" -Method POST -ContentType "application/json" -Headers $headers -Body '{"title":"Задача","description":"Описание"}'

# Получить задачи
Invoke-RestMethod -Uri "http://localhost:8080/api/tasks" -Headers $headers
```

## HTTP-коды

| Код | Описание |
|-----|----------|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 404 | Not Found |
| 405 | Method Not Allowed |
| 409 | Conflict |
| 500 | Internal Server Error |

## Middleware логирование

Все HTTP запросы логируются в формате:
```
HH:mm:ss.SSS [requestId] INFO  - HTTP GET /api/tasks -> 200 OK (15ms) | UA: ...
```

## Структура проекта

```
src/
├── main/
│   ├── kotlin/com/example/
│   │   ├── Application.kt
│   │   ├── models/Models.kt
│   │   ├── plugins/
│   │   │   ├── Plugins.kt      # Serialization, JWT, StatusPages, Logging
│   │   │   └── Routing.kt      # Routes + Swagger
│   │   ├── repository/
│   │   │   ├── UserRepository.kt
│   │   │   └── TaskRepository.kt
│   │   ├── routes/
│   │   │   ├── AuthRoutes.kt
│   │   │   └── TaskRoutes.kt
│   │   └── security/
│   │       └── JwtConfig.kt
│   └── resources/
│       ├── logback.xml
│       └── openapi/
│           └── documentation.yaml  # OpenAPI спецификация
└── test/
    └── kotlin/com/example/
        ├── AuthTest.kt         # 6 тестов
        └── TaskTest.kt         # 12 тестов
```

## Особенности

- **Swagger UI** на `/swagger` с возможностью тестирования API
- **OpenAPI 3.0** спецификация с полным описанием всех endpoints
- **18 автотестов** покрывающих ключевые сценарии
- **StatusPages** для единообразной обработки ошибок
- **CallLogging** middleware с request ID для трейсинга
- **Изоляция данных** - пользователи видят только свои задачи
