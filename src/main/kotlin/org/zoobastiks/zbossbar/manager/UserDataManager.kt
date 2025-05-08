package org.zoobastiks.zbossbar.manager

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.zoobastiks.zbossbar.Zbossbar
import java.io.File
import java.util.UUID

class UserDataManager(private val plugin: Zbossbar) {
    
    private val disabledPlayers = mutableSetOf<UUID>()
    private lateinit var userdataFile: File
    private lateinit var userdataConfig: FileConfiguration
    
    init {
        // Создаем директорию для данных, если она не существует
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        
        // Инициализируем файл userdata.yml
        userdataFile = File(plugin.dataFolder, "userdata.yml")
        
        if (!userdataFile.exists()) {
            try {
                userdataFile.createNewFile()
            } catch (e: Exception) {
                plugin.logger.severe("Не удалось создать userdata.yml: ${e.message}")
            }
        }
        
        userdataConfig = YamlConfiguration.loadConfiguration(userdataFile)
    }
    
    /**
     * Загружает данные пользователей из файла
     */
    fun loadUserData() {
        disabledPlayers.clear()
        
        val disabledList = userdataConfig.getStringList("disabled-players")
        for (uuidStr in disabledList) {
            try {
                val uuid = UUID.fromString(uuidStr)
                disabledPlayers.add(uuid)
            } catch (e: IllegalArgumentException) {
                plugin.logger.warning("Неверный UUID в файле userdata.yml: $uuidStr")
            }
        }
        
        if (plugin.configManager.debugMode) {
            plugin.logger.info("Загружены данные пользователей: ${disabledPlayers.size} игроков отключили боссбар")
        }
    }
    
    /**
     * Сохраняет данные пользователей в файл
     */
    fun saveUserData() {
        val disabledList = disabledPlayers.map { it.toString() }
        userdataConfig.set("disabled-players", disabledList)
        
        try {
            userdataConfig.save(userdataFile)
            if (plugin.configManager.debugMode) {
                plugin.logger.info("Данные пользователей сохранены: ${disabledPlayers.size} игроков")
            }
        } catch (e: Exception) {
            plugin.logger.severe("Ошибка при сохранении userdata.yml: ${e.message}")
        }
    }
    
    /**
     * Сохраняет предпочтения конкретного игрока
     */
    fun savePlayerPreference(player: Player) {
        // Просто вызываем общее сохранение, так как у нас только один параметр (enabled/disabled)
        saveUserData()
    }
    
    /**
     * Проверяет, включен ли боссбар для игрока
     */
    fun isEnabled(player: Player): Boolean {
        return !disabledPlayers.contains(player.uniqueId)
    }
    
    /**
     * Устанавливает состояние боссбара для игрока
     */
    fun setEnabled(player: Player, enabled: Boolean) {
        if (enabled) {
            disabledPlayers.remove(player.uniqueId)
        } else {
            disabledPlayers.add(player.uniqueId)
        }
        
        // Если включено автосохранение, сохраняем изменения сразу
        if (plugin.configManager.debugMode) {
            plugin.logger.info("Игрок ${player.name} ${if (enabled) "включил" else "отключил"} боссбар")
        }
    }
} 