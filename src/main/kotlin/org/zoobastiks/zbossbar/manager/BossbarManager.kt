package org.zoobastiks.zbossbar.manager

import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.zoobastiks.zbossbar.Zbossbar
import org.zoobastiks.zbossbar.model.WorldBossbarConfig
import org.zoobastiks.zbossbar.utils.TextUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.UUID
import java.util.Calendar

class BossbarManager(private val plugin: Zbossbar) {
    
    private val bossbars: MutableMap<UUID, BossBar> = mutableMapOf()
    private val playerWorlds: MutableMap<UUID, String> = mutableMapOf()
    private val animationTasks: MutableMap<UUID, BukkitTask> = mutableMapOf()
    
    // Состояние анимации для каждого игрока
    private data class AnimationState(
        var colorIndex: Int = 0,
        var styleIndex: Int = 0,
        var titleIndex: Int = 0,
        var ascending: Boolean = true,  // Для пинг-понг анимации
        var tickCount: Int = 0,         // Для отслеживания интервала смены текста
        var lastUpdate: Long = System.currentTimeMillis() // Для отслеживания времени обновления
    )
    
    private val animationStates: MutableMap<UUID, AnimationState> = mutableMapOf()
    
    init {
        startAnimationTask()
    }
    
    /**
     * Создает новый боссбар для игрока в соответствии с его текущим миром
     */
    fun createBossbar(player: Player, config: WorldBossbarConfig): BossBar? {
        if (!config.enabled) {
            return null
        }
        
        // Проверка прав доступа, если указано в настройках
        if (config.permission != null && !player.hasPermission(config.permission)) {
            return null
        }
        
        // Очищаем существующий боссбар, если он есть
        removeBossbar(player)
        
        // Устанавливаем начальный текст
        val rawTitle = if (config.hasMultipleTexts()) {
            config.titles.first()
        } else {
            config.title
        }
        
        // Форматирование текста с плейсхолдерами
        val title = processText(player, rawTitle)
        
        // Создаем новый боссбар с начальными значениями
        val bossbar = Bukkit.createBossBar(
            title,
            config.colors.first(),
            config.styles.first()
        )
        
        // Устанавливаем начальное значение прогресса
        updateProgress(player, bossbar, config)
        
        // Добавляем игрока
        bossbar.addPlayer(player)
        
        // Сохраняем боссбар и информацию о мире
        bossbars[player.uniqueId] = bossbar
        playerWorlds[player.uniqueId] = player.world.name
        
        // Инициализируем анимацию, если включена
        if (config.hasAnyAnimation()) {
            animationStates[player.uniqueId] = AnimationState()
            startAnimation(player, config)
        }
        
        if (plugin.configManager.debugMode) {
            plugin.logger.info("Создан боссбар для игрока ${player.name} в мире ${player.world.name}: $title")
        }
        
        return bossbar
    }
    
    /**
     * Обновляет существующий боссбар игрока
     */
    fun updateBossbar(player: Player, config: WorldBossbarConfig?) {
        val bossbar = bossbars[player.uniqueId] ?: return
        
        if (config == null || !config.enabled) {
            removeBossbar(player)
            return
        }
        
        // Проверка прав доступа, если указано в настройках
        if (config.permission != null && !player.hasPermission(config.permission)) {
            removeBossbar(player)
            return
        }
        
        val state = animationStates[player.uniqueId]
        
        // Если нет состояния анимации, но боссбар поддерживает анимацию, создаем его
        if (state == null && config.hasAnyAnimation()) {
            animationStates[player.uniqueId] = AnimationState()
            startAnimation(player, config)
        }
        
        // Обновляем текущий мир игрока
        playerWorlds[player.uniqueId] = player.world.name
        
        // Обновляем прогресс
        updateProgress(player, bossbar, config)
    }
    
    /**
     * Удаляет боссбар для игрока
     */
    fun removeBossbar(player: Player) {
        val bossbar = bossbars.remove(player.uniqueId)
        bossbar?.removePlayer(player)
        
        // Удаление анимации
        stopAnimation(player)
        animationStates.remove(player.uniqueId)
        
        // Удаление информации о мире
        playerWorlds.remove(player.uniqueId)
        
        if (plugin.configManager.debugMode && bossbar != null) {
            plugin.logger.info("Удален боссбар для игрока ${player.name}")
        }
    }
    
    /**
     * Удаляет все боссбары
     */
    fun removeAllBossbars() {
        for (bossbar in bossbars.values) {
            bossbar.removeAll()
        }
        bossbars.clear()
        playerWorlds.clear()
        animationStates.clear()
        animationTasks.forEach { (_, task) -> task.cancel() }
        animationTasks.clear()
    }
    
