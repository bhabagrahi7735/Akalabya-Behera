package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class EmptyStateType {
    HOME,
    ARCHIVE,
    SEARCH,
    CALENDAR,
    FAVORITES
}

@Composable
fun EmptyState(
    type: EmptyStateType,
    onActionClick: (() -> Unit)? = null,
    actionText: String? = null,
    modifier: Modifier = Modifier
) {
    val (icon, title, description) = when (type) {
        EmptyStateType.HOME -> Triple(
            Icons.Rounded.HistoryEdu,
            "Your Daily Sanctuary",
            "Take a quiet pause. Capture your raw thoughts, gratitude, or emotions for today."
        )
        EmptyStateType.ARCHIVE -> Triple(
            Icons.Rounded.AutoStories,
            "No Journal Pages Yet",
            "Your archive will preserve all your written reflections, moods, and memories."
        )
        EmptyStateType.SEARCH -> Triple(
            Icons.Rounded.SearchOff,
            "No Reflections Found",
            "Try searching for different keywords, dates, or selecting another mood filter."
        )
        EmptyStateType.CALENDAR -> Triple(
            Icons.Rounded.CalendarMonth,
            "No Entries on This Day",
            "Would you like to write a reflection or backfill a journal memory for this date?"
        )
        EmptyStateType.FAVORITES -> Triple(
            Icons.Rounded.AutoStories,
            "No Starred Reflections",
            "Star meaningful entries to revisit your favorite breakthroughs and memories here."
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (onActionClick != null && actionText != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = CircleShape,
                modifier = Modifier.testTag("empty_state_action_btn")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Create,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
