package com.example.core.constants

object AppConstants {
    const val APP_NAME = "FarmSathi"
    const val TAGLINE = "Your AI-Powered Farm Companion"
    
    // Supported Languages
    val SUPPORTED_LANGUAGES = listOf(
        LanguageItem("en", "English", "English"),
        LanguageItem("hi", "हिन्दी", "Hindi"),
        LanguageItem("mr", "मराठी", "Marathi"),
        LanguageItem("te", "తెలుగు", "Telugu"),
        LanguageItem("pa", "ਪੰਜਾਬੀ", "Punjabi"),
        LanguageItem("gu", "ગુજરાતી", "Gujarati")
    )
    
    // Default farmer location
    const val DEFAULT_LOCATION = "Indore Mandi Region, MP"
    const val DEFAULT_FARM_SIZE = "4.5 Acres"
}

data class LanguageItem(
    val code: String,
    val nativeName: String,
    val englishName: String
)