    /**
     * Форматирует заголовок боссбара с поддержкой цветов и плейсхолдеров
     */
    private fun formatTitle(player: Player, title: String): String {
        return processText(player, title)
    }
    
    /**
     * Рассчитывает значение прогресса для боссбара
     */
    private fun calculateProgress(player: Player, config: WorldBossbarConfig): Double {
        return when (config.progressType.uppercase()) {
            "STATIC" -> config.progressValue.coerceIn(0.0, 1.0)
            "PLAYER_HEALTH" -> (player.health / player.maxHealth).coerceIn(0.0, 1.0)
            "PLAYER_XP" -> player.exp.toDouble().coerceIn(0.0, 1.0)
            "TPS" -> {
                val tps = Bukkit.getTPS()[0]
                (tps / 20.0).coerceIn(0.0, 1.0)
            }
            "TIME" -> {
                val date = Date()
                if (config.useServerTime) {
                    val format = SimpleDateFormat(config.timeFormat)
                    val timeString = format.format(date)
                    
                    // Если формат содержит часы, то делаем прогресс на основе часов
                    if (config.timeFormat.contains("HH") || config.timeFormat.contains("hh")) {
                        val calendar = java.util.Calendar.getInstance()
                        calendar.time = date
                        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(java.util.Calendar.MINUTE)
                        val second = calendar.get(java.util.Calendar.SECOND)
                        
                        val totalSeconds = hour * 3600 + minute * 60 + second
                        val daySeconds = 24 * 3600
                        
                        (totalSeconds.toDouble() / daySeconds).coerceIn(0.0, 1.0)
                    } else {
                        0.5 // Если формат не содержит часы, используем фиксированное значение
                    }
                } else {
                    0.5 // Если не используется серверное время, используем фиксированное значение
                }
            }
            else -> 1.0 // По умолчанию полный прогресс
        }
    }
    
    /**
     * Запускает задачу анимации для боссбаров
     */
    private fun startAnimationTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val now = System.currentTimeMillis()
            
            val iterator = animationStates.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val uuid = entry.key
                val state = entry.value
                
                val player = Bukkit.getPlayer(uuid) ?: continue
                val bb = bossbars[uuid] ?: continue
                val worldConfig = plugin.configManager.getWorldConfig(player.world) ?: continue
                
                // Если нет ни одной анимации, удаляем состояние
                val hasMultipleTexts = worldConfig.titles.size > 1
                val hasColorAnimation = worldConfig.animationEnabled && worldConfig.colors.size > 1
                val hasStyleAnimation = worldConfig.animationEnabled && worldConfig.styles.size > 1
                
                if (!hasMultipleTexts && !hasColorAnimation && !hasStyleAnimation) {
                    iterator.remove()
                    continue
                }
                
                // Обновляем заголовок если есть несколько
                if (hasMultipleTexts && now - state.lastUpdate >= worldConfig.titleChangeInterval * 50) {
                    state.titleIndex = (state.titleIndex + 1) % worldConfig.titles.size
                    val title = formatTitle(player, worldConfig.titles[state.titleIndex])
                    bb.setTitle(title)
                    state.lastUpdate = now
                }
                
