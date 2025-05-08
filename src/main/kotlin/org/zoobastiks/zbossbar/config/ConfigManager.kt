package org.zoobastiks.zbossbar.config

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.zoobastiks.zbossbar.Zbossbar
import org.zoobastiks.zbossbar.model.WorldBossbarConfig
import java.util.EnumMap
import java.util.logging.Level

class ConfigManager(private val plugin: Zbossbar) {
    
    lateinit var config: FileConfiguration
        private set
    
    // Основные настройки
    var savePlayerPreferences: Boolean = true
        private set
    
    var usePlaceholders: Boolean = true
        private set
    
    var smoothWorldTransitions: Boolean = true
        private set
    
    var missingWorldBehavior: String = "DEFAULT"
        private set
    
    var maxViewDistance: Int = -1
        private set
    
    var joinDelay: Long = 10L
        private set
    
    var worldChangeDelay: Long = 5L
        private set
    
    var autoSaveInterval: Int = 5
        private set
    
    var cachePlaceholders: Boolean = false
        private set
    
    var placeholderCacheTime: Int = 3
        private set
    
    var debugMode: Boolean = false
        private set
    
    var bossbarsPerPlayerLimit: Int = 3
        private set
    
    private val worldConfigs: MutableMap<String, WorldBossbarConfig> = mutableMapOf()
    
    fun loadConfig() {
        plugin.saveDefaultConfig()
        
        plugin.reloadConfig()
        config = plugin.config
        
        // Загрузка основных настроек
        savePlayerPreferences = config.getBoolean("save-player-preferences", true)
        usePlaceholders = config.getBoolean("use-placeholders", true)
        smoothWorldTransitions = config.getBoolean("smooth-world-transitions", true)
        missingWorldBehavior = config.getString("missing-world-behavior", "DEFAULT") ?: "DEFAULT"
        maxViewDistance = config.getInt("max-view-distance", -1)
        joinDelay = config.getLong("join-delay", 10L)
        worldChangeDelay = config.getLong("world-change-delay", 5L)
        autoSaveInterval = config.getInt("auto-save-interval", 5)
        cachePlaceholders = config.getBoolean("cache-placeholders", false)
        placeholderCacheTime = config.getInt("placeholder-cache-time", 3)
        debugMode = config.getBoolean("debug-mode", false)
        bossbarsPerPlayerLimit = config.getInt("bossbars-per-player-limit", 3)
        
        loadWorldConfigs()
        
        if (debugMode) {
            plugin.logger.info("Конфигурация загружена:")
            plugin.logger.info("- Миров с боссбарами: ${worldConfigs.size}")
            plugin.logger.info("- Сохранение предпочтений игроков: $savePlayerPreferences")
            plugin.logger.info("- Интервал автосохранения: $autoSaveInterval минут")
            plugin.logger.info("- Кэширование плейсхолдеров: $cachePlaceholders (время: $placeholderCacheTime сек)")
        }
    }
    
    fun saveConfig() {
        plugin.saveConfig()
    }
    
    fun isPluginEnabled(): Boolean {
        return config.getBoolean("enabled", true)
    }
    
    fun getUpdateInterval(): Int {
        return config.getInt("update-interval", 20)
    }
    
    fun getWorldConfig(world: World): WorldBossbarConfig? {
        val worldName = world.name
        
        // Если конфигурация для мира уже существует, возвращаем её
        if (worldConfigs.containsKey(worldName)) {
            return worldConfigs[worldName]
        }
        
        // Если конфигурации нет, действуем в соответствии с настройкой missingWorldBehavior
        return when (missingWorldBehavior.uppercase()) {
            "NONE" -> null
            "CREATE" -> {
                // Создаем новую конфигурацию для мира
                val defaultConfig = createDefaultWorldConfig(worldName)
                worldConfigs[worldName] = defaultConfig
                
                // Сохраняем в конфигурацию
                saveWorldConfig(worldName, defaultConfig)
                
                defaultConfig
            }
            else -> { // DEFAULT
                // Ищем конфигурацию для мира world
                val defaultWorld = worldConfigs["world"] ?: createDefaultWorldConfig("world")
                if (!worldConfigs.containsKey("world")) {
                    worldConfigs["world"] = defaultWorld
                    saveWorldConfig("world", defaultWorld)
                }
                
                defaultWorld
            }
        }
    }
    
