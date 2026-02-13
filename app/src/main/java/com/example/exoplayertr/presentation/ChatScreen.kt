package com.example.exoplayertr.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayertr.core.toastMessage
import com.example.exoplayertr.presentation.components.ControlPanel

@Composable
fun ChatScreenRoot(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sideEffect by viewModel.sideEffect.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onStart()
                Lifecycle.Event.ON_STOP -> viewModel.onStop()
                Lifecycle.Event.ON_PAUSE -> viewModel.onPause()
                Lifecycle.Event.ON_RESUME -> viewModel.onResume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ChatScreen(
        state = state,
        sideEffect = sideEffect,
        exoPlayer = viewModel.getPlayer(),
        onIntent = viewModel::handleIntent,
        onClearSideEffect = viewModel::clearSideEffect,
        onInitializeSpeech = viewModel::initializeSpeechRecognition,
        onTogglePlayPause = viewModel::togglePlayPause
    )
}

@Composable
private fun ChatScreen(
    state: ChatState,
    sideEffect: ChatSideEffect?,
    exoPlayer: ExoPlayer,
    onIntent: (ChatIntent) -> Unit,
    onClearSideEffect: () -> Unit,
    onInitializeSpeech: () -> Unit,
    onTogglePlayPause: () -> Unit
) {
    val context = LocalContext.current
    var isTransitioning by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onInitializeSpeech()
        } else {
            toastMessage(
                context = context,
                message = "Microphone permission required for voice interaction"
            )
        }
    }

    LaunchedEffect(sideEffect) {
        sideEffect?.let { effect ->
            when (effect) {
                is ChatSideEffect.SpeechRecognized -> {
                    toastMessage(
                        context = context,
                        message = "You said: ${effect.text}"
                    )
                }
                is ChatSideEffect.PermissionRequired -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                else -> Unit
            }
            onClearSideEffect()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTogglePlayPause() }
                    )
                }
        ) {
            VideoPlayer(
                exoPlayer = exoPlayer,
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                visible = isTransitioning,
                enter = fadeIn(animationSpec = tween(120)),
                exit = fadeOut(animationSpec = tween(180))
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }
        }

        ControlPanel(
            state = state,
            onStartChat = { onIntent(ChatIntent.StartChat) },
            onEndChat = { onIntent(ChatIntent.EndChat) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@PreviewLightDark
@Composable
private fun ChatScreenPreview() {
    val context = LocalContext.current
    ChatScreen(
        state = ChatState.Idle,
        sideEffect = null,
        exoPlayer = ExoPlayer.Builder(context).build(),
        onIntent = {},
        onClearSideEffect = {},
        onInitializeSpeech = {},
        onTogglePlayPause = {}
    )
}