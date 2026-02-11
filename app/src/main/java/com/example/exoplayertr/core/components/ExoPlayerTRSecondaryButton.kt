package com.example.exoplayertr.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.exoplayertr.R

@Composable
internal fun ExoPlayerTRSecondaryButton(
    onEndChat: () -> Unit
) {
    OutlinedButton(
        onClick = onEndChat,
        modifier = Modifier.fillMaxWidth(0.7f)
    ) {
        Text(
            text = stringResource(R.string.end_chat),
            style = MaterialTheme.typography.labelMedium
        )
    }
}