# SmartCut App

Мобильное Android-приложение для управления умным кухонным слайсером на базе ESP32. Разработано в рамках курсовой работы.

Макет Figma: https://www.figma.com/design/ZbdAXuwVz9lwWIfUcJVCeb/Untitled?node-id=0-1&t=tekI7Cac4UcmaUws-0

---

## О проекте

SmartCut — система автоматической нарезки продуктов. Пользователь выбирает рецепт, выбирает ингредиент и режим нарезки (кубики или слайсы), после чего команда отправляется на устройство ESP32 через MQTT-протокол. Сервер хранит рецепты, ингредиенты и управляет авторизацией пользователей.

---

## Системная архитектура

```
Android App  ──── HTTP (Ktor Client) ────  Ktor Backend  ──── PostgreSQL
     │                                          │
     └──── MQTT (Eclipse Paho) ────  HiveMQ Broker  ────  ESP32
```

- Приложение общается с бэкендом по HTTP для работы с рецептами и авторизации
- Команды на устройство отправляются через публичный MQTT-брокер HiveMQ
- Бэкенд не участвует в MQTT-обмене — приложение напрямую публикует команды на брокер

---

## Стек технологий

### Android (клиент)
| Технология | Назначение |
|---|---|
| Kotlin | Язык разработки |
| Jetpack Compose | Декларативный UI |
| Material Design 3 | Дизайн-система |
| Navigation Compose | Навигация между экранами |
| ViewModel + StateFlow | MVVM, управление состоянием |
| Ktor Client | HTTP-запросы к серверу |
| Kotlinx Serialization | Сериализация JSON |
| Eclipse Paho MQTT 1.2.5 | MQTT-клиент для ESP32 |
| Coil 3 | Загрузка изображений по URL |
| SharedPreferences | Локальное хранение токена и адреса брокера |

### Backend (сервер)
| Технология | Назначение |
|---|---|
| Kotlin | Язык разработки |
| Ktor | HTTP-фреймворк |
| Netty | Движок HTTP-сервера |
| Exposed ORM | Kotlin DSL для SQL-запросов |
| PostgreSQL | База данных |
| HikariCP | Пул соединений с БД |
| JWT (Auth0) | Авторизация по токенам |
| BCrypt | Хэширование паролей |
| Logback | Логирование |

---

## Архитектура Android-клиента

Используется **Clean Architecture** с разделением на три слоя и паттерн **MVVM**.

```
presentation/        ← экраны (Compose) + ViewModel
domain/              ← модели данных + интерфейсы репозиториев
data/                ← реализация репозиториев + API + MQTT + локальное хранилище
```

### Слои

**Data** — работа с внешними источниками данных:
- `ApiClient.kt` — настройка HTTP-клиента Ktor, BASE_URL
- `TokenStorage.kt` — in-memory хранение JWT-токена
- `PreferencesManager.kt` — хранение токена и URL брокера в SharedPreferences
- `MqttManager.kt` — подключение к MQTT-брокеру, pub/sub
- `RecipeRepositoryImpl.kt` — HTTP-запросы за рецептами
- `AuthRepositoryImpl.kt` — HTTP-запросы авторизации
- `RecipeMapper.kt` — конвертация DTO → доменная модель
- `RecipeDto.kt`, `AuthDto.kt` — классы для десериализации JSON

**Domain** — чистые модели и интерфейсы без зависимостей от фреймворков:
- `Recipe.kt`, `Ingredient.kt`, `User.kt` — доменные модели
- `RecipeRepository.kt`, `AuthRepository.kt` — интерфейсы репозиториев
- `GetRecipesUseCase.kt`, `GetRecipeByIdUseCase.kt`, `LoginUseCase.kt` — бизнес-операции

**Presentation** — экраны и ViewModel:
- `MainScreen` + `MainViewModel` — главный экран, рецепт дня, последние рецепты
- `RecipesScreen` + `RecipeViewModel` — список всех рецептов
- `RecipeDetailScreen` + `RecipeDetailViewModel` — детали рецепта, выбор режима нарезки
- `BladeCubeScreen` + `BladeSettingsViewModel` — настройка нарезки кубиками
- `SlicesScreen` + `SliceSettingsViewModel` — настройка нарезки слайсами
- `SettingsScreen` + `SettingsViewModel` — настройки MQTT-брокера
- `LoginScreen` + `LoginViewModel` — вход в аккаунт
- `RegisterScreen` + `RegisterViewModel` — регистрация
- `CreateRecipeScreen` + `CreateRecipeViewModel` — создание рецепта
- `NavGraph.kt` — граф навигации
- `Screen.kt` — маршруты навигации (sealed class)

---

## Архитектура бэкенда

```
Application.kt       ← точка входа, подключение плагинов
plugins/             ← настройка Ktor (Security, Serialization, Routing)
routing/             ← HTTP-маршруты (auth, recipes)
database/            ← подключение к БД, таблицы, DataSeeder
services/            ← бизнес-логика (UsersService)
```

### Файлы

| Файл | Описание |
|---|---|
| `Application.kt` | Точка входа. Инициализирует БД, плагины, маршруты |
| `DatabaseFactory.kt` | Настройка HikariCP, подключение Exposed к PostgreSQL |
| `Security.kt` | Настройка JWT-авторизации |
| `Serialization.kt` | Настройка JSON-сериализации |
| `Routing.kt` | Регистрация роутов, раздача статических файлов изображений |
| `AuthRoutes.kt` | POST /auth/register, POST /auth/login |
| `RecipeRoutes.kt` | GET /recipes, GET /recipes/{id}, POST /recipes, PUT /recipes/{id}, DELETE /recipes/{id} |
| `UsersService.kt` | Хэширование паролей, создание пользователей, генерация JWT |
| `DataSeeder.kt` | Заполнение базы стартовыми рецептами при запуске сервера |

---

## API Endpoints

| Метод | Путь | Авторизация | Описание |
|---|---|---|---|
| POST | `/auth/register` | Нет | Регистрация пользователя |
| POST | `/auth/login` | Нет | Вход, возвращает JWT-токен |
| GET | `/recipes` | JWT | Список всех рецептов с ингредиентами |
| GET | `/recipes/{id}` | JWT | Рецепт по ID |
| POST | `/recipes` | JWT | Создание рецепта |
| PUT | `/recipes/{id}` | JWT | Редактирование рецепта |
| DELETE | `/recipes/{id}` | JWT | Удаление рецепта |
| GET | `/images/{filename}` | Нет | Статические изображения |

---

## MQTT

Приложение использует библиотеку **Eclipse Paho MQTT v3 1.2.5**.

| Параметр | Значение |
|---|---|
| Брокер по умолчанию | `tcp://broker.hivemq.com:1883` |
| Топик команд | `smartcut/cmd` |
| Топик статуса | `smartcut/status` |

Адрес брокера можно изменить в настройках приложения — сохраняется в SharedPreferences.

### Формат команд

**Нарезка кубиками:**
```json
{"mode": "cube", "width": 15, "height": 15, "speed": 0.30}
```

**Нарезка слайсами:**
```json
{"mode": "slice", "thickness": 5, "speed": 0.30}
```

### Поток команды

```
Пользователь нажимает "Применить"
        ↓
ViewModel вызывает MqttManager.publish()
        ↓
MqttManager публикует JSON в топик smartcut/cmd
        ↓
HiveMQ Broker доставляет сообщение
        ↓
ESP32 (подписан на smartcut/cmd) получает команду и запускает нарезку
```

---

## База данных

### Таблицы

**users**
| Поле | Тип | Описание |
|---|---|---|
| id | Int | Первичный ключ |
| name | String | Имя пользователя |
| email | String | Email (уникальный) |
| password_hash | String | Хэш пароля BCrypt |

**recipes**
| Поле | Тип | Описание |
|---|---|---|
| id | Int | Первичный ключ |
| name | String | Название рецепта |
| description | String | Описание |
| image_url | String | URL изображения |
| user_id | Int | FK → users |

**ingredients**
| Поле | Тип | Описание |
|---|---|---|
| id | Int | Первичный ключ |
| name | String | Название ингредиента |
| amount | String | Количество |
| cuttable | Boolean | Можно ли нарезать на устройстве |
| recipe_id | Int | FK → recipes |

Фотографии рецептов хранятся на диске сервера в папке `resources/images/`. В базе данных хранится только URL.

---

## Авторизация

1. Пользователь вводит email и пароль
2. Сервер проверяет пароль через BCrypt
3. Сервер генерирует JWT-токен (payload: userId) и возвращает клиенту
4. Приложение сохраняет токен в SharedPreferences через `PreferencesManager`
5. Каждый HTTP-запрос отправляется с заголовком `Authorization: Bearer <token>`
6. Сервер проверяет подпись токена через `configureSecurity()`

---

## Запуск проекта

### Требования
- Android Studio Hedgehog или новее
- JDK 21 (для бэкенда)
- PostgreSQL 14+
- Android устройство или эмулятор (API 24+)

### Бэкенд
1. Создать базу данных PostgreSQL
2. Настроить подключение в `application.yaml`
3. Запустить `Application.kt` — таблицы создадутся автоматически, DataSeeder заполнит стартовые рецепты

### Android
1. Открыть проект в Android Studio
2. В `ApiClient.kt` указать IP-адрес сервера:
   ```kotlin
   const val BASE_URL = "http://<ваш_ip>:8080"
   ```
3. Убедиться что телефон и сервер в одной Wi-Fi сети
4. Запустить приложение

---

## Структура проекта (Android)

```
app/src/main/java/com/example/smartcutapp/
├── data/
│   ├── local/
│   │   └── PreferencesManager.kt
│   ├── mapper/
│   │   └── RecipeMapper.kt
│   ├── mqtt/
│   │   └── MqttManager.kt
│   ├── remote/
│   │   ├── api/
│   │   │   ├── ApiClient.kt
│   │   │   └── TokenStorage.kt
│   │   └── dto/
│   │       ├── RecipeDto.kt
│   │       └── AuthDto.kt
│   └── repository/
│       ├── RecipeRepositoryImpl.kt
│       └── AuthRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── Recipe.kt
│   │   ├── Ingredient.kt
│   │   └── User.kt
│   ├── repository/
│   │   ├── RecipeRepository.kt
│   │   └── AuthRepository.kt
│   └── usecase/
│       ├── GetRecipesUseCase.kt
│       ├── GetRecipeByIdUseCase.kt
│       └── LoginUseCase.kt
├── presentation/
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   └── screens/
│       ├── main/
│       ├── recipes/
│       ├── recipe_detail/
│       ├── blade/
│       ├── settings/
│       ├── login/
│       ├── register/
│       └── create_recipe/
├── SmartCutApplication.kt
└── MainActivity.kt
```
