package org.zoobastiks.zbossbar

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.zoobastiks.zbossbar.command.BossbarCommand
import org.zoobastiks.zbossbar.command.ZbossbarCommand
import org.zoobastiks.zbossbar.config.ConfigManager
import org.zoobastiks.zbossbar.manager.BossbarManager
import org.zoobastiks.zbossbar.manager.UserDataManager
import java.util.logging.Level

class Zbossbar : JavaPlugin(), Listener {
    
    lateinit var configManager: ConfigManager
        private set
    
    lateinit var bossbarManager: BossbarManager
        private set
    
    lateinit var userDataManager: UserDataManager
        private set
    
    // Ссылка на PlaceholderAPI
    var placeholderAPI: Any? = null
        private set
    
    private var updateTask: BukkitTask? = null
    private var saveTask: BukkitTask? = null
    
    override fun onEnable() {
        // Проверяем совместимость сервера
        if (!checkServerCompatibility()) {
            logger.severe("Этот плагин требует Paper API. Отключение...")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        
        // Инициализируем менеджеры
        configManager = ConfigManager(this)
        bossbarManager = BossbarManager(this)
        userDataManager = UserDataManager(this)
        
        // Загружаем настройки
        configManager.loadConfig()
        
        // Загружаем данные пользователей
        try {
            userDataManager.loadUserData()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Ошибка при загрузке данных пользователей", e)
        }
        
        // Проверяем наличие и подключаем PlaceholderAPI, если доступен
        setupPlaceholderAPI()
        
        // Регистрируем команды
        getCommand("bossbar")?.setExecutor(BossbarCommand(this))
        getCommand("zbossbar")?.setExecutor(ZbossbarCommand(this))
        
        // Регистрируем слушатели событий
        server.pluginManager.registerEvents(this, this)
        
        // Запускаем задачу обновления боссбаров
        startUpdateTask()
        
        // Запускаем задачу автосохранения данных пользователей, если включено
        if (configManager.savePlayerPreferences && configManager.autoSaveInterval > 0) {
            startSaveTask()
        }
        
        // Запускаем боссбары для всех онлайн-игроков при перезагрузке сервера
        Bukkit.getScheduler().runTaskLater(this, Runnable {
            for (player in server.onlinePlayers) {
                setupPlayerBossbar(player)
            }
            
            // Логируем инфо о запуске
            logger.info("Zbossbar запущен успешно! Версия: ${description.version}")
            if (configManager.debugMode) {
                logger.info("Режим отладки активирован")
                logger.info("Состояние плагина: ${if(configManager.isPluginEnabled()) "Включен" else "Выключен"}")
                logger.info("PlaceholderAPI: ${if(placeholderAPI != null) "Найден" else "Не найден"}")
            }
        }, 20L) // Задержка в 1 секунду для уверенности, что все плагины загружены
    }
    
    override fun onDisable() {
        // Остановить задачи
        updateTask?.cancel()
        saveTask?.cancel()
        
        // Сохранить данные пользователей
        try {
            userDataManager.saveUserData()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Ошибка при сохранении данных пользователей", e)
        }
        
        // Удалить все боссбары
        bossbarManager.removeAllBossbars()
        
        logger.info("Zbossbar остановлен.")
    }
    
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        
        // Задержка для загрузки данных игрока
        Bukkit.getScheduler().runTaskLater(this, Runnable {
            setupPlayerBossbar(player)
        }, configManager.joinDelay)
    }
    
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        
        // Сохраняем данные игрока и удаляем боссбар
        if (configManager.savePlayerPreferences) {
            userDataManager.savePlayerPreference(player)
        }
        