                // Обновляем цвет и стиль, если анимация включена
                if (worldConfig.animationEnabled && now - state.lastUpdate >= worldConfig.animationInterval * 50) {
                    // Обновляем цвет если есть несколько цветов
                    if (hasColorAnimation) {
                        if (worldConfig.pingPongAnimation) {
                            // Пинг-понг анимация цветов
                            if (state.ascending) {
                                if (state.colorIndex < worldConfig.colors.size - 1) {
                                    state.colorIndex++
                                } else {
                                    state.ascending = false
                                }
                            } else {
                                if (state.colorIndex > 0) {
                                    state.colorIndex--
                                } else {
                                    state.ascending = true
                                }
                            }
                        } else if (worldConfig.reverseAnimation) {
                            // Обратная анимация
                            state.colorIndex = (state.colorIndex - 1 + worldConfig.colors.size) % worldConfig.colors.size
                        } else {
                            // Стандартная анимация
                            state.colorIndex = (state.colorIndex + 1) % worldConfig.colors.size
                        }
                        bb.color = worldConfig.colors[state.colorIndex]
                    }
                    
                    // Обновляем стиль если есть несколько стилей
                    if (hasStyleAnimation) {
                        if (worldConfig.pingPongAnimation) {
                            // Пинг-понг анимация стилей
                            if (state.ascending) {
                                if (state.styleIndex < worldConfig.styles.size - 1) {
                                    state.styleIndex++
                                } else {
                                    state.ascending = false
                                }
                            } else {
                                if (state.styleIndex > 0) {
                                    state.styleIndex--
                                } else {
                                    state.ascending = true
                                }
                            }
                        } else if (worldConfig.reverseAnimation) {
                            // Обратная анимация
                            state.styleIndex = (state.styleIndex - 1 + worldConfig.styles.size) % worldConfig.styles.size
                        } else {
                            // Стандартная анимация
                            state.styleIndex = (state.styleIndex + 1) % worldConfig.styles.size
                        }
                        bb.style = worldConfig.styles[state.styleIndex]
                    }
                }
            }
        }, 1L, 1L) // Проверяем каждый тик для плавной анимации
    }
    
    private fun startAnimation(player: Player, config: WorldBossbarConfig) {
        stopAnimation(player) // Останавливаем существующую анимацию
        
        if (!config.hasAnyAnimation()) {
            return
        }
        
        val uuid = player.uniqueId
        val bb = bossbars[uuid] ?: return
        
        // Создаем задачу анимации
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val state = animationStates[uuid] ?: return@Runnable
            val cb = bossbars[uuid]
            
            if (cb == null || !player.isOnline) {
                stopAnimation(player)
                return@Runnable
            }
            
            var titleChanged = false
            
            // Обработка смены текста
            if (config.hasMultipleTexts()) {
                state.tickCount++
                
                if (state.tickCount >= config.titleChangeInterval) {
                    state.tickCount = 0
                    state.titleIndex = (state.titleIndex + 1) % config.titles.size
                    titleChanged = true
                }
            }
            
            // Обработка анимации цветов, если включена
            if (config.hasColorAnimation()) {
                if (config.pingPongAnimation) {
                    // Пинг-понг анимация цветов
                    if (state.ascending) {
                        if (state.colorIndex < config.colors.size - 1) {
                            state.colorIndex++
                        } else {
                            state.ascending = false
                        }
                    } else {
                        if (state.colorIndex > 0) {
                            state.colorIndex--
                        } else {
                            state.ascending = true
                        }
                    }
                } else if (config.reverseAnimation) {
                    // Обратная анимация
                    state.colorIndex = (state.colorIndex - 1 + config.colors.size) % config.colors.size
                } else {
                    // Стандартная анимация
                    state.colorIndex = (state.colorIndex + 1) % config.colors.size
                }
                
                cb.color = config.colors[state.colorIndex]
            }
            
            // Обработка анимации стилей, если включена
            if (config.hasStyleAnimation()) {
                if (config.pingPongAnimation) {
                    // Пинг-понг анимация стилей
                    if (state.ascending) {
                        if (state.styleIndex < config.styles.size - 1) {
                            state.styleIndex++
                        } else {
                            state.ascending = false
                        }
                    } else {
                        if (state.styleIndex > 0) {
                            state.styleIndex--
                        } else {
                            state.ascending = true
                        }
                    }
                } else if (config.reverseAnimation) {
                    // Обратная анимация
                    state.styleIndex = (state.styleIndex - 1 + config.styles.size) % config.styles.size
                } else {
                    // Стандартная анимация
                    state.styleIndex = (state.styleIndex + 1) % config.styles.size
                }
                
                cb.style = config.styles[state.styleIndex]
            }
            
            // Если текст изменился, обновляем его с плейсхолдерами
            if (titleChanged) {
                val rawTitle = config.titles[state.titleIndex]
                cb.setTitle(processText(player, rawTitle))
            }
            
        }, config.animationInterval.toLong(), config.animationInterval.toLong())
        
        // Сохраняем задачу
        animationTasks[uuid] = task
    }
    
    private fun stopAnimation(player: Player) {
        val uuid = player.uniqueId
        val task = animationTasks.remove(uuid)
        task?.cancel()
    }
    
    private fun processText(player: Player, text: String): String {
        // Используем TextUtils для форматирования
        var processedText = TextUtils.formatText(text)
        
        // Применяем PlaceholderAPI если доступен
        if (plugin.configManager.usePlaceholders && plugin.placeholderAPI != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                // Используем reflection для вызова метода setPlaceholders
                val placeholderClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI")
                val setPlaceholders = placeholderClass.getMethod("setPlaceholders", Player::class.java, String::class.java)
                processedText = setPlaceholders.invoke(null, player, processedText) as String
            } catch (e: Exception) {
                if (plugin.configManager.debugMode) {
                    plugin.logger.warning("Ошибка при обработке плейсхолдеров: ${e.message}")
                }
            }
        }
        
        return processedText
    }
    
    fun onPlayerLeave(player: Player) {
        // Очищаем всю информацию и отменяем задачи для вышедшего игрока
        stopAnimation(player)
        animationStates.remove(player.uniqueId)
        bossbars.remove(player.uniqueId)
        playerWorlds.remove(player.uniqueId)
    }
    
    fun getBossbarsCount(): Int {
        return bossbars.size
    }
    
    fun updateAllBossbars() {
        // Обновляем боссбары для всех игроков
        for (player in Bukkit.getOnlinePlayers()) {
            val bossbar = bossbars[player.uniqueId] ?: continue
            val worldName = playerWorlds[player.uniqueId] ?: continue
            
            // Проверяем, изменился ли мир игрока
            if (player.world.name != worldName) {
                // Если включены плавные переходы между мирами
                if (plugin.configManager.smoothWorldTransitions) {
                    // Планируем создание нового боссбара через указанную задержку
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        val config = plugin.configManager.getWorldConfig(player.world)
                        if (config != null && config.enabled && 
                            (config.permission == null || player.hasPermission(config.permission))) {
                            createBossbar(player, config)
                        } else {
                            removeBossbar(player)
                        }
                    }, plugin.configManager.worldChangeDelay)
                } else {
                    // Обрабатываем изменение мира сразу
                    val config = plugin.configManager.getWorldConfig(player.world)
                    if (config != null && config.enabled && 
                        (config.permission == null || player.hasPermission(config.permission))) {
                        createBossbar(player, config)
                    } else {
                        removeBossbar(player)
                    }
                }
            } else {
                // Обновляем существующий боссбар
                val config = plugin.configManager.getWorldConfig(player.world)
                updateBossbar(player, config)
            }
        }
    }
    
    // Для обновления видимости боссбара для всех игроков
    fun updateBossbarVisibility(visible: Boolean) {
        for (bossbar in bossbars.values) {
            bossbar.isVisible = visible
        }
    }
    
    fun hasActiveBossbar(player: Player): Boolean {
        return bossbars.containsKey(player.uniqueId)
    }
    
    fun getPlayerBossbar(player: Player): BossBar? {
        return bossbars[player.uniqueId]
    }
    
    fun getPlayerWorld(player: Player): String? {
        return playerWorlds[player.uniqueId]
    }
    
    private fun updateProgress(player: Player, bossbar: BossBar, config: WorldBossbarConfig) {
        when (config.progressType.uppercase()) {
            "STATIC" -> {
                bossbar.progress = config.progressValue.coerceIn(0.0, 1.0)
            }
            "PLAYER_HEALTH" -> {
                val health = player.health
                val maxHealth = player.maxHealth
                bossbar.progress = (health / maxHealth).coerceIn(0.0, 1.0)
            }
            "PLAYER_LEVEL" -> {
                val level = player.level.toDouble()
                val maxLevel = config.progressMaxValue.coerceAtLeast(1.0)
                bossbar.progress = (level / maxLevel).coerceIn(0.0, 1.0)
            }
            "PLAYER_EXP" -> {
                bossbar.progress = player.exp.toDouble().coerceIn(0.0, 1.0)
            }
            "SERVER_TPS" -> {
                val tps = Bukkit.getTPS()[0]
                bossbar.progress = (tps / 20.0).coerceIn(0.0, 1.0)
            }
            "TIME" -> {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                bossbar.progress = (hour.toDouble() / 24.0).coerceIn(0.0, 1.0)
            }
            "CUSTOM" -> {
                // Если установлен плейсхолдер, используем его
                val placeholder = config.progressPlaceholder
                if (placeholder.isNotEmpty() && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    try {
                        // Используем reflection для вызова PlaceholderAPI
                        val placeholderClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI")
                        val setPlaceholders = placeholderClass.getMethod("setPlaceholders", Player::class.java, String::class.java)
                        val processedValue = setPlaceholders.invoke(null, player, placeholder) as String
                        val value = processedValue.toDoubleOrNull()
                        
                        if (value != null) {
                            val maxValue = config.progressMaxValue.coerceAtLeast(1.0)
                            bossbar.progress = (value / maxValue).coerceIn(0.0, 1.0)
                        } else {
                            bossbar.progress = 1.0
                        }
                    } catch (e: Exception) {
                        if (plugin.configManager.debugMode) {
                            plugin.logger.warning("Ошибка при обработке плейсхолдера прогресса: ${e.message}")
                        }
                        bossbar.progress = 1.0
                    }
                } else {
                    bossbar.progress = 1.0
                }
            }
            else -> {
                bossbar.progress = 1.0
            }
        }
    }
} 