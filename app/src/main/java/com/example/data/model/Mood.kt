package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class Mood(
    val id: String,
    val displayName: String,
    val description: String,
    val lightColorValue: Long,
    val darkColorValue: Long,
    val lightContainerValue: Long,
    val darkContainerValue: Long
) {
    CALM(
        id = "calm",
        displayName = "Calm",
        description = "Peaceful, centered & tranquil",
        lightColorValue = 0xFF2A6F68,
        darkColorValue = 0xFF80D4CA,
        lightContainerValue = 0xFFE0F2F1,
        darkContainerValue = 0xFF1B3835
    ),
    BRIGHT(
        id = "bright",
        displayName = "Bright",
        description = "Energetic, joyful & inspired",
        lightColorValue = 0xFFB86200,
        darkColorValue = 0xFFFFB74D,
        lightContainerValue = 0xFFFFF3E0,
        darkContainerValue = 0xFF3E2800
    ),
    HEAVY(
        id = "heavy",
        displayName = "Heavy",
        description = "Reflective, weighed down or melancholic",
        lightColorValue = 0xFF4C566A,
        darkColorValue = 0xFFB0BEC5,
        lightContainerValue = 0xFFECEFF1,
        darkContainerValue = 0xFF242A35
    ),
    RESTLESS(
        id = "restless",
        displayName = "Restless",
        description = "Scattered, anxious or stirred up",
        lightColorValue = 0xFFC04E3A,
        darkColorValue = 0xFFFF8A80,
        lightContainerValue = 0xFFFFEBEE,
        darkContainerValue = 0xFF3D1612
    );

    val lightColor: Color get() = Color(lightColorValue)
    val darkColor: Color get() = Color(darkColorValue)
    val lightContainer: Color get() = Color(lightContainerValue)
    val darkContainer: Color get() = Color(darkContainerValue)

    companion object {
        fun fromString(name: String?): Mood {
            return entries.find { it.name.equals(name, ignoreCase = true) || it.id.equals(name, ignoreCase = true) } ?: CALM
        }
    }
}
