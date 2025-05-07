package org.zoobastiks.zbossbar.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.zoobastiks.zbossbar.Zbossbar
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.logging.Level

class UserDataManager(private val plugin: Zbossbar) {
    
    private val userDataFile: File
    private lateinit var userDataConfig: FileConfiguration
    
    init {
        // Создаем папку плагина, если её нет
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        
        // Создаем файл userdata.yml
        userDataFile = File(plugin.dataFolder, "userdata.yml")
        
        // Загружаем или создаем конфигурацию
        loadUserData()
    }
    
    /**
     * Загружает данные пользователей из файла
     */
    fun loadUserData() {
        if (!userDataFile.exists()) {
            try {
                userDataFile.createNewFile()
                
                // Создаем начальную структуру файла
                val defaultConfig = YamlConfiguration()
                defaultConfig.createSection("players")
                defaultConfig.save(userDataFile)
                
                plugin.logger.info("Создан новый файл userdata.yml")
            } catch (e: IOException) {
                plugin.logger.log(Level.SEVERE, "Не удалось создать файл userdata.yml", e)
            }
        }
        
        userDataConfig = YamlConfiguration.loadConfiguration(userDataFile)
        
        // Если секция players не существует, создаем её
        if (!userDataConfig.isConfigurationSection("players")) {
            userDataConfig.createSection("players")
            saveUserData()
        }
    }
    
    /**
     * Сохраняет данные пользователей в файл
     */
    fun saveUserData() {
        try {
            userDataConfig.save(userDataFile)
            plugin.logger.info("Файл userdata.yml успешно сохранен")
        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "Не удалось сохранить файл userdata.yml", e)
        }
    }
    
    /**
     * Получает список UUID игроков, отключивших боссбар
     */
    fun getDisabledPlayers(): Set<UUID> {
        val result = mutableSetOf<UUID>()
        
        val playersSection = userDataConfig.getConfigurationSection("players")
        if (playersSection != null) {
            for (uuidString in playersSection.getKeys(false)) {
                try {
                    val uuid = UUID.fromString(uuidString)
                    val disabled = playersSection.getBoolean(uuidString)
                    if (disabled) {
                        result.add(uuid)
                    }
                } catch (e: IllegalArgumentException) {
                    plugin.logger.log(Level.WARNING, "Неверный UUID в userdata.yml: $uuidString")
                }
            }
        }
        
        return result
    }
    
    /**
     * Сохраняет список UUID игроков, отключивших боссбар
     */
    fun saveDisabledPlayers(disabledPlayers: Set<UUID>) {
        // Очищаем секцию игроков
        userDataConfig.set("players", null)
        val playersSection = userDataConfig.createSection("players")
        
        // Записываем новые данные
        for (uuid in disabledPlayers) {
            playersSection.set(uuid.toString(), true)
        }
        
        saveUserData()
    }
} 