package com.example.exoplayertr.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.exoplayertr.R

@Composable
internal fun ExoPlayerTRPrimaryButton(
    onStartChat: () -> Unit
) {
    Button(
        onClick = onStartChat,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = stringResource(R.string.start_chat),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}