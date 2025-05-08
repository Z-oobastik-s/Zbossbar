package org.zoobastiks.zbossbar.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.zoobastiks.zbossbar.Zbossbar
import org.bukkit.ChatColor

/**
 * Команда для включения/отключения боссбара для отдельного игрока
 */
class BossbarCommand(private val plugin: Zbossbar) : CommandExecutor {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Проверяем, что команда выполняется игроком
        if (sender !is Player) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cЭта команда может быть выполнена только игроком"))
            return true
        }
        
        // Проверяем наличие прав
        if (!sender.hasPermission("zbossbar.toggle")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.configManager.getMessage("no-permission")))
            return true
        }
        
        // Включаем/отключаем боссбар
        val enabled = plugin.togglePlayerBossbar(sender)
        
        // Отправляем сообщение о результате
        if (enabled) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.configManager.getMessage("player-toggle-on")))
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.configManager.getMessage("player-toggle-off")))
        }
        
        return true
    }
} 