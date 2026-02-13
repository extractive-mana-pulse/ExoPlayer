package com.example.exoplayertr.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.exoplayertr.core.components.ExoPlayerTRPrimaryButton
import com.example.exoplayertr.core.components.ExoPlayerTRSecondaryButton
import com.example.exoplayertr.presentation.ChatState

@Composable
internal fun ControlPanel(
    state: ChatState,
    onStartChat: () -> Unit,
    onEndChat: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (state is ChatState.Listening) {
                RecordingIndicator()
                Spacer(modifier = Modifier.width(8.dp))
            }
            AnimatedStatusTextFading(state = state)
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (state) {
            is ChatState.Idle -> ExoPlayerTRPrimaryButton(onStartChat)
            is ChatState.Greeting,
            is ChatState.Listening,
            is ChatState.Responding,
            is ChatState.Fallback -> ExoPlayerTRSecondaryButton(onEndChat)
            is ChatState.Goodbye -> Unit
            ChatState.Prompt -> Unit
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}