package org.zoobastiks.zbossbar.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.awt.Color
import java.util.regex.Pattern

object TextUtils {
    
    private val GRADIENT_PATTERN = Pattern.compile("<gradient:(#[A-Fa-f0-9]{6}):(#[A-Fa-f0-9]{6})>(.*?)</gradient>")
    private val HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})")
    private val LEGACY_PATTERN = Pattern.compile("[&§]([0-9a-fA-FklmnorKLMNOR])")
    
    private val legacySerializer = LegacyComponentSerializer.legacyAmpersand()
    
    /**
     * Форматирует текст с поддержкой цветов и градиентов
     * для отображения игроку (не использует PlaceholderAPI)
     */
    fun formatText(text: String): String {
        // Обрабатываем градиенты
        var formattedText = processGradients(text)
        
        // Обрабатываем HEX цвета
        formattedText = processHexColors(formattedText)
        
        // Обрабатываем обычные цвета (&a, §b и т.д.)
        formattedText = ChatColor.translateAlternateColorCodes('&', formattedText)
        
        return formattedText
    }
    
    /**
     * Обрабатывает градиентные теги в тексте
     */
    private fun processGradients(text: String): String {
        var result = text
        val matcher = GRADIENT_PATTERN.matcher(result)
        val sb = StringBuilder()
        
        while (matcher.find()) {
            try {
                val startColorHex = matcher.group(1)
                val endColorHex = matcher.group(2)
                val content = matcher.group(3)
                
                val startColor = Color.decode(startColorHex)
                val endColor = Color.decode(endColorHex)
                val gradientText = createGradientText(content, startColor, endColor)
                
                matcher.appendReplacement(sb, gradientText)
            } catch (e: Exception) {
                // В случае ошибки просто возвращаем исходный текст
                matcher.appendReplacement(sb, matcher.group(3))
            }
        }
        matcher.appendTail(sb)
        
        return sb.toString()
    }
    
    /**
     * Обрабатывает HEX цвета в тексте (&#RRGGBB)
     */
    private fun processHexColors(text: String): String {
        var result = text
        val matcher = HEX_PATTERN.matcher(result)
        val sb = StringBuilder()
        
        while (matcher.find()) {
            try {
                val hex = matcher.group(1)
                // Формат для 1.16+: §x§R§R§G§G§B§B
                val replacement = StringBuilder("§x")
                for (c in hex.toCharArray()) {
                    replacement.append("§").append(c)
                }
                matcher.appendReplacement(sb, replacement.toString())
            } catch (e: Exception) {
                // В случае ошибки просто сохраняем исходный текст
                matcher.appendReplacement(sb, "&" + matcher.group(0))
            }
        }
        matcher.appendTail(sb)
        
        return sb.toString()
    }
    
    /**
     * Создает текст с градиентом между двумя цветами
     */
    private fun createGradientText(text: String, startColor: Color, endColor: Color): String {
        val chars = text.toCharArray()
        val sb = StringBuilder()
        
        // Если текст пустой или с одним символом, просто применяем начальный цвет
        if (chars.isEmpty()) return ""
        if (chars.size == 1) {
            return "§x" + colorToMinecraftFormat(startColor) + chars[0]
        }
        
        // Создаем градиент для каждого символа
        for (i in chars.indices) {
            val ratio = i.toFloat() / (chars.size - 1)
            val r = interpolate(startColor.red, endColor.red, ratio)
            val g = interpolate(startColor.green, endColor.green, ratio)
            val b = interpolate(startColor.blue, endColor.blue, ratio)
            
            val color = Color(r, g, b)
            
            // Добавляем цвет и символ
            sb.append("§x").append(colorToMinecraftFormat(color)).append(chars[i])
        }
        
        return sb.toString()
    }
    
    /**
     * Преобразует объект Color в формат для Minecraft (§R§R§G§G§B§B)
     */
    private fun colorToMinecraftFormat(color: Color): String {
        val hex = String.format("%02X%02X%02X", color.red, color.green, color.blue)
        val sb = StringBuilder()
        
        for (c in hex.toCharArray()) {
            sb.append("§").append(c)
        }
        
        return sb.toString()
    }
    
    /**
     * Интерполирует между двумя значениями
     */
    private fun interpolate(start: Int, end: Int, ratio: Float): Int {
        return (start + (end - start) * ratio).toInt().coerceIn(0, 255)
    }
} 