        bossbarManager.onPlayerLeave(player)
    }
    
    @EventHandler
    fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
        val player = event.player
        val fromWorld = event.from
        
        if (configManager.debugMode) {
            logger.info("Игрок ${player.name} сменил мир с ${fromWorld.name} на ${player.world.name}")
        }
        
        // Обновление боссбара будет обработано в задаче обновления
        // или сразу, если гладкие переходы отключены
        if (!configManager.smoothWorldTransitions) {
            val config = configManager.getWorldConfig(player.world)
            if (config != null && config.enabled) {
                bossbarManager.createBossbar(player, config)
            } else {
                bossbarManager.removeBossbar(player)
            }
        }
    }
    
    fun reloadPlugin() {
        // Остановить задачи
        updateTask?.cancel()
        saveTask?.cancel()
        
        // Сохранить данные пользователей перед перезагрузкой
        try {
            userDataManager.saveUserData()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Ошибка при сохранении данных пользователей перед перезагрузкой", e)
        }
        
        // Перезагружаем настройки
        configManager.loadConfig()
        
        // Удаляем все боссбары
        bossbarManager.removeAllBossbars()
        
        // Перезагружаем пользовательские данные
        try {
            userDataManager.loadUserData()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Ошибка при перезагрузке данных пользователей", e)
        }
        
        // Перезапускаем задачи
        startUpdateTask()
        
        if (configManager.savePlayerPreferences && configManager.autoSaveInterval > 0) {
            startSaveTask()
        }
        
        // Создаем боссбары заново для всех игроков
        for (player in server.onlinePlayers) {
            setupPlayerBossbar(player)
        }
        
        if (configManager.debugMode) {
            logger.info("Плагин перезагружен успешно!")
        }
    }
    
    private fun setupPlayerBossbar(player: Player) {
        // Если плагин отключен, не показываем боссбар
        if (!configManager.isPluginEnabled()) {
            return
        }
        
        // Проверяем, не отключил ли игрок боссбар
        if (configManager.savePlayerPreferences && !userDataManager.isEnabled(player)) {
            return
        }
        
        // Получаем конфигурацию для мира
        val config = configManager.getWorldConfig(player.world)
        if (config != null && config.enabled) {
            // Проверяем показывать ли босбар при входе
            if (!config.showOnJoin) {
                return
            }
            
            // Проверяем права доступа если указаны
            if (config.permission != null && !player.hasPermission(config.permission)) {
                return
            }
            
            // Создаем боссбар
            bossbarManager.createBossbar(player, config)
        }
    }
    
    private fun startUpdateTask() {
        updateTask?.cancel()
        
        // Запускаем задачу обновления боссбаров
        val updateInterval = configManager.getUpdateInterval().toLong()
        updateTask = Bukkit.getScheduler().runTaskTimer(this, Runnable {
            bossbarManager.updateAllBossbars()
        }, updateInterval, updateInterval)
    }
    
    private fun startSaveTask() {
        saveTask?.cancel()
        
        // Запускаем задачу сохранения данных пользователей
        val saveIntervalTicks = configManager.autoSaveInterval * 60 * 20L // минуты -> тики
        saveTask = Bukkit.getScheduler().runTaskTimer(this, Runnable {
            try {
                if (configManager.debugMode) {
                    logger.info("Выполняется автосохранение данных пользователей...")
                }
                userDataManager.saveUserData()
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Ошибка при автосохранении данных пользователей", e)
            }
        }, saveIntervalTicks, saveIntervalTicks)
    }
    
    private fun setupPlaceholderAPI(): Boolean {
        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            placeholderAPI = server.pluginManager.getPlugin("PlaceholderAPI")
            if (configManager.debugMode) {
                logger.info("PlaceholderAPI найден и подключен!")
            }
            return true
        }
        
        if (configManager.debugMode) {
            logger.warning("PlaceholderAPI не найден. Плейсхолдеры не будут работать.")
        }
        return false
    }
    
    private fun checkServerCompatibility(): Boolean {
        try {
            // Проверяем наличие Paper API
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent")
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }
    }
    
    fun togglePluginEnabled(): Boolean {
        val newState = !configManager.isPluginEnabled()
        
        if (newState) {
            // Включение плагина
            for (player in server.onlinePlayers) {
                setupPlayerBossbar(player)
            }
            startUpdateTask()
            
            if (configManager.savePlayerPreferences && configManager.autoSaveInterval > 0 && saveTask == null) {
                startSaveTask()
            }
        } else {
            // Отключение плагина
            bossbarManager.removeAllBossbars()
            updateTask?.cancel()
            updateTask = null
        }
        
        return newState
    }
    
    fun togglePlayerBossbar(player: Player): Boolean {
        if (configManager.savePlayerPreferences) {
            val currentState = userDataManager.isEnabled(player)
            val newState = !currentState
            
            userDataManager.setEnabled(player, newState)
            
            if (newState) {
                // Включаем боссбар
                val config = configManager.getWorldConfig(player.world)
                if (config != null && config.enabled) {
                    bossbarManager.createBossbar(player, config)
                }
            } else {
                // Отключаем боссбар
                bossbarManager.removeBossbar(player)
            }
            
            return newState
        } else {
            // Если сохранение предпочтений отключено, просто переключаем состояние
            val hasActiveBossbar = bossbarManager.hasActiveBossbar(player)
            
            if (hasActiveBossbar) {
                bossbarManager.removeBossbar(player)
                return false
            } else {
                val config = configManager.getWorldConfig(player.world)
                if (config != null && config.enabled) {
                    bossbarManager.createBossbar(player, config)
                    return true
                }
                return false
            }
        }
    }
} 