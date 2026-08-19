package com.example.core.navigation

sealed class Screen(val route: String, val title: String) {
    // Initial Flow & Authentication
    object Splash : Screen("splash", "FarmSathi")
    object Onboarding : Screen("onboarding", "Welcome to FarmSathi")
    object Auth : Screen("auth", "Farmer Sign In")
    object ProfileSetup : Screen("profile_setup", "Farmer Profile Setup")
    object FarmSetup : Screen("farm_setup", "Farm Setup")

    // Top-Level Bottom Navigation Tabs
    object Home : Screen("home", "Home")
    object Farm : Screen("farm", "My Farm")
    object AgroSathiAi : Screen("agrosathi_ai", "FarmSathi AI")
    object Market : Screen("market", "Mandi Market")
    object Profile : Screen("profile", "Profile")
    
    // Feature Sub-screens
    object Telemetry : Screen("telemetry", "IoT Telemetry")
    object DiseaseDetection : Screen("disease_detection", "Crop Doctor AI")
    object SoilHealth : Screen("soil_health", "Soil & NPK Health")
    object SmartIrrigation : Screen("smart_irrigation", "Smart Irrigation")
    object WeatherIntelligence : Screen("weather_intelligence", "Weather Intelligence")
    object Forecasting : Screen("forecasting", "Yield Forecasting")
    object Buyers : Screen("buyers", "Buyer Matching")
    object Logistics : Screen("logistics", "Smart Logistics")
    object OutbreakRadar : Screen("outbreak_radar", "Outbreak Radar")
    object DigitalTwin : Screen("digital_twin", "Farm Digital Twin")
    object Notifications : Screen("notifications", "Notifications")
}
