package org.zoobastiks.zbossbar.utils

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.zoobastiks.zbossbar.Zbossbar

class MessageUtils(private val plugin: Zbossbar) {
    
    private val legacySerializer = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build()
    
    /**
     * Отправляет сообщение отправителю команды
     * 
     * @param sender отправитель команды
     * @param key ключ сообщения в конфигурации
     */
    fun sendMessage(sender: CommandSender, key: String) {
        val message = plugin.configManager.getMessage(key)
        val formattedMessage = formatMessage(message)
        val component = legacySerializer.deserialize(formattedMessage)
        sender.sendMessage(component)
    }
    
    /**
     * Отправляет сообщение всем игрокам на сервере
     * 
     * @param key ключ сообщения в конфигурации
     */
    fun broadcastMessage(key: String) {
        val message = plugin.configManager.getMessage(key)
        val formattedMessage = formatMessage(message)
        val component = legacySerializer.deserialize(formattedMessage)
        plugin.server.broadcast(component)
    }
    
    /**
     * Форматирует сообщение с поддержкой цветов
     * 
     * @param message сообщение для форматирования
     * @return отформатированное сообщение
     */
    fun formatMessage(message: String): String {
        return message.replace("&", "§")
    }
    
    /**
     * Проверяет, имеет ли игрок указанное разрешение
     * 
     * @param sender отправитель команды
     * @param permission разрешение для проверки
     * @return true, если у отправителя есть разрешение, иначе false
     */
    fun hasPermission(sender: CommandSender, permission: String): Boolean {
        if (!sender.hasPermission(permission)) {
            sendMessage(sender, "no-permission")
            return false
        }
        return true
    }
} 