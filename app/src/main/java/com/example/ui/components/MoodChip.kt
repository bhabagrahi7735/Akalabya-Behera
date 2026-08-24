package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Storm
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Mood

fun getMoodIcon(mood: Mood): ImageVector {
    return when (mood) {
        Mood.CALM -> Icons.Rounded.SelfImprovement
        Mood.BRIGHT -> Icons.Rounded.WbSunny
        Mood.HEAVY -> Icons.Rounded.Cloud
        Mood.RESTLESS -> Icons.Rounded.Storm
    }
}

@Composable
fun MoodChip(
    mood: Mood,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val moodColor = if (isDark) mood.darkColor else mood.lightColor
    val moodContainer = if (isDark) mood.darkContainer else mood.lightContainer

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) moodContainer else MaterialTheme.colorScheme.surface,
        animationSpec = spring(),
        label = "chipBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) moodColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        animationSpec = spring(),
        label = "chipBorder"
    )

    Surface(
        modifier = modifier
            .testTag("mood_chip_${mood.id}")
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = if (compact) 10.dp else 14.dp,
                    vertical = if (compact) 6.dp else 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 16.dp else 20.dp)
                    .background(
                        color = if (selected) moodColor else moodColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(if (compact) 10.dp else 12.dp)
                    )
                } else {
                    Icon(
                        imageVector = getMoodIcon(mood),
                        contentDescription = null,
                        tint = moodColor,
                        modifier = Modifier.size(if (compact) 10.dp else 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = mood.displayName,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = if (compact) 12.sp else 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = if (selected) moodColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MoodSelectorRow(
    selectedMood: Mood,
    onMoodSelected: (Mood) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Mood.entries.forEach { mood ->
            MoodChip(
                mood = mood,
                selected = selectedMood == mood,
                onClick = { onMoodSelected(mood) },
                compact = compact,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
