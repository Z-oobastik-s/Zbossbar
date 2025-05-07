package org.zoobastiks.zbossbar.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.zoobastiks.zbossbar.Zbossbar

/**
 * Команда для включения/отключения боссбара для отдельного игрока
 */
class BossbarCommand(private val plugin: Zbossbar) : CommandExecutor {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Проверяем, что команда выполняется игроком
        if (sender !is Player) {
            sender.sendMessage("§cЭта команда может быть выполнена только игроком")
            return true
        }
        
        // Проверяем наличие прав
        if (!sender.hasPermission("zbossbar.toggle")) {
            sender.sendMessage(plugin.configManager.getMessage("no-permission"))
            return true
        }
        
        // Включаем/отключаем боссбар
        val enabled = plugin.togglePlayerBossbar(sender)
        
        // Отправляем сообщение о результате
        if (enabled) {
            sender.sendMessage(plugin.configManager.getMessage("player-toggle-on"))
        } else {
            sender.sendMessage(plugin.configManager.getMessage("player-toggle-off"))
        }
        
        return true
    }
} 