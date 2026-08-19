package com.example.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val phoneNumber: String = "9826044123",
    val otpCode: String = "",
    val isOtpSent: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val formattedPhone: String = "",
    val resendCountdown: Int = 30,
    val canResend: Boolean = false,
    val testOtpHint: String = "123456",
    val isAuthenticated: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                when (state) {
                    is AuthState.Unauthenticated -> {
                        _uiState.update {
                            it.copy(
                                isOtpSent = false,
                                isLoading = false,
                                isAuthenticated = false,
                                errorMessage = null
                            )
                        }
                    }
                    is AuthState.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is AuthState.OtpSent -> {
                        _uiState.update {
                            it.copy(
                                isOtpSent = true,
                                isLoading = false,
                                formattedPhone = state.formattedPhone,
                                testOtpHint = state.testOtpHint,
                                errorMessage = null,
                                canResend = false,
                                resendCountdown = 30
                            )
                        }
                        startCountdown()
                    }
                    is AuthState.Authenticated -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                errorMessage = null
                            )
                        }
                    }
                    is AuthState.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = state.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onPhoneChanged(phone: String) {
        val filtered = phone.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(phoneNumber = filtered, errorMessage = null) }
    }

    fun onOtpChanged(otp: String) {
        val filtered = otp.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(otpCode = filtered, errorMessage = null) }
    }

    fun sendOtp() {
        viewModelScope.launch {
            authRepository.sendOtp(_uiState.value.phoneNumber)
        }
    }

    fun verifyOtp() {
        viewModelScope.launch {
            authRepository.verifyOtp(_uiState.value.otpCode)
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            authRepository.resendOtp()
        }
    }

    fun editPhoneNumber() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                isOtpSent = false,
                otpCode = "",
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 30 downTo 1) {
                _uiState.update { it.copy(resendCountdown = i, canResend = false) }
                delay(1000)
            }
            _uiState.update { it.copy(resendCountdown = 0, canResend = true) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
