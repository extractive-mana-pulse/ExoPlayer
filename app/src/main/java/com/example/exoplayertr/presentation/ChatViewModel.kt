package com.example.exoplayertr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exoplayertr.core.KeywordMatcher
import com.example.exoplayertr.core.ResponseType
import com.example.exoplayertr.core.toVideoConfig
import com.example.exoplayertr.domain.repo.SpeechRecognitionManagerRepo
import com.example.exoplayertr.domain.repo.VideoPlayerManagerRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val videoPlayerManagerRepo: VideoPlayerManagerRepo,
    private val speechRecognitionManagerRepo: SpeechRecognitionManagerRepo
) : ViewModel() {

    private val _state = MutableStateFlow<ChatState>(ChatState.Idle)
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _sideEffect = MutableStateFlow<ChatSideEffect?>(null)
    val sideEffect: StateFlow<ChatSideEffect?> = _sideEffect.asStateFlow()

    private var isVideoPaused = false
    private var isManualPause = false
    private var isSpeechInitialized = false
    private var isActivelyListening = false

    private var silencePromptCount = 0
    private var silenceTimeoutJob: Job? = null

    companion object {
        private const val SILENCE_TIMEOUT_MS = 10_000L
        private const val MAX_SILENCE_PROMPTS = 2
    }

    init {
        processStateChange(ChatState.Idle)
        initializeSpeechRecognition()
    }

    fun initializeSpeechRecognition() {
        if (!isSpeechInitialized) {
            speechRecognitionManagerRepo.initialize()
            speechRecognitionManagerRepo.setCallbacks(
                onResult = { recognizedText ->
                    isActivelyListening = false
                    silencePromptCount = 0
                    cancelSilenceTimeout()
                    handleIntent(ChatIntent.SpeechRecognized(recognizedText))
                },
                onError = {
                    isActivelyListening = false
                }
            )
            isSpeechInitialized = true
        }
    }

    fun handleIntent(intent: ChatIntent) {
        viewModelScope.launch {
            when (intent) {
                is ChatIntent.StartChat -> handleStartChat()
                is ChatIntent.VideoCompleted -> handleVideoCompleted()
                is ChatIntent.SpeechRecognized -> handleSpeechRecognized(intent.text)
                is ChatIntent.SpeechRecognitionFailed -> handleSpeechFailed()
                is ChatIntent.EndChat -> handleEndChat()
                is ChatIntent.SilenceTimeout -> handleSilenceTimeout()
            }
        }
    }

    private fun processStateChange(state: ChatState) {
        when (state) {
            is ChatState.Idle -> playStateVideo(state, shouldLoop = true)
            is ChatState.Greeting -> playStateVideo(state, shouldLoop = false, nextVideo = "listening")
            is ChatState.Listening -> playStateVideo(state, shouldLoop = true)
            is ChatState.Responding -> playStateVideo(state, shouldLoop = false, nextVideo = null)
            is ChatState.Prompt -> playStateVideo(state, shouldLoop = false, nextVideo = null)
            is ChatState.Goodbye -> playStateVideo(state, shouldLoop = false, nextVideo = null)
            is ChatState.Fallback -> playStateVideo(state, shouldLoop = false, nextVideo = null)
        }
    }

    private fun handleVideoCompleted() {
        when (val currentState = _state.value) {
            is ChatState.Greeting -> { setState(ChatState.Listening); startListeningWithDelay() }
            is ChatState.Prompt -> { setState(ChatState.Listening); startListeningWithDelay() }
            is ChatState.Goodbye -> { silencePromptCount = 0; setState(ChatState.Idle) }
            is ChatState.Fallback -> { silencePromptCount = 0; setState(ChatState.Listening); startListeningWithDelay() }
            is ChatState.Responding -> {
                if (currentState.responseType == ResponseType.GOODBYE) {
                    silencePromptCount = 0
                    stopSpeechRecognition()
                    setState(ChatState.Idle)
                } else {
                    silencePromptCount = 0
                    setState(ChatState.Listening)
                    startListeningWithDelay()
                }
            }
            else -> Unit
        }
    }

    fun togglePlayPause() {
        if (isVideoPaused) {
            videoPlayerManagerRepo.resume()
            isVideoPaused = false
            isManualPause = false

            if (_state.value is ChatState.Listening && isSpeechInitialized) {
                viewModelScope.launch {
                    delay(300)
                    if (_state.value is ChatState.Listening) {
                        startSpeechRecognition()
                    }
                }
            }
        } else {
            isManualPause = true
            cancelSilenceTimeout()
            stopSpeechRecognition()
            videoPlayerManagerRepo.pause()
            isVideoPaused = true
        }
    }

    private fun handleSpeechRecognized(text: String) {
        if (_state.value !is ChatState.Listening) return

        cancelSilenceTimeout()
        stopSpeechRecognition()

        val responseType = KeywordMatcher.matchKeywords(text)
        _sideEffect.value = ChatSideEffect.SpeechRecognized(text, responseType)
        setState(ChatState.Responding(responseType))
    }

    private fun handleSpeechFailed() {
        if (_state.value !is ChatState.Listening) return

        stopSpeechRecognition()
        setState(ChatState.Fallback)
    }

    private fun handleEndChat() {
        cancelSilenceTimeout()
        stopSpeechRecognition()
        silencePromptCount = 0
        setState(ChatState.Goodbye)
    }

    private fun handleSilenceTimeout() {
        if (_state.value !is ChatState.Listening) return
        cancelSilenceTimeout()
        stopSpeechRecognition()
        silencePromptCount++
        if (silencePromptCount >= MAX_SILENCE_PROMPTS) setState(ChatState.Goodbye) else setState(ChatState.Prompt)
    }

    private fun setState(newState: ChatState) {
        val previousState = _state.value
        _state.value = newState
        processStateChange(newState)
        _sideEffect.value = ChatSideEffect.StateChanged(previousState, newState)
    }

    private fun playStateVideo(
        state: ChatState,
        shouldLoop: Boolean,
        nextVideo: String? = null
    ) {
        val videoConfig = state.toVideoConfig()
        videoPlayerManagerRepo.playVideo(
            videoFileName = videoConfig.fileName,
            shouldLoop = shouldLoop,
            nextVideoFileName = nextVideo,
            onCompleted = {
                if (!shouldLoop) {
                    handleIntent(ChatIntent.VideoCompleted)
                }
            }
        )
    }

    private fun startSpeechRecognition() {
        if (isSpeechInitialized && !isActivelyListening && !isVideoPaused) {
            speechRecognitionManagerRepo.startListening()
            isActivelyListening = true
            startSilenceTimeout()
        }
    }

    private fun stopSpeechRecognition() {
        if (isActivelyListening) {
            speechRecognitionManagerRepo.cancel()
            isActivelyListening = false
        }
    }

    private fun startSilenceTimeout() {
        cancelSilenceTimeout()
        silenceTimeoutJob = viewModelScope.launch {
            delay(SILENCE_TIMEOUT_MS)
            handleIntent(ChatIntent.SilenceTimeout)
        }
    }

    private fun cancelSilenceTimeout() {
        silenceTimeoutJob?.cancel()
        silenceTimeoutJob = null
    }

    private fun startListeningWithDelay() {
        if (!isVideoPaused) {
            viewModelScope.launch {
                delay(300)
                if (_state.value is ChatState.Listening && !isVideoPaused) {
                    startSpeechRecognition()
                }
            }
        }
    }
    fun onStart() {
        videoPlayerManagerRepo.resume()

        if (_state.value is ChatState.Listening && isSpeechInitialized && !isVideoPaused) {
            viewModelScope.launch {
                delay(500)
                if (_state.value is ChatState.Listening) {
                    startSpeechRecognition()
                }
            }
        }
    }

    fun onStop() {
        isManualPause = true
        cancelSilenceTimeout()
        stopSpeechRecognition()
        videoPlayerManagerRepo.pause()
    }

    fun onPause() {
        isManualPause = true
        cancelSilenceTimeout()
        stopSpeechRecognition()
        videoPlayerManagerRepo.pause()
    }

    fun onResume() {
        if (!isVideoPaused) {
            videoPlayerManagerRepo.resume()
            isManualPause = false
        }
    }

    fun getPlayer() = videoPlayerManagerRepo.getOrCreatePlayer()

    fun clearSideEffect() { _sideEffect.value = null }

    override fun onCleared() {
        super.onCleared()
        cancelSilenceTimeout()
        videoPlayerManagerRepo.release()
        if (isSpeechInitialized) {
            speechRecognitionManagerRepo.destroy()
        }
    }

    private fun handleStartChat() {
        if (_state.value is ChatState.Idle) {
            silencePromptCount = 0
            setState(ChatState.Greeting)
        }
    }
}