    private fun loadWorldConfigs() {
        worldConfigs.clear()
        
        val worldsSection = config.getConfigurationSection("worlds")
        if (worldsSection == null) {
            plugin.logger.warning("Секция 'worlds' не найдена в конфигурации. Используются настройки по умолчанию.")
            val defaultWorld = createDefaultWorldConfig("world")
            worldConfigs["world"] = defaultWorld
            saveWorldConfig("world", defaultWorld)
            return
        }
        
        for (worldName in worldsSection.getKeys(false)) {
            val worldSection = worldsSection.getConfigurationSection(worldName)
            if (worldSection != null) {
                try {
                    val worldConfig = parseWorldConfig(worldSection)
                    worldConfigs[worldName] = worldConfig
                    
                    if (debugMode) {
                        plugin.logger.info("Загружена конфигурация для мира $worldName: ${worldConfig.title}")
                    }
                } catch (e: Exception) {
                    plugin.logger.log(Level.WARNING, "Ошибка при загрузке конфигурации для мира $worldName", e)
                }
            }
        }
        
        // Если нет конфигурации для основного мира, создаем её
        if (!worldConfigs.containsKey("world")) {
            val defaultWorld = createDefaultWorldConfig("world")
            worldConfigs["world"] = defaultWorld
            saveWorldConfig("world", defaultWorld)
        }
    }
    
    private fun parseWorldConfig(section: ConfigurationSection): WorldBossbarConfig {
        // Основные настройки
        val enabled = section.getBoolean("enabled", true)
        val title = section.getString("title", "<gradient:#FF0000:#A6EB0F>〔Zbossbar〕</gradient> &fДобро пожаловать!")
        
        // Получаем список заголовков
        val titles = mutableListOf<String>()
        if (section.isList("titles")) {
            titles.addAll(section.getStringList("titles"))
        } else if (section.isString("title")) {
            titles.add(title ?: "<gradient:#FF0000:#A6EB0F>〔Zbossbar〕</gradient> &fДобро пожаловать!")
        }
        
        // Интервал смены заголовков
        val titleChangeInterval = section.getInt("title-change-interval", 100)
        
        // Получаем цвет/цвета
        val colors = mutableListOf<BarColor>()
        if (section.isString("color")) {
            try {
                val colorStr = section.getString("color", "WHITE") ?: "WHITE"
                colors.add(BarColor.valueOf(colorStr.uppercase()))
            } catch (e: IllegalArgumentException) {
                colors.add(BarColor.WHITE)
                plugin.logger.warning("Неверный цвет боссбара: ${section.getString("color")}. Используется WHITE.")
            }
        } else if (section.isList("colors")) {
            for (colorStr in section.getStringList("colors")) {
                try {
                    colors.add(BarColor.valueOf(colorStr.uppercase()))
                } catch (e: IllegalArgumentException) {
                    plugin.logger.warning("Неверный цвет боссбара: $colorStr. Пропущен.")
                }
            }
            if (colors.isEmpty()) {
                colors.add(BarColor.WHITE)
            }
        } else {
            colors.add(BarColor.WHITE)
        }
        
        // Получаем стиль/стили
        val styles = mutableListOf<BarStyle>()
        if (section.isString("style")) {
            try {
                val styleStr = section.getString("style", "SOLID") ?: "SOLID"
                styles.add(BarStyle.valueOf(styleStr.uppercase()))
            } catch (e: IllegalArgumentException) {
                styles.add(BarStyle.SOLID)
                plugin.logger.warning("Неверный стиль боссбара: ${section.getString("style")}. Используется SOLID.")
            }
        } else if (section.isList("styles")) {
            for (styleStr in section.getStringList("styles")) {
                try {
                    styles.add(BarStyle.valueOf(styleStr.uppercase()))
                } catch (e: IllegalArgumentException) {
                    plugin.logger.warning("Неверный стиль боссбара: $styleStr. Пропущен.")
                }
            }
            if (styles.isEmpty()) {
                styles.add(BarStyle.SOLID)
            }
        } else {
            styles.add(BarStyle.SOLID)
        }
        
        // Настройки анимации
        val animationSection = section.getConfigurationSection("animation")
        val animationEnabled = animationSection?.getBoolean("enabled", false) ?: false
        val animationInterval = animationSection?.getInt("interval", 20) ?: 20
        val reverseAnimation = animationSection?.getBoolean("reverse", false) ?: false
        val pingPongAnimation = animationSection?.getBoolean("ping-pong", false) ?: false
        
        // Настройки прогресса
        val progressSection = section.getConfigurationSection("progress")
        val progressType = progressSection?.getString("type", "STATIC") ?: "STATIC"
        val progressValue = progressSection?.getDouble("value", 1.0) ?: 1.0
        val progressMaxValue = progressSection?.getDouble("max-value", 100.0) ?: 100.0
        val progressPlaceholder = progressSection?.getString("placeholder", "") ?: ""
        
        val timeFormat = progressSection?.getConfigurationSection("time")?.getString("format", "HH:mm:ss") ?: "HH:mm:ss"
        val useServerTime = progressSection?.getConfigurationSection("time")?.getBoolean("server-time", true) ?: true
        
        // Дополнительные настройки
        val overlayPrevious = section.getBoolean("overlay-previous", false)
        val permission = section.getString("permission")
        val showOnJoin = section.getBoolean("show-on-join", true)
        
        return WorldBossbarConfig(
            enabled,
            title ?: "<gradient:#FF0000:#A6EB0F>〔Zbossbar〕</gradient> &fДобро пожаловать!",
            if (titles.isEmpty()) listOf(title ?: "<gradient:#FF0000:#A6EB0F>〔Zbossbar〕</gradient> &fДобро пожаловать!") else titles,
            titleChangeInterval,
            colors,
            styles,
            animationEnabled,
            animationInterval,
            reverseAnimation,
            pingPongAnimation,
            progressType,
            progressValue,
            progressMaxValue,
            progressPlaceholder,
            timeFormat,
            useServerTime,
            overlayPrevious,
            permission,
            showOnJoin
        )
    }
    
    private fun createDefaultWorldConfig(worldName: String): WorldBossbarConfig {
        return WorldBossbarConfig(
            true,
            "<gradient:#FF0000:#A6EB0F>〔Zbossbar〕</gradient> &fМир: &e$worldName",
            listOf(
                "<gradient:#FF0000:#A6EB0F>〔Zbossbar〕</gradient> &fМир: &e$worldName",
                "&eДобро пожаловать на сервер!",
                "&bИспользуйте &6/bossbar &bдля управления"
            ),
            100,
            mutableListOf(BarColor.WHITE),
            mutableListOf(BarStyle.SOLID),
            false,
            20,
            false,
            false,
            "STATIC",
            1.0,
            100.0,
            "",
            "HH:mm:ss",
            true,
            false,
            null,
            true
        )
    }
    
    private fun saveWorldConfig(worldName: String, worldConfig: WorldBossbarConfig) {
        config.set("worlds.$worldName.enabled", worldConfig.enabled)
        config.set("worlds.$worldName.title", worldConfig.title)
        
        // Сохраняем список заголовков
        if (worldConfig.titles.size > 1) {
            config.set("worlds.$worldName.titles", worldConfig.titles)
            config.set("worlds.$worldName.title-change-interval", worldConfig.titleChangeInterval)
        }
        
        // Сохраняем цвет/цвета
        if (worldConfig.colors.size == 1) {
            config.set("worlds.$worldName.color", worldConfig.colors[0].name)
        } else {
            config.set("worlds.$worldName.colors", worldConfig.colors.map { it.name })
        }
        
        // Сохраняем стиль/стили
        if (worldConfig.styles.size == 1) {
            config.set("worlds.$worldName.style", worldConfig.styles[0].name)
        } else {
            config.set("worlds.$worldName.styles", worldConfig.styles.map { it.name })
        }
        
        // Сохраняем настройки анимации
        config.set("worlds.$worldName.animation.enabled", worldConfig.animationEnabled)
        config.set("worlds.$worldName.animation.interval", worldConfig.animationInterval)
        config.set("worlds.$worldName.animation.reverse", worldConfig.reverseAnimation)
        config.set("worlds.$worldName.animation.ping-pong", worldConfig.pingPongAnimation)
        
        // Сохраняем настройки прогресса
        config.set("worlds.$worldName.progress.type", worldConfig.progressType)
        if (worldConfig.progressType == "STATIC") {
            config.set("worlds.$worldName.progress.value", worldConfig.progressValue)
        } else if (worldConfig.progressType == "CUSTOM") {
            config.set("worlds.$worldName.progress.placeholder", worldConfig.progressPlaceholder)
            config.set("worlds.$worldName.progress.max-value", worldConfig.progressMaxValue)
        } else if (worldConfig.progressType == "PLAYER_LEVEL") {
            config.set("worlds.$worldName.progress.max-value", worldConfig.progressMaxValue)
        } else if (worldConfig.progressType == "TIME") {
            config.set("worlds.$worldName.progress.time.format", worldConfig.timeFormat)
            config.set("worlds.$worldName.progress.time.server-time", worldConfig.useServerTime)
        }
        
        // Сохраняем дополнительные настройки
        if (worldConfig.overlayPrevious) {
            config.set("worlds.$worldName.overlay-previous", worldConfig.overlayPrevious)
        }
        if (worldConfig.permission != null) {
            config.set("worlds.$worldName.permission", worldConfig.permission)
        }
        if (!worldConfig.showOnJoin) {
            config.set("worlds.$worldName.show-on-join", worldConfig.showOnJoin)
        }
        
        saveConfig()
    }
    
    fun getMessage(key: String): String {
        val prefix = config.getString("messages.prefix", "&8[&b&lZbossbar&8] ")
        val message = config.getString("messages.$key", "&cСообщение не найдено: $key")
        return "$prefix$message"
    }
    
    fun getMessageWithoutPrefix(key: String): String {
        return config.getString("messages.$key", "&cСообщение не найдено: $key") ?: "&cСообщение не найдено: $key"
    }
} 