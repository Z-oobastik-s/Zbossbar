package org.zoobastiks.zbossbar.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.zoobastiks.zbossbar.Zbossbar
import java.util.Collections

/**
 * Команда для глобального управления плагином Zbossbar
 */
class ZbossbarCommand(private val plugin: Zbossbar) : CommandExecutor, TabCompleter {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Проверяем наличие прав
        if (!sender.hasPermission("zbossbar.admin")) {
            sender.sendMessage(plugin.configManager.getMessage("no-permission"))
            return true
        }
        
        if (args.isEmpty()) {
            // Базовая команда - включаем/отключаем плагин глобально
            togglePlugin(sender)
            return true
        }
        
        when (args[0].lowercase()) {
            "reload" -> {
                reloadPlugin(sender)
                return true
            }
            "help" -> {
                showHelp(sender)
                return true
            }
        }
        
        // Если дошли до сюда, значит команда не распознана
        sender.sendMessage("§cНеизвестная команда. Используйте /zbossbar help для справки")
        return true
    }
    
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (!sender.hasPermission("zbossbar.admin")) {
            return Collections.emptyList()
        }
        
        if (args.size == 1) {
            val completions = mutableListOf("reload", "help")
            val input = args[0].lowercase()
            return completions.filter { it.startsWith(input) }
        }
        
        return Collections.emptyList()
    }
    
    /**
     * Включает или отключает плагин глобально
     */
    private fun togglePlugin(sender: CommandSender) {
        val newState = plugin.togglePluginEnabled()
        
        if (newState) {
            sender.sendMessage(plugin.configManager.getMessage("plugin-enabled"))
        } else {
            sender.sendMessage(plugin.configManager.getMessage("plugin-disabled"))
        }
    }
    
    /**
     * Перезагружает плагин
     */
    private fun reloadPlugin(sender: CommandSender) {
        try {
            plugin.reloadPlugin()
            sender.sendMessage(plugin.configManager.getMessage("reload-success"))
        } catch (e: Exception) {
            sender.sendMessage(plugin.configManager.getMessage("reload-error"))
            plugin.logger.severe("Ошибка при перезагрузке плагина: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Показывает справку по командам
     */
    private fun showHelp(sender: CommandSender) {
        sender.sendMessage(plugin.configManager.getMessage("help-header"))
        
        val helpCommand = plugin.configManager.getMessageWithoutPrefix("help-command")
        sender.sendMessage(helpCommand.replace("%command%", "bossbar")
            .replace("%description%", "Включить/отключить боссбар для себя"))
        sender.sendMessage(helpCommand.replace("%command%", "zbossbar")
            .replace("%description%", "Включить/отключить плагин глобально"))
        sender.sendMessage(helpCommand.replace("%command%", "zbossbar reload")
            .replace("%description%", "Перезагрузить конфигурацию"))
        sender.sendMessage(helpCommand.replace("%command%", "zbossbar help")
            .replace("%description%", "Показать эту справку"))
        
        sender.sendMessage(plugin.configManager.getMessage("help-footer"))
    }
} 