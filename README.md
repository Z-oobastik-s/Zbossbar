# Zbossbar - Продвинутый плагин для BossBar
![Version](https://img.shields.io/badge/версия-1.0-brightgreen)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4+-blue)
![Java](https://img.shields.io/badge/Java-21+-red)
![Последнее обновление](https://img.shields.io/badge/Обновлено-Июнь%202025-orange)

## 📖 Обзор

**Zbossbar** - это мощный и гибкий плагин для Minecraft, который позволяет создавать и настраивать персональные боссбары для игроков на вашем сервере. С помощью этого плагина вы можете показывать важную информацию, приветствия, новости и многое другое в верхней части экрана игроков в стильном и заметном виде.

![ZBossbar Demo](https://i.ibb.co/tpGFZBxj/1.jpg)

## ✨ Особенности

- **Уникальный BossBar для каждого мира** - настройте разные сообщения и стили для каждого мира на вашем сервере
- **Циклические сообщения** - настройте несколько сообщений, которые будут автоматически сменяться через заданные интервалы
- **Полная поддержка цветов** - используйте стандартные цвета Minecraft, HEX-цвета и градиенты для стильного оформления
- **Поддержка PlaceholderAPI** - добавляйте динамические данные в ваши сообщения
- **Анимация цветов и стилей** - создавайте привлекательные анимированные эффекты для боссбаров
- **Различные типы прогресса** - здоровье игрока, TPS сервера, время, произвольное значение и др.
- **Персональные настройки** - игроки могут отключать/включать отображение боссбара
- **Плавные переходы между мирами** - боссбар плавно меняется при перемещении между мирами
- **Оптимизированный код** - минимальное влияние на производительность сервера

## 📋 Требования

- **Minecraft**: 1.21.4 или выше
- **Java**: 21 или выше
- **Сервер**: Paper, Spigot или производные (рекомендуется Paper)
- **Опционально**: PlaceholderAPI для использования расширенных плейсхолдеров

## 💾 Установка

1. Скачайте последнюю версию плагина из [официального репозитория](https://github.com/Z-oobastik-s/Zbossbar)
2. Поместите JAR-файл в папку `plugins` вашего сервера
3. Перезапустите сервер или используйте плагин для загрузки плагинов без перезапуска
4. Отредактируйте файл `config.yml` под ваши нужды (он будет создан автоматически)
5. Выполните команду `/zbossbar reload`, чтобы применить изменения

## 🔧 Команды

| Команда | Описание | Разрешение |
|---------|----------|------------|
| `/bossbar` | Включить/выключить отображение боссбара для себя | zbossbar.toggle |
| `/zbossbar` | Показать помощь по командам плагина | zbossbar.admin |
| `/zbossbar reload` | Перезагрузить конфигурацию плагина | zbossbar.admin |
| `/zbossbar toggle` | Включить/выключить плагин на сервере | zbossbar.admin |
| `/zbossbar toggle <игрок>` | Включить/выключить боссбар для указанного игрока | zbossbar.admin |
| `/zbossbar world <мир>` | Переключить видимость боссбара в указанном мире | zbossbar.admin |
| `/zbossbar status` | Показать статус плагина и статистику | zbossbar.admin |
| `/zbossbar debug` | Включить/выключить режим отладки | zbossbar.admin |
| `/zbossbar help` | Показать список всех команд | zbossbar.admin |

## 🔐 Разрешения

| Разрешение | Описание | По умолчанию |
|------------|----------|--------------|
| `zbossbar.toggle` | Позволяет игроку включать/выключать боссбар для себя | true |
| `zbossbar.admin` | Доступ ко всем административным командам | op |
| `zbossbar.reload` | Разрешение на перезагрузку плагина | op |
| `zbossbar.toggle.others` | Позволяет включать/выключать боссбар для других игроков | op |
| `zbossbar.bypass` | Игнорирует настройки видимости мира (боссбар всегда виден) | op |
| `zbossbar.world.<мир>` | Разрешает видеть боссбар в конкретном мире | true |
| `zbossbar.update` | Получать уведомления об обновлениях плагина | op |
| `zbossbar.debug` | Разрешение на использование команды отладки | op |
| `zbossbar.use` | Базовое разрешение на использование плагина | true |
| `zbossbar.*` | Все разрешения плагина | op |

## ⚙️ Конфигурация

### Основные настройки (config.yml)

```yaml
# Zbossbar - Продвинутый плагин для BossBar
# Версия: 1.0
# Minecraft: 1.21.4+
# Автор: Z-oobastik-s
# Дата: 2025

# Глобальные настройки
settings:
  # Включить/выключить плагин
  enabled: true
  
  # Проверять обновления при запуске
  check-updates: true
  
  # Интервал обновления боссбара (в тиках, 20 тиков = 1 секунда)
  update-interval: 2
  
  # Максимальное расстояние видимости боссбара (-1 = бесконечно)
  max-view-distance: -1
  
  # Плавные переходы между мирами
  smooth-world-transitions: true
  
  # Задержка при смене мира (в тиках)
  world-change-delay: 5
  
  # Сохранение пользовательских настроек
  save-player-preferences: true
  
  # Интервал автосохранения (в минутах)
  auto-save-interval: 5
  
  # Интеграция с PlaceholderAPI
  use-placeholders: true
  
  # Кэширование плейсхолдеров
  cache-placeholders: true
  
  # Время кэширования плейсхолдеров (в секундах)
  placeholder-cache-time: 3
  
  # Режим отладки
  debug-mode: false

# Настройки для конкретных миров
worlds:
  # Настройки для мира "world"
  world:
    # Включить/выключить боссбар в этом мире
    enabled: true
    
    # Заголовок боссбара (можно использовать цвета и PlaceholderAPI)
    title: "<gradient:#FF0000:#A6EB0F>〔Zbossbar〕</gradient> &fДобро пожаловать, %player_name%!"
    
    # Несколько заголовков для циклического отображения
    titles:
      - "<gradient:#FF0000:#A6EB0F>〔Zbossbar〕</gradient> &fДобро пожаловать, %player_name%!"
      - "<gradient:#00BFFF:#4B0082>〔Сервер〕</gradient> &fОнлайн: &a%server_online%&f/&c%server_max_players%"
      - "<rainbow>〔Информация〕</rainbow> &fУровень: &a%player_level% &f| Опыт: &a%player_exp_to_level%"
    
    # Интервал смены заголовков (в тиках)
    title-change-interval: 100
    
    # Цвет боссбара (PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE)
    color: BLUE
    
    # Несколько цветов для анимации
    colors:
      - BLUE
      - PURPLE
      - RED
      - YELLOW
    
    # Стиль боссбара (SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20)
    style: SEGMENTED_12
    
    # Несколько стилей для анимации
    styles:
      - SEGMENTED_12
      - SEGMENTED_20
      - SOLID
    
    # Настройки анимации
    animation-enabled: true
    animation-interval: 20
    reverse-animation: false
    ping-pong-animation: true
    
    # Тип прогресса (STATIC, PLAYER_HEALTH, PLAYER_EXP, SERVER_TPS, TIME, CUSTOM)
    progress-type: PLAYER_HEALTH
    
    # Статическое значение прогресса (если тип STATIC, от 0.0 до 1.0)
    progress-value: 1.0
    
    # Максимальное значение прогресса (для типов PLAYER_LEVEL и CUSTOM)
    progress-max-value: 100.0
    
    # Плейсхолдер для пользовательского прогресса (если тип CUSTOM)
    progress-placeholder: "%player_health%"
    
    # Формат времени (для типа TIME)
    time-format: "HH:mm:ss"
    
    # Использовать серверное время (для типа TIME)
    use-server-time: true
    
    # Отображать боссбар поверх предыдущего
    overlay-previous: false
    
    # Разрешение, необходимое для отображения этого боссбара
    permission: null
    
    # Показывать боссбар при входе в мир
    show-on-join: true

  # Настройки для мира "world_nether"
  world_nether:
    enabled: true
    title: "<gradient:#FF4500:#8B0000>〔Ад〕</gradient> &cБудьте осторожны, %player_name%!"
    color: RED
    style: SOLID
    progress-type: PLAYER_HEALTH

  # Настройки для мира "world_the_end"
  world_the_end:
    enabled: true
    title: "<gradient:#9932CC:#483D8B>〔Край〕</gradient> &dОпасность повсюду!"
    color: PURPLE
    style: SEGMENTED_20
    progress-type: STATIC
    progress-value: 1.0

# Настройки сообщений плагина
messages:
  prefix: "&8[&b&lZbossbar&8] "
  enabled: "&aБоссбар включен."
  disabled: "&cБоссбар выключен."
  reloaded: "&aКонфигурация успешно перезагружена."
  no-permission: "&cУ вас нет разрешения на выполнение этой команды."
  player-not-found: "&cИгрок не найден."
  toggle-on: "&aБоссбар для &e%player% &aвключен."
  toggle-off: "&cБоссбар для &e%player% &cвыключен."
  plugin-enabled: "&aПлагин успешно включен."
  plugin-disabled: "&cПлагин отключен."
  world-enabled: "&aБоссбар в мире &e%world% &aвключен."
  world-disabled: "&cБоссбар в мире &e%world% &cвыключен."
  debug-enabled: "&aРежим отладки включен."
  debug-disabled: "&cРежим отладки выключен."
  status-format: "&7Статус: %status% &7| Боссбаров: &e%count% &7| Миры: &e%worlds%"
```

## 🎨 Форматирование текста

### Стандартные цвета
- `&0` или `§0` - Черный
- `&1` или `§1` - Темно-синий
- `&2` или `§2` - Темно-зеленый
- `&3` или `§3` - Темно-голубой
- `&4` или `§4` - Темно-красный
- `&5` или `§5` - Фиолетовый
- `&6` или `§6` - Золотой
- `&7` или `§7` - Серый
- `&8` или `§8` - Темно-серый
- `&9` или `§9` - Синий
- `&a` или `§a` - Зеленый
- `&b` или `§b` - Голубой
- `&c` или `§c` - Красный
- `&d` или `§d` - Розовый
- `&e` или `§e` - Желтый
- `&f` или `§f` - Белый

### Форматирование
- `&l` или `§l` - Жирный
- `&o` или `§o` - Курсив
- `&n` или `§n` - Подчеркнутый
- `&m` или `§m` - Зачеркнутый
- `&k` или `§k` - Случайные символы
- `&r` или `§r` - Сброс форматирования

### HEX-цвета
```
&#RRGGBB или #RRGGBB
Пример: &#ff0000 - красный цвет
```

### Градиенты
```
<gradient:#первый_цвет:#второй_цвет>текст</gradient>
Пример: <gradient:#ff0000:#00ff00>Радужный текст</gradient>
```

### Радужный текст
```
<rainbow>текст</rainbow>
Пример: <rainbow>Радужный текст</rainbow>
```

## 🔄 Использование PlaceholderAPI

Для использования PlaceholderAPI в сообщениях боссбара, необходимо:

1. Установить плагин PlaceholderAPI на сервер
2. Установить нужные расширения PlaceholderAPI (используйте команду `/papi ecloud`)
3. Включить интеграцию в настройках плагина (`use-placeholders: true`)
4. Использовать плейсхолдеры в формате `%placeholder%` в текстах боссбара

### Часто используемые плейсхолдеры:
- `%player_name%` - имя игрока
- `%player_health%` - здоровье игрока
- `%player_level%` - уровень игрока
- `%server_online%` - количество игроков онлайн
- `%server_max_players%` - максимальное количество игроков на сервере
- `%server_tps%` - TPS сервера
- `%world_name%` - название текущего мира

Полный список плейсхолдеров можно найти на официальном сайте [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders).

## 📝 Примеры настройки

### Простой боссбар
```yaml
worlds:
  world:
    enabled: true
    title: "&aДобро пожаловать на сервер, &e%player_name%&a!"
    color: GREEN
    style: SOLID
    progress-type: STATIC
    progress-value: 1.0
```

### Боссбар с циклическими сообщениями
```yaml
worlds:
  world:
    enabled: true
    titles:
      - "&aДобро пожаловать на &6Super Server&a!"
      - "&eИграйте честно и уважайте других!"
      - "&bОнлайн: &f%server_online%&b/&f%server_max_players%"
      - "&dНаш сайт: &fexample.com"
    title-change-interval: 100
    color: BLUE
    style: SEGMENTED_12
    progress-type: TIME
```

### Боссбар с анимацией цветов и стилей
```yaml
worlds:
  world:
    enabled: true
    title: "<gradient:#ff0000:#00ff00>Супер сервер!</gradient>"
    colors:
      - RED
      - PURPLE
      - BLUE
      - GREEN
      - YELLOW
    styles:
      - SOLID
      - SEGMENTED_6
      - SEGMENTED_10
      - SEGMENTED_20
    animation-enabled: true
    animation-interval: 15
    ping-pong-animation: true
    progress-type: SERVER_TPS
```

## 🛠️ Устранение неполадок

### Боссбар не отображается
1. Убедитесь, что плагин правильно загружен (`/plugins list`)
2. Проверьте, включен ли плагин в конфигурации (`enabled: true`)
3. Убедитесь, что у вас есть необходимые разрешения
4. Проверьте, включен ли боссбар для текущего мира

### Ошибки с PlaceholderAPI
1. Убедитесь, что PlaceholderAPI установлен и активирован
2. Проверьте, установлены ли нужные расширения
3. Включите режим отладки для получения дополнительной информации об ошибках

### Проблемы с производительностью
1. Уменьшите частоту обновления боссбара (увеличьте значение `update-interval`)
2. Отключите анимацию цветов и стилей для миров с большим количеством игроков
3. Включите кэширование плейсхолдеров (`cache-placeholders: true`)
4. Уменьшите количество плейсхолдеров в сообщениях

## 📞 Поддержка

Если у вас возникли проблемы или вопросы по использованию плагина, вы можете:

1. Связаться с автором через [Telegram](https://t.me/Zoobastiks)

## 📜 Лицензия

**Copyright © 2023-2025 Z-oobastik-s**

Все права защищены.

Распространение, модификация или использование кода данного плагина без явного письменного разрешения автора строго запрещены. Любое несанкционированное использование или копирование кода, алгоритмов или концепций, содержащихся в данном плагине, будет преследоваться по закону.

Для получения коммерческой лицензии или разрешения на использование кода обращайтесь к автору.

---

*Сделано с ❤️ Z-oobastik-s*

[GitHub](https://github.com/Z-oobastik-s/Zbossbar)