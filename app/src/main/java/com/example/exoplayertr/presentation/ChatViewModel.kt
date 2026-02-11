package com.example.exoplayertr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exoplayertr.core.KeywordMatcher
import com.example.exoplayertr.core.ResponseType
import com.example.exoplayertr.core.toVideoConfig
import com.example.exoplayertr.domain.repo.SpeechRecognitionManagerRepo
import com.example.exoplayertr.domain.repo.VideoPlayerManagerRepo
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private var isSpeechInitialized = false

    private val _state = MutableStateFlow<ChatState>(ChatState.Idle)
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _sideEffect = MutableStateFlow<ChatSideEffect?>(null)
    val sideEffect: StateFlow<ChatSideEffect?> = _sideEffect.asStateFlow()

    init {
        processStateChange(ChatState.Idle)
        initializeSpeechRecognition()
    }

    fun initializeSpeechRecognition() {
        if (!isSpeechInitialized) {
            speechRecognitionManagerRepo.initialize()
            speechRecognitionManagerRepo.setCallbacks(
                onResult = { recognizedText ->
                    handleIntent(ChatIntent.SpeechRecognized(recognizedText))
                },
                onError = {
                    handleIntent(ChatIntent.SpeechRecognitionFailed)
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
            }
        }
    }

    private fun handleSpeechRecognized(text: String) {
        if (_state.value !is ChatState.Listening) return

        if (isSpeechInitialized) {
            speechRecognitionManagerRepo.stopListening()
        }

        val responseType = KeywordMatcher.matchKeywords(text)
        _sideEffect.value = ChatSideEffect.SpeechRecognized(text, responseType)
        setState(ChatState.Responding(responseType))
    }

    private fun handleSpeechFailed() {
        if (_state.value !is ChatState.Listening) return

        if (isSpeechInitialized) {
            speechRecognitionManagerRepo.stopListening()
        }
        setState(ChatState.Fallback)
    }

    private fun handleEndChat() {
        if (isSpeechInitialized) {
            speechRecognitionManagerRepo.cancel()
        }
        setState(ChatState.Goodbye)
    }

    private fun setState(newState: ChatState) {
        val previousState = _state.value
        _state.value = newState
        processStateChange(newState)
        _sideEffect.value = ChatSideEffect.StateChanged(previousState, newState)
    }

    private fun handleVideoCompleted() {
        when (val currentState = _state.value) {
            is ChatState.Greeting -> {
                setState(ChatState.Listening)
                viewModelScope.launch {
                    delay(300)
                    if (_state.value is ChatState.Listening && isSpeechInitialized) {
                        speechRecognitionManagerRepo.startListening()
                    }
                }
            }
            is ChatState.Responding -> {
                if (currentState.responseType == ResponseType.GOODBYE) {
                    if (isSpeechInitialized) { speechRecognitionManagerRepo.cancel() }
                    setState(ChatState.Idle)
                } else {
                    setState(ChatState.Listening)
                    viewModelScope.launch {
                        delay(300)
                        if (_state.value is ChatState.Listening && isSpeechInitialized) {
                            speechRecognitionManagerRepo.startListening()
                        }
                    }
                }
            }
            is ChatState.Goodbye -> setState(ChatState.Idle)
            is ChatState.Fallback -> {
                setState(ChatState.Listening)
                viewModelScope.launch {
                    delay(300)
                    if (_state.value is ChatState.Listening && isSpeechInitialized) {
                        speechRecognitionManagerRepo.startListening()
                    }
                }
            }
            else -> Unit
        }
    }

    private fun processStateChange(state: ChatState) {
        when (state) {
            is ChatState.Idle -> playStateVideo(state, shouldLoop = true)
            is ChatState.Greeting -> playStateVideo(state, shouldLoop = false, nextVideo = "listening")
            is ChatState.Listening -> playStateVideo(state, shouldLoop = true)
            is ChatState.Responding -> playStateVideo(state, shouldLoop = false, nextVideo = null)
            is ChatState.Goodbye -> playStateVideo(state, shouldLoop = false, nextVideo = null)
            is ChatState.Fallback -> playStateVideo(state, shouldLoop = false, nextVideo = null)
        }
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

    fun getPlayer() = videoPlayerManagerRepo.getOrCreatePlayer()

    fun clearSideEffect() {
        _sideEffect.value = null
    }

    fun onPause() {
        videoPlayerManagerRepo.pause()
        if (isSpeechInitialized) {
            speechRecognitionManagerRepo.cancel()
        }
    }

    fun onResume() {
        videoPlayerManagerRepo.resume()
        if (_state.value is ChatState.Listening && isSpeechInitialized) {
            speechRecognitionManagerRepo.startListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        videoPlayerManagerRepo.release()
        if (isSpeechInitialized) {
            speechRecognitionManagerRepo.destroy()
        }
    }

    private fun handleStartChat() {
        if (_state.value is ChatState.Idle) {
            setState(ChatState.Greeting)
        }
    }
}