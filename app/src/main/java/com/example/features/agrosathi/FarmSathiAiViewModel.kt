package com.example.features.agrosathi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.voice.*
import com.example.data.models.*
import com.example.data.repository.FarmRepository
import com.example.data.service.DefaultFarmSathiAiService
import com.example.data.service.FarmSathiAiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class FarmSathiAiUiState(
    val messages: List<FarmAiMessage> = emptyList(),
    val orbState: OrbState = OrbState.IDLE,
    val voiceSessionState: VoiceSessionState = VoiceSessionState.Idle,
    val selectedVoiceLanguage: VoiceLanguage = VoiceLanguage.HINDI,
    val isTtsMuted: Boolean = false,
    val activePlayingMessageId: String? = null,
    val inputText: String = "",
    val activeLanguage: String = "hi",
    val isOffline: Boolean = false,
    val suggestedQuestions: List<String> = listOf(
        "What should I do today?",
        "How is my farm?",
        "Should I irrigate?",
        "Is my crop at risk?",
        "Should I sell now?",
        "Which buyer is best?",
        "What will happen if I delay irrigation?",
        "आज क्या करना चाहिए?",
        "मेरी फसल की हालत कैसी है?",
        "क्या आज सिंचाई करनी चाहिए?",
        "इंदौर मंडी में सोयाबीन का भाव क्या है?",
        "सबसे अच्छा खरीदार कौन सा है?"
    ),
    val context: FarmIntelligenceContext = FarmIntelligenceContext()
)

class FarmSathiAiViewModel(
    application: Application,
    private val repository: FarmRepository,
    private val aiService: FarmSathiAiService = DefaultFarmSathiAiService(),
    private val speechToTextService: FarmSpeechToTextService = AndroidSpeechToTextService(application.applicationContext),
    private val textToSpeechService: FarmTextToSpeechService = AndroidTextToSpeechService(application.applicationContext)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FarmSathiAiUiState())
    val uiState: StateFlow<FarmSathiAiUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    init {
        loadFarmContext()
    }

    private fun loadFarmContext() {
        viewModelScope.launch {
            combine(
                repository.getFarmerProfile(),
                repository.getCropFields(),
                repository.getTelemetryData(),
                repository.getSoilHealthReport(),
                repository.getWeatherForecast()
            ) { profile, fields, telemetry, soil, weather ->
                val mandi = repository.getMandiPrices().firstOrNull() ?: emptyList()
                val buyers = repository.getBuyerOffers().firstOrNull() ?: emptyList()
                val alerts = repository.getOutbreakAlerts().firstOrNull() ?: emptyList()

                FarmIntelligenceContext(
                    farmerProfile = profile,
                    cropFields = fields,
                    telemetry = telemetry,
                    soilHealth = soil,
                    weather = weather,
                    mandiPrices = mandi,
                    buyerOffers = buyers,
                    outbreakAlerts = alerts
                )
            }.collect { farmCtx ->
                val lang = if (farmCtx.farmerProfile.selectedLanguage == "hi") VoiceLanguage.HINDI else VoiceLanguage.ENGLISH
                _uiState.update { current ->
                    current.copy(
                        context = farmCtx,
                        activeLanguage = farmCtx.farmerProfile.selectedLanguage,
                        selectedVoiceLanguage = lang
                    )
                }

                if (_uiState.value.messages.isEmpty()) {
                    val briefing = aiService.generateDailyProactiveBriefing(farmCtx)
                    _uiState.update { it.copy(messages = listOf(briefing)) }
                }
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun setVoiceLanguage(language: VoiceLanguage) {
        _uiState.update { it.copy(selectedVoiceLanguage = language, activeLanguage = language.code.take(2)) }
        if (_uiState.value.voiceSessionState is VoiceSessionState.Listening) {
            startVoiceSession(hasPermission = true)
        }
    }

    fun toggleTtsMute() {
        val nextMuted = !_uiState.value.isTtsMuted
        _uiState.update { it.copy(isTtsMuted = nextMuted) }
        textToSpeechService.setMuted(nextMuted)
    }

    // ---------------- VOICE RECOGNITION PIPELINE ----------------

    fun startVoiceSession(hasPermission: Boolean) {
        if (!hasPermission) {
            _uiState.update { it.copy(voiceSessionState = VoiceSessionState.RequestingPermission) }
            return
        }

        val lang = _uiState.value.selectedVoiceLanguage
        _uiState.update {
            it.copy(
                orbState = OrbState.LISTENING,
                voiceSessionState = VoiceSessionState.Listening(language = lang)
            )
        }

        speechToTextService.startListening(
            language = lang,
            onRmsChanged = { rms ->
                _uiState.update { state ->
                    val cur = state.voiceSessionState
                    if (cur is VoiceSessionState.Listening) {
                        state.copy(voiceSessionState = cur.copy(rmsDb = rms))
                    } else state
                }
            },
            onPartialResult = { partial ->
                _uiState.update { state ->
                    val cur = state.voiceSessionState
                    if (cur is VoiceSessionState.Listening) {
                        state.copy(voiceSessionState = cur.copy(partialText = partial))
                    } else state
                }
            },
            onResult = { result ->
                when (result) {
                    is VoiceRecognitionResult.Success -> {
                        _uiState.update {
                            it.copy(voiceSessionState = VoiceSessionState.Processing(result.text))
                        }
                        sendMessage(result.text)
                    }
                    is VoiceRecognitionResult.Error -> {
                        _uiState.update {
                            it.copy(
                                orbState = if (it.isOffline) OrbState.OFFLINE else OrbState.IDLE,
                                voiceSessionState = VoiceSessionState.Error(
                                    code = result.code,
                                    message = result.detail,
                                    canRetry = true
                                )
                            )
                        }
                    }
                    else -> {}
                }
            }
        )
    }

    fun stopVoiceListening() {
        speechToTextService.stopListening()
    }

    fun cancelVoiceSession() {
        speechToTextService.cancelListening()
        textToSpeechService.stop()
        _uiState.update {
            it.copy(
                orbState = if (it.isOffline) OrbState.OFFLINE else OrbState.IDLE,
                voiceSessionState = VoiceSessionState.Idle,
                activePlayingMessageId = null
            )
        }
    }

    fun retryVoiceSession() {
        startVoiceSession(hasPermission = true)
    }

    // ---------------- MESSAGE PROCESSING & TTS PIPELINE ----------------

    fun sendMessage(text: String? = null) {
        val query = (text ?: _uiState.value.inputText).trim()
        if (query.isBlank()) return

        val userMessage = FarmAiMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            text = query,
            timestamp = timeFormat.format(Date())
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                orbState = OrbState.THINKING
            )
        }

        viewModelScope.launch {
            val response = aiService.processQuery(query, _uiState.value.context)

            _uiState.update {
                it.copy(
                    messages = it.messages + response,
                    orbState = OrbState.SPEAKING,
                    activePlayingMessageId = response.id,
                    voiceSessionState = if (it.voiceSessionState !is VoiceSessionState.Idle) {
                        VoiceSessionState.Speaking(text = response.text, language = it.selectedVoiceLanguage)
                    } else VoiceSessionState.Idle
                )
            }

            // Speak answer aloud if not muted
            if (!_uiState.value.isTtsMuted) {
                textToSpeechService.speak(
                    text = response.text,
                    language = _uiState.value.selectedVoiceLanguage,
                    onStart = {
                        _uiState.update { it.copy(orbState = OrbState.SPEAKING) }
                    },
                    onDone = {
                        _uiState.update {
                            it.copy(
                                orbState = if (it.isOffline) OrbState.OFFLINE else OrbState.IDLE,
                                activePlayingMessageId = null,
                                voiceSessionState = VoiceSessionState.Idle
                            )
                        }
                    },
                    onError = {
                        _uiState.update {
                            it.copy(
                                orbState = if (it.isOffline) OrbState.OFFLINE else OrbState.IDLE,
                                activePlayingMessageId = null,
                                voiceSessionState = VoiceSessionState.Idle
                            )
                        }
                    }
                )
            } else {
                delay(1200)
                _uiState.update {
                    it.copy(
                        orbState = if (it.isOffline) OrbState.OFFLINE else OrbState.IDLE,
                        activePlayingMessageId = null,
                        voiceSessionState = VoiceSessionState.Idle
                    )
                }
            }
        }
    }

    fun scanCropWithCamera() {
        _uiState.update { it.copy(orbState = OrbState.THINKING) }

        val isHindi = _uiState.value.selectedVoiceLanguage == VoiceLanguage.HINDI
        val userPhotoMsg = FarmAiMessage(
            id = "user_photo_${System.currentTimeMillis()}",
            isUser = true,
            text = if (isHindi) "📷 [फोटो स्कैन: खेत 1 सोयाबीन पत्ती JS-2034]" else "📷 [Captured Crop Photo: Soybean Leaf JS-2034 in Field 1]",
            timestamp = timeFormat.format(Date())
        )

        _uiState.update { it.copy(messages = it.messages + userPhotoMsg) }

        viewModelScope.launch {
            val diagnosis = aiService.analyzeCropImagePrompt("Soybean Yellow Mosaic check", _uiState.value.context)
            _uiState.update {
                it.copy(
                    messages = it.messages + diagnosis,
                    orbState = OrbState.SPEAKING,
                    activePlayingMessageId = diagnosis.id
                )
            }

            if (!_uiState.value.isTtsMuted) {
                textToSpeechService.speak(
                    text = diagnosis.text,
                    language = _uiState.value.selectedVoiceLanguage,
                    onDone = {
                        _uiState.update {
                            it.copy(
                                orbState = if (it.isOffline) OrbState.OFFLINE else OrbState.IDLE,
                                activePlayingMessageId = null
                            )
                        }
                    }
                )
            } else {
                delay(2000)
                _uiState.update {
                    it.copy(
                        orbState = if (it.isOffline) OrbState.OFFLINE else OrbState.IDLE,
                        activePlayingMessageId = null
                    )
                }
            }
        }
    }

    fun toggleAudioPlayback(messageId: String) {
        val isCurrentlyPlaying = _uiState.value.activePlayingMessageId == messageId
        if (isCurrentlyPlaying) {
            textToSpeechService.stop()
            _uiState.update {
                it.copy(
                    activePlayingMessageId = null,
                    orbState = if (it.isOffline) OrbState.OFFLINE else OrbState.IDLE
                )
            }
        } else {
            val msg = _uiState.value.messages.find { it.id == messageId } ?: return
            _uiState.update {
                it.copy(
                    activePlayingMessageId = messageId,
                    orbState = OrbState.SPEAKING
                )
            }

            textToSpeechService.speak(
                text = msg.text,
                language = _uiState.value.selectedVoiceLanguage,
                onDone = {
                    _uiState.update {
                        if (it.activePlayingMessageId == messageId) {
                            it.copy(
                                activePlayingMessageId = null,
                                orbState = if (it.isOffline) OrbState.OFFLINE else OrbState.IDLE
                            )
                        } else it
                    }
                },
                onError = {
                    _uiState.update {
                        it.copy(
                            activePlayingMessageId = null,
                            orbState = if (it.isOffline) OrbState.OFFLINE else OrbState.IDLE
                        )
                    }
                }
            )
        }
    }

    fun toggleOfflineMode() {
        val nextOffline = !_uiState.value.isOffline
        _uiState.update {
            it.copy(
                isOffline = nextOffline,
                orbState = if (nextOffline) OrbState.OFFLINE else OrbState.IDLE
            )
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            val freshBriefing = aiService.generateDailyProactiveBriefing(_uiState.value.context)
            _uiState.update { it.copy(messages = listOf(freshBriefing), orbState = OrbState.IDLE) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechToTextService.release()
        textToSpeechService.release()
    }
}
