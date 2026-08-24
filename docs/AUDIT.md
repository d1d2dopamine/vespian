# Vespian: аудит

Коротко: в репозитории Vespian лежит модуль `app` от Ikna. Манифест, файл
сборки, тема и дефолтные строки — иknовские. Всё остальное (2112 строк
`MainActivity`, движок, Telegram-бот, Health Connect, Room) — веспиановское и
нетронутое. Отсюда все три симптома: вылет при запуске, вечная загрузка и «ничего
не работает, хотя раньше работало».

---

## 1. Почему вылетало

### 1.1 Тема не AppCompat — падение в `onCreate`

`MainActivity`, `OnboardingActivity` и `SettingsActivity` наследуются от
`AppCompatActivity`. В манифесте прописана `@style/Theme.Ikna`, а она объявлена
так:

```xml
<style name="Theme.Ikna" parent="android:Theme.Material.NoActionBar">
```

`AppCompatActivity` при старте проверяет тему и бросает
`IllegalStateException: You need to use a Theme.AppCompat theme (or descendant)
with this activity`. Это вылет до появления интерфейса — то, что видно как
«приложение запустилось и сразу закрылось».

### 1.2 В манифесте объявлен не тот класс приложения и не та Activity

```xml
<application android:name=".IknaApp" ...>
    <activity android:name=".MainActivity" ...>   <!-- = dev.ikna.MainActivity -->
```

`namespace` в `app/build.gradle.kts` — `dev.ikna`, поэтому `.IknaApp` и
`.MainActivity` разворачиваются в `dev.ikna.*`, а не в `dev.vespian.*`. Запускался
чужой экран; при отсутствии/несовместимости классов — `ClassNotFoundException`
и `ActivityNotFoundException`.

### 1.3 Падение в композиции: медиана пустого списка

`MainActivity.kt`, `ModelTab`:

```kotlin
fun median(pick: (Particle) -> Double) =
    f.particles.map(pick).sorted()[f.particles.size / 2]
```

При пустом `particles` — `IndexOutOfBoundsException` прямо во время
отрисовки. Пустой фильтр — норма: первый запуск, сброс данных, неудачный
`refit`.

---

## 2. Почему всё вечно грузилось

### 2.1 Исключения проглатываются, состояние остаётся `null`

```kotlin
LaunchedEffect(Unit) {
    forecast = runCatching { Engine.forecast(ctx) }.getOrNull()
}
...
if (forecast == null) Loading()
```

`getOrNull()` превращает любую ошибку (миграция Room, отсутствующий Health
Connect, `SecurityException` без разрешения) в `null`, а `null` — в `Loading()`
навсегда. Тот же приём в `DriftTab` и `ModelTab`. Спиннер вместо ошибки — это не
загрузка, это скрытый сбой.

Правильно: три состояния вместо двух.

```kotlin
sealed interface State<out T> {
    data object Loading : State<Nothing>
    data class Ready<T>(val value: T) : State<T>
    data class Failed(val error: Throwable) : State<Nothing>
}
```

и `Failed` показывать через `Trial3Notice(text = ..., actionLabel = "ПОВТОРИТЬ")`.

### 2.2 `while (true)` в `LaunchedEffect`

`DataTab`:

```kotlin
LaunchedEffect(refresh) {
    while (true) {
        rows = load()
        delay(60_000L)
    }
}
```

Ключ `refresh` меняется при каждом `onChanged()`, эффект перезапускается,
состояние сбрасывается в загрузку. Плюс опрос базы раз в минуту, пока экран
открыт. Нужен `Flow` из Room (`@Query` возвращающий `Flow<List<...>>`) и
`collectAsStateWithLifecycle()` — база сама скажет, когда данные изменились.

### 2.3 Одна большая `Column` с `verticalScroll`

Все вкладки собираются целиком, включая невидимые. `LazyColumn` — и первый кадр
перестаёт зависеть от объёма истории.

### 2.4 Глобальный мутабельный синглтон

```kotlin
private object UiCache { var forecast: Forecast? = null; ... }
```

Живёт дольше Activity, не потокобезопасен, отдаёт устаревшие данные после
смены конфигурации и держит ссылки. Место этому — `ViewModel` + `StateFlow`.

---

## 3. Почему «раньше работало»

Два независимых механизма.

**Подпись.** Файл сборки ищет `ikna.keystore` в корне. Его там нет — в репозитории
лежит `app/vespian-debug.jks`. Значит `hasFixedKey = false`, `signingConfig` не
применяется, APK подписывается локальным debug-ключом, который у каждой машины
и каждого CI-раннера свой. Установка поверх ранее установленной версии
отклоняется: `INSTALL_FAILED_UPDATE_INCOMPATIBLE` / «signatures do not match».
На телефоне остаётся старый рабочий APK, новый не ставится — «раньше работало».

**Сборка вообще не проходит.** `app/build.gradle.kts` использует акцессоры
каталога версий (`libs.plugins.android.application`, `libs.androidx.core.ktx`,
…), а каталога в репозитории нет: папки `gradle/` не существует, значит нет и
`gradle/libs.versions.toml`. Gradle падает на конфигурации с `Unresolved
reference: libs`. То, что стоит на телефоне, собрано из другого состояния
репозитория.

---

## 4. Полный список найденного

### Сборка и конфигурация

| № | Проблема | Следствие |
|---|---|---|
| 1 | нет `gradle/libs.versions.toml`, а `app/build.gradle.kts` использует `libs.*` | `Unresolved reference: libs` — сборка не конфигурируется |
| 2 | `namespace` и `applicationId` = `dev.ikna` | `R` генерируется в `dev.ikna`; ни один файл в `dev/vespian/**` его не импортирует → 547 обращений `R.string.*` не компилируются в 8 файлах |
| 3 | версии плагинов заданы и в корневом `build.gradle.kts` (8.9.1 / 2.0.21), и через `alias(libs…)` в модуле | рассинхронизация версий |
| 4 | нет зависимости `androidx.appcompat` | три Activity наследуют `AppCompatActivity` — не компилируется |
| 5 | нет `androidx.health.connect:connect-client` | 16 файлов импортируют Health Connect — не компилируется |
| 6 | нет `androidx.security:security-crypto` (используется в `tg/Secrets.kt`) | не компилируется |
| 7 | лишние зависимости от Ikna: navigation-compose, lottie, datastore, serialization | вес APK, время сборки |
| 8 | keystore указывает на несуществующий `ikna.keystore` | случайная подпись, невозможность обновления (п. 3) |
| 9 | `ksp { arg("room.schemaLocation", …) }` при `exportSchema = false` в `Db.kt` | схемы не выгружаются, миграции нечем проверить |
| 10 | в `app/src/main/java/dev/ikna/**` лежит 38 файлов чужого приложения | часть ссылается на отсутствующие классы; мусор в сборке |

### Манифест и ресурсы

| № | Проблема | Следствие |
|---|---|---|
| 11 | манифест иknовский: `.IknaApp`, `.MainActivity` | запускается не то приложение (п. 1.2) |
| 12 | не объявлен `VespianApp` | `Store.restore()` и `Scheduler.start()` не вызываются никогда: расписание, воркеры, бот мертвы |
| 13 | не объявлены `OnboardingActivity` и `SettingsActivity` | `ActivityNotFoundException` при первом запуске и при открытии настроек |
| 14 | не объявлен `LightService` | датчик света не опрашивается, `LIGHT_MIN_SAMPLES` не набирается, модель без светового входа |
| 15 | не объявлены `BootReceiver`, `WatchdogReceiver`, `Reply` | после перезагрузки и после обновления ничего не запускается; кнопки в уведомлениях не работают |
| 16 | не объявлен `ForecastWidget` + `meta-data` | виджет не появляется в списке, хотя `widget_forecast_info.xml` и layout на месте |
| 17 | нет `INTERNET` | Telegram-бот (`HttpURLConnection` к `api.telegram.org`) не работает |
| 18 | нет `FOREGROUND_SERVICE` / типа службы | на targetSdk 34+ `startForeground()` бросает `MissingForegroundServiceTypeException` |
| 19 | нет `RECEIVE_BOOT_COMPLETED` | автозапуск невозможен |
| 20 | нет health-разрешений (`READ_SLEEP`, `READ_HEART_RATE`, `READ_OXYGEN_SATURATION`, `READ_HEALTH_DATA_IN_BACKGROUND`, `READ_HEALTH_DATA_HISTORY`) | Health Connect не отдаёт данные |
| 21 | нет `BLUETOOTH_CONNECT` | `health/Band.kt` не подключается к браслету |
| 22 | нет `<queries>` для `com.google.android.apps.healthdata` | проверка «установлен ли Health Connect» в онбординге всегда отвечает «нет» |
| 23 | нет intent-filter для `ACTION_SHOW_PERMISSIONS_RATIONALE` и alias `ViewPermissionUsageActivity` | Health Connect на Android 14+ не пускает к разрешениям |
| 24 | `values/themes.xml` содержит только `Theme.Ikna` | п. 1.1 |
| 25 | `values/strings.xml` — одна строка `app_name = Ikna`, при 478 в `values-ru` | 465 идентификаторов не резолвятся; на неруссоязычном устройстве приложение называлось бы Ikna |
| 26 | `AppCompatDelegate.setApplicationLocales` без `res/xml/locales_config.xml` | системный выбор языка для приложения не работает |

### Рантайм

| № | Проблема | Где |
|---|---|---|
| 27 | `runCatching{}.getOrNull()` вокруг всей загрузки | `MainActivity` (3 места) |
| 28 | медиана пустого списка | `ModelTab` |
| 29 | `while (true) { … delay(60_000) }` | `DataTab` |
| 30 | глобальный `object UiCache` вместо `ViewModel` | `MainActivity` |
| 31 | одна `Column` + `verticalScroll` на все вкладки | `MainActivity` |
| 32 | `CoroutineScope(SupervisorJob() + Dispatchers.IO)` в `Application` без отмены | `VespianApp` |
| 33 | Room v6, `exportSchema = false`, шесть рукописных миграций, нет `fallbackToDestructiveMigration` | `db/Db.kt` — любая ошибка миграции = `IllegalStateException` при первом обращении, проглатывается в п. 27 и превращается в вечный спиннер |
| 34 | `Engine` кеширует `filterCache/forecastCache` в статических полях | несогласованность после смены данных, если забыт `invalidate()` |
| 35 | в CI есть grep на U+FFFD в strings.xml, но `dev/ikna/ui/theme/Theme.kt` содержит такой символ в комментарии | шумный красный CI |

---

## 5. Порядок исправления

Применить готовый набор из `../vespian-fix`:

```sh
cd vespian-fix && ./apply.sh /path/to/vespian-main
cd /path/to/vespian-main && rm -rf app/src/main/java/dev/ikna
./gradlew :app:assembleDebug
```

Это закрывает пункты 1–8, 11–26. Дальше вручную, в этом порядке:

1. **п. 27** — трёхсостояточная модель загрузки. Пока это не сделано, любая
   следующая ошибка снова выглядит как «вечная загрузка», и отладка невозможна.
2. **п. 33** — на время отладки добавить
   `.fallbackToDestructiveMigrationOnDowngrade()` и включить `exportSchema = true`;
   миграции 1→6 прогнать тестом на реальных дампах.
3. **п. 28** — `particles.isEmpty()` → «модель ещё не обучена», а не индекс.
4. **п. 29** — `Flow` из DAO вместо опроса.
5. **п. 30, 31** — `ViewModel` + `StateFlow`, `LazyColumn`.
6. **п. 32** — скоуп с отменой или `WorkManager`.
7. **п. 10, 35** — удалить `dev/ikna/**`.

## 6. Потом — интерфейс

`dev/vespian/ui/Theme.kt` (119 строк) — обычная тема Material 3 с двумя схемами.
После того как проект соберётся, её можно заменить на `Trial3Theme` из
`:trial3lib`: слой совместимости `compat/Material3Compat.kt` уже делает так, что
`MaterialTheme.colorScheme.*` и `Text(...)` продолжают работать без правки
экранов. В `Trial3Glyph` для этого уже есть нужные знаки: `MOON`, `SUN`, `BED`,
`CLOCK`, `ALARM`, `PULSE`, `HEART`, `CUP`, `FLASK`.
