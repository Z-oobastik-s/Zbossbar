package org.zoobastiks.zbossbar.model

import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle

/**
 * Класс, содержащий настройки босс-бара для конкретного мира
 */
data class WorldBossbarConfig(
    // Основные настройки
    val enabled: Boolean,
    val title: String,
    val titles: List<String>,
    val titleChangeInterval: Int,
    
    // Цвета и стили
    val colors: List<BarColor>,
    val styles: List<BarStyle>,
    
    // Настройки анимации
    val animationEnabled: Boolean,
    val animationInterval: Int,
    val reverseAnimation: Boolean,
    val pingPongAnimation: Boolean,
    
    // Настройки прогресса
    val progressType: String,
    val progressValue: Double,
    val progressMaxValue: Double,
    val progressPlaceholder: String,
    val timeFormat: String,
    val useServerTime: Boolean,
    
    // Дополнительные настройки
    val overlayPrevious: Boolean,
    val permission: String?,
    val showOnJoin: Boolean
) {
    /**
     * Проверяет, имеет ли игрок необходимое разрешение для отображения боссбара
     */
    fun hasRequiredPermission(playerHasPermission: (String) -> Boolean): Boolean {
        if (permission == null || permission.isEmpty()) {
            return true
        }
        return playerHasPermission(permission)
    }
    
    /**
     * Возвращает, имеет ли этот боссбар множественные тексты
     */
    fun hasMultipleTexts(): Boolean {
        return titles.size > 1
    }
    
    /**
     * Возвращает, имеет ли этот боссбар анимацию цветов
     */
    fun hasColorAnimation(): Boolean {
        return animationEnabled && colors.size > 1
    }
    
    /**
     * Возвращает, имеет ли этот боссбар анимацию стилей
     */
    fun hasStyleAnimation(): Boolean {
        return animationEnabled && styles.size > 1
    }
    
    /**
     * Возвращает, имеет ли этот боссбар какую-либо анимацию
     */
    fun hasAnyAnimation(): Boolean {
        return hasMultipleTexts() || hasColorAnimation() || hasStyleAnimation()
    }
} 