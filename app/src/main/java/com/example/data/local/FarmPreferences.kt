package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class FarmPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isAuthenticated(): Boolean {
        return prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
    }

    fun setAuthenticated(authenticated: Boolean) {
        prefs.edit().putBoolean(KEY_IS_AUTHENTICATED, authenticated).apply()
    }

    fun isProfileSetupCompleted(): Boolean {
        return prefs.getBoolean(KEY_IS_PROFILE_SETUP_COMPLETED, false)
    }

    fun setProfileSetupCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_IS_PROFILE_SETUP_COMPLETED, completed).apply()
    }

    fun isFarmSetupCompleted(): Boolean {
        return prefs.getBoolean(KEY_IS_FARM_SETUP_COMPLETED, false)
    }

    fun setFarmSetupCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_IS_FARM_SETUP_COMPLETED, completed).apply()
    }

    fun getSavedPhone(): String {
        return prefs.getString(KEY_SAVED_PHONE, "") ?: ""
    }

    fun setSavedPhone(phone: String) {
        prefs.edit().putString(KEY_SAVED_PHONE, phone).apply()
    }

    fun getSavedName(): String {
        return prefs.getString(KEY_SAVED_NAME, "Rameshwar Patel") ?: "Rameshwar Patel"
    }

    fun setSavedName(name: String) {
        prefs.edit().putString(KEY_SAVED_NAME, name).apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_AUTHENTICATED, false)
            .putBoolean(KEY_IS_PROFILE_SETUP_COMPLETED, false)
            .putBoolean(KEY_IS_FARM_SETUP_COMPLETED, false)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "farmsathi_user_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_IS_AUTHENTICATED = "key_is_authenticated"
        private const val KEY_IS_PROFILE_SETUP_COMPLETED = "key_is_profile_setup_completed"
        private const val KEY_IS_FARM_SETUP_COMPLETED = "key_is_farm_setup_completed"
        private const val KEY_SAVED_PHONE = "key_saved_phone"
        private const val KEY_SAVED_NAME = "key_saved_name"
    }
}
