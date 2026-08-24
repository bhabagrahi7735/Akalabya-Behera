package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SaveState {
    IDLE,
    SAVING,
    SAVED,
    OFFLINE_SAVED,
    ERROR
}

@Composable
fun AutoSaveIndicator(
    saveState: SaveState,
    lastSavedTime: Long? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("auto_save_status_indicator"),
        shape = RoundedCornerShape(16.dp),
        color = when (saveState) {
            SaveState.SAVING -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            SaveState.SAVED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            SaveState.OFFLINE_SAVED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            SaveState.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            SaveState.IDLE -> Color.Transparent
        }
    ) {
        AnimatedContent(
            targetState = saveState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "saveStateAnim"
        ) { state ->
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                when (state) {
                    SaveState.SAVING -> {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = "Saving",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Saving...",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    SaveState.SAVED -> {
                        Icon(
                            imageVector = Icons.Rounded.CloudDone,
                            contentDescription = "Saved",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val timeStr = lastSavedTime?.let {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it))
                        }
                        Text(
                            text = if (timeStr != null) "Saved $timeStr" else "Saved",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SaveState.OFFLINE_SAVED -> {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Saved offline",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Saved offline",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SaveState.ERROR -> {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sync error (saved locally)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    SaveState.IDLE -> {
                        // Empty
                    }
                }
            }
        }
    }
}
