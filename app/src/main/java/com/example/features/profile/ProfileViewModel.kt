package com.example.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.constants.AppConstants
import com.example.data.models.FarmerProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.FarmRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: FarmerProfile = FarmerProfile(),
    val isOfflineModeEnabled: Boolean = true,
    val isAutoSyncEnabled: Boolean = true,
    val showLanguageDialog: Boolean = false,
    val showLogoutDialog: Boolean = false
)

class ProfileViewModel(
    private val repository: FarmRepository,
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFarmerProfile().collect { profile ->
                _uiState.update { it.copy(profile = profile) }
            }
        }
    }

    fun selectLanguage(langCode: String) {
        repository.updateLanguage(langCode)
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    fun setLanguageDialogVisible(show: Boolean) {
        _uiState.update { it.copy(showLanguageDialog = show) }
    }

    fun setLogoutDialogVisible(show: Boolean) {
        _uiState.update { it.copy(showLogoutDialog = show) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        authRepository?.logout()
        _uiState.update { it.copy(showLogoutDialog = false) }
        onLoggedOut()
    }

    fun toggleOfflineMode(enabled: Boolean) {
        _uiState.update { it.copy(isOfflineModeEnabled = enabled) }
    }
}
