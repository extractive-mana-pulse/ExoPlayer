package com.example.exoplayertr.data.repoImpl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.exoplayertr.domain.model.candidates
import com.example.exoplayertr.domain.repo.SpeechRecognitionManagerRepo

class SpeechRecognitionManager(
    private val context: Context
) : SpeechRecognitionManagerRepo {

    private var isListening = false
    private var onError: (() -> Unit)? = null
    private var onResult: ((String) -> Unit)? = null
    private var speechRecognizer: SpeechRecognizer? = null

    override fun setCallbacks(onResult: (String) -> Unit, onError: () -> Unit) {
        this.onResult = onResult
        this.onError = onError
    }

    override fun initialize() {
        val isAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        if (!isAvailable) return

        speechRecognizer = createBestRecognizer().apply {
            setRecognitionListener(recognitionListener)
        }
    }

    private fun createBestRecognizer(): SpeechRecognizer {
        val packageManager = context.packageManager

        for (component in candidates) {
            val intent = Intent(RecognitionService.SERVICE_INTERFACE).apply {
                setComponent(component)
            }
            val resolveInfo = packageManager.resolveService(intent, 0)
            if (resolveInfo != null) {
                return SpeechRecognizer.createSpeechRecognizer(context, component)
            }
        }
        return SpeechRecognizer.createSpeechRecognizer(context)
    }

    override fun startListening() { if (isListening) return
        speechRecognizer?.let { recognizer ->
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra("android.speech.extra.DICTATION_MODE", true)

                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 12000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L)
            }
            recognizer.startListening(intent)
            isListening = true
        }
    }

    override fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
        }
    }

    override fun cancel() {
        if (isListening) {
            speechRecognizer?.cancel()
            isListening = false
        }
    }

    override fun destroy() {
        cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        onResult = null
        onError = null
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { isListening = false }

        override fun onError(error: Int) {
            android.util.Log.e("SpeechRecognizer", "onError code: $error")
            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "No permission"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout"
                else -> "Unknown: $error"
            }
            initialize()
            isListening = false
            onError?.invoke()
        }

        override fun onResults(results: Bundle?) {
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                if (matches.isNotEmpty()) {
                    isListening = false
                    onResult?.invoke(matches[0])
                } else {
                    isListening = false
                    onError?.invoke()
                }
            } ?: run {
                isListening = false
                onError?.invoke()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}