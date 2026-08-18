package com.example.util

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class PostBgPreset(
    val id: String,
    val name: String,
    val isGradient: Boolean = false,
    val solidColor: Color = Color.Transparent,
    val gradientColors: List<Color> = emptyList(),
    val isDarkText: Boolean = false
) {
    fun getBrush(): Brush? {
        return if (isGradient && gradientColors.size >= 2) {
            Brush.linearGradient(gradientColors)
        } else null
    }

    val textColor: Color
        get() = if (isDarkText) Color.Black else Color.White
}

object PostBgStyle {
    val PRESETS = listOf(
        PostBgPreset("NONE", "Default", solidColor = Color.Transparent, isDarkText = false),
        
        // Single Solid Colors (14)
        PostBgPreset("RED", "Red", solidColor = Color(0xFFE53935), isDarkText = false),
        PostBgPreset("GREEN", "Green", solidColor = Color(0xFF4CAF50), isDarkText = false),
        PostBgPreset("BLUE", "Blue", solidColor = Color(0xFF1E88E5), isDarkText = false),
        PostBgPreset("WHITE", "White", solidColor = Color(0xFFFFFFFF), isDarkText = true),
        PostBgPreset("BLACK", "Black", solidColor = Color(0xFF121212), isDarkText = false),
        PostBgPreset("PURPLE", "Purple", solidColor = Color(0xFF8E24AA), isDarkText = false),
        PostBgPreset("YELLOW", "Yellow", solidColor = Color(0xFFFDD835), isDarkText = true),
        PostBgPreset("ORANGE", "Orange", solidColor = Color(0xFFFB8C00), isDarkText = false),
        PostBgPreset("CYAN", "Cyan", solidColor = Color(0xFF00ACC1), isDarkText = false),
        PostBgPreset("MAGENTA", "Magenta", solidColor = Color(0xFFD81B60), isDarkText = false),
        PostBgPreset("PINK", "Pink", solidColor = Color(0xFFFF80AB), isDarkText = true),
        PostBgPreset("DARK_RED", "Dark Red", solidColor = Color(0xFF880E4F), isDarkText = false),
        PostBgPreset("DEEP_BLUE", "Deep Blue", solidColor = Color(0xFF0D47A1), isDarkText = false),
        PostBgPreset("OLIVE", "Olive", solidColor = Color(0xFF1B5E20), isDarkText = false),
        
        // Gradient Colors (10)
        PostBgPreset("GRADIENT_RED_YELLOW", "Fire", isGradient = true, gradientColors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)), isDarkText = false),
        PostBgPreset("GRADIENT_BLUE_PURPLE", "Cosmic", isGradient = true, gradientColors = listOf(Color(0xFF4A00E0), Color(0xFF8E24AA)), isDarkText = false),
        PostBgPreset("GRADIENT_SUNSET", "Sunset", isGradient = true, gradientColors = listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B)), isDarkText = false),
        PostBgPreset("GRADIENT_NEON_TEAL", "Neon Teal", isGradient = true, gradientColors = listOf(Color(0xFF00F2FE), Color(0xFF4FACFE)), isDarkText = true),
        PostBgPreset("GRADIENT_PURPLE_PINK", "Mystic", isGradient = true, gradientColors = listOf(Color(0xFFDA22FF), Color(0xFF9733EE)), isDarkText = false),
        PostBgPreset("GRADIENT_EMERALD", "Emerald", isGradient = true, gradientColors = listOf(Color(0xFF0575E6), Color(0xFF00F260)), isDarkText = false),
        PostBgPreset("GRADIENT_GOLD", "Gold", isGradient = true, gradientColors = listOf(Color(0xFFF2994A), Color(0xFFF2C94C)), isDarkText = true),
        PostBgPreset("GRADIENT_NIGHT", "Night", isGradient = true, gradientColors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)), isDarkText = false),
        PostBgPreset("GRADIENT_ROSE", "Rose", isGradient = true, gradientColors = listOf(Color(0xFFF857A6), Color(0xFFFF5858)), isDarkText = false),
        PostBgPreset("GRADIENT_LIME", "Lime", isGradient = true, gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D)), isDarkText = true)
    )

    fun getPreset(id: String?): PostBgPreset {
        if (id.isNullOrBlank()) return PRESETS.first()
        return PRESETS.find { it.id == id } ?: PRESETS.first()
    }
}
