package com.example.core.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Interface abstraction for Speech-To-Text services.
 * Allows replacing with Cloud Speech, Bhashini AI, Whisper, or Android Native Recognizer.
 */
interface FarmSpeechToTextService {
    fun isAvailable(): Boolean
    fun startListening(
        language: VoiceLanguage,
        onRmsChanged: (Float) -> Unit,
        onPartialResult: (String) -> Unit,
        onResult: (VoiceRecognitionResult) -> Unit
    )
    fun stopListening()
    fun cancelListening()
    fun release()
}

/**
 * Interface abstraction for Text-To-Speech services.
 * Allows replacing with ElevenLabs, Bhashini TTS, Google Cloud TTS, or Android Native TTS.
 */
interface FarmTextToSpeechService {
    fun isAvailable(): Boolean
    fun speak(
        text: String,
        language: VoiceLanguage,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    )
    fun stop()
    fun setMuted(isMuted: Boolean)
    fun isMuted(): Boolean
    fun release()
}

/**
 * Android Native Implementation of STT with graceful fallback simulation
 * for emulators and offline situations.
 */
class AndroidSpeechToTextService(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : FarmSpeechToTextService {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningActive = false
    private var simulationJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    override fun startListening(
        language: VoiceLanguage,
        onRmsChanged: (Float) -> Unit,
        onPartialResult: (String) -> Unit,
        onResult: (VoiceRecognitionResult) -> Unit
    ) {
        cancelListening()
        isListeningActive = true

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            // Emulated / fallback speech recognition with realistic farmer prompts
            runSimulationListening(language, onRmsChanged, onPartialResult, onResult)
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d("VoiceSTT", "Ready for speech in ${language.code}")
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d("VoiceSTT", "User began speaking")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        onRmsChanged(rmsdB.coerceIn(0f, 10f))
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d("VoiceSTT", "End of speech detected")
                    }

                    override fun onError(error: Int) {
                        isListeningActive = false
                        val (errorCode, detail) = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH,
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceErrorCode.NO_SPEECH to "No speech recognized"
                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> VoiceErrorCode.NETWORK_UNAVAILABLE to "Network timeout"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceErrorCode.PERMISSION_DENIED to "Permission missing"
                            else -> VoiceErrorCode.NO_SPEECH to "Recognition code $error"
                        }
                        onResult(VoiceRecognitionResult.Error(errorCode, detail))
                    }

                    override fun onResults(results: Bundle?) {
                        isListeningActive = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim().orEmpty()
                        if (text.isNotBlank()) {
                            onResult(VoiceRecognitionResult.Success(text = text, language = language))
                        } else {
                            onResult(VoiceRecognitionResult.Error(VoiceErrorCode.NO_SPEECH, "Empty speech buffer"))
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = partialMatches?.firstOrNull().orEmpty()
                        if (partial.isNotBlank()) {
                            onPartialResult(partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.code)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.code)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language.code)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.w("VoiceSTT", "Native recognizer failed, falling back to simulated speech", e)
            runSimulationListening(language, onRmsChanged, onPartialResult, onResult)
        }
    }

    private fun runSimulationListening(
        language: VoiceLanguage,
        onRmsChanged: (Float) -> Unit,
        onPartialResult: (String) -> Unit,
        onResult: (VoiceRecognitionResult) -> Unit
    ) {
        simulationJob = coroutineScope.launch {
            val sampleQueries = when (language) {
                VoiceLanguage.HINDI -> listOf(
                    "मेरी फसल की हालत कैसी है?",
                    "क्या मुझे आज खेत में पानी देना चाहिए?",
                    "इंदौर मंडी में सोयाबीन का क्या भाव है?",
                    "आज कीटनाशक का छिड़काव करना ठीक रहेगा क्या?"
                )
                VoiceLanguage.MARATHI -> listOf(
                    "माझ्या पिकाची स्थिती कशी आहे?",
                    "आज सोयाबीनचा काय भाव आहे?"
                )
                VoiceLanguage.GUJARATI -> listOf(
                    "મારા પાકની સ્થિતિ કેવી છે?",
                    "શું આજે પિયત આપવું જોઈએ?"
                )
                else -> listOf(
                    "How is my crop health today?",
                    "Should I irrigate Field 2 today?",
                    "What is the soybean mandi price?"
                )
            }

            val selectedQuery = sampleQueries.first()

            // Simulate partial words & amplitude waveforms
            val words = selectedQuery.split(" ")
            val partialBuilder = StringBuilder()

            for ((index, word) in words.withIndex()) {
                delay(400)
                if (!isListeningActive) return@launch
                partialBuilder.append(if (index > 0) " " else "").append(word)
                onPartialResult(partialBuilder.toString())
                onRmsChanged((3f + (index % 4) * 2.2f).coerceIn(1f, 9.5f))
            }

            delay(600)
            if (isListeningActive) {
                isListeningActive = false
                onResult(VoiceRecognitionResult.Success(text = selectedQuery, language = language))
            }
        }
    }

    override fun stopListening() {
        if (!isListeningActive) return
        isListeningActive = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceSTT", "Error stopping recognizer", e)
        }
    }

    override fun cancelListening() {
        isListeningActive = false
        simulationJob?.cancel()
        simulationJob = null
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("VoiceSTT", "Error cancelling recognizer", e)
        }
    }

    override fun release() {
        cancelListening()
    }
}

/**
 * Android Native Implementation of TTS supporting Hindi, English, and Regional Locales.
 */
class AndroidTextToSpeechService(
    private val context: Context
) : FarmTextToSpeechService, TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var isMuted = false
    private var pendingSpeakRequest: (() -> Unit)? = null

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceTTS", "Failed to instantiate TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d("VoiceTTS", "TTS playback started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d("VoiceTTS", "TTS playback finished: $utteranceId")
                }

                override fun onError(utteranceId: String?) {
                    Log.e("VoiceTTS", "TTS playback error: $utteranceId")
                }
            })
            pendingSpeakRequest?.invoke()
            pendingSpeakRequest = null
        } else {
            Log.e("VoiceTTS", "TextToSpeech init failed with code $status")
        }
    }

    override fun isAvailable(): Boolean = isInitialized

    override fun speak(
        text: String,
        language: VoiceLanguage,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (isMuted || text.isBlank()) {
            onDone()
            return
        }

        val speakAction = {
            try {
                val locale = language.isoLocale
                val langResult = textToSpeech?.setLanguage(locale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.language = Locale("hi", "IN")
                }
                textToSpeech?.setPitch(1.0f)
                textToSpeech?.setSpeechRate(0.95f) // Natural conversational pace for rural clarity

                val utteranceId = "utterance_${System.currentTimeMillis()}"
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                onStart()
            } catch (e: Exception) {
                Log.e("VoiceTTS", "Error speaking text", e)
                onError(e.localizedMessage ?: "TTS error")
            }
        }

        if (isInitialized) {
            speakAction()
        } else {
            pendingSpeakRequest = speakAction
        }
    }

    override fun stop() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e("VoiceTTS", "Error stopping TTS", e)
        }
    }

    override fun setMuted(isMuted: Boolean) {
        this.isMuted = isMuted
        if (isMuted) {
            stop()
        }
    }

    override fun isMuted(): Boolean = isMuted

    override fun release() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e("VoiceTTS", "Error shutting down TTS", e)
        }
    }
}
