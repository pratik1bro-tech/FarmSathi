package com.example.core.voice

import java.util.Locale

enum class VoiceLanguage(
    val code: String,
    val isoLocale: Locale,
    val nativeName: String,
    val englishName: String,
    val flagEmoji: String
) {
    HINDI("hi-IN", Locale("hi", "IN"), "हिन्दी", "Hindi", "🇮🇳"),
    ENGLISH("en-IN", Locale("en", "IN"), "English", "English", "🇮🇳"),
    MARATHI("mr-IN", Locale("mr", "IN"), "मराठी", "Marathi", "🇮🇳"),
    GUJARATI("gu-IN", Locale("gu", "IN"), "ગુજરાતી", "Gujarati", "🇮🇳"),
    PUNJABI("pa-IN", Locale("pa", "IN"), "ਪੰਜਾਬੀ", "Punjabi", "🇮🇳"),
    TELUGU("te-IN", Locale("te", "IN"), "తెలుగు", "Telugu", "🇮🇳"),
    TAMIL("ta-IN", Locale("ta", "IN"), "தமிழ்", "Tamil", "🇮🇳"),
    KANNADA("kn-IN", Locale("kn", "IN"), "ಕನ್ನಡ", "Kannada", "🇮🇳");

    companion object {
        fun fromCode(code: String): VoiceLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) || it.code.startsWith(code, ignoreCase = true) }
                ?: HINDI
        }
    }
}

enum class VoiceErrorCode(val userFriendlyHindi: String, val userFriendlyEnglish: String) {
    NO_SPEECH("कोई आवाज सुनाई नहीं दी। कृपया दोबारा बोलें।", "No speech detected. Please speak clearly into microphone."),
    NETWORK_UNAVAILABLE("इंटरनेट कनेक्शन धीमा है। ऑफलाइन वॉयस मोड सक्रिय है।", "Internet disconnected. Local speech mode active."),
    PERMISSION_DENIED("माइक्रोफोन की अनुमति आवश्यक है।", "Microphone permission is required to speak."),
    MIC_BUSY("माइक्रोफोन व्यस्त है। कृपया पुनः प्रयास करें।", "Microphone is busy. Please try again."),
    TIMEOUT("समय समाप्त हो गया। कृपया दोबारा बोलें।", "Listening timed out. Please speak again."),
    ENGINE_UNAVAILABLE("वॉयस सर्विस लोड हो रही है।", "Voice engine initializing.");
}

sealed class VoiceRecognitionResult {
    data class PartialResult(val text: String, val rmsDb: Float) : VoiceRecognitionResult()
    data class Success(val text: String, val confidence: Float = 1.0f, val language: VoiceLanguage) : VoiceRecognitionResult()
    data class Error(val code: VoiceErrorCode, val detail: String) : VoiceRecognitionResult()
}

sealed class VoiceSessionState {
    object Idle : VoiceSessionState()
    object RequestingPermission : VoiceSessionState()
    data class Listening(val partialText: String = "", val rmsDb: Float = 0f, val language: VoiceLanguage) : VoiceSessionState()
    data class Processing(val text: String) : VoiceSessionState()
    data class Speaking(val text: String, val language: VoiceLanguage) : VoiceSessionState()
    data class Error(val code: VoiceErrorCode, val message: String, val canRetry: Boolean = true) : VoiceSessionState()
}
