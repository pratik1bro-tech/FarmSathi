package com.example.data.repository

import com.example.data.local.FarmPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface AuthState {
    object Unauthenticated : AuthState
    data class OtpSent(
        val phoneNumber: String,
        val formattedPhone: String,
        val resendSecondsLeft: Int = 30,
        val testOtpHint: String = "123456"
    ) : AuthState
    object Loading : AuthState
    data class Authenticated(val phoneNumber: String) : AuthState
    data class Error(val message: String, val previousState: AuthState = Unauthenticated) : AuthState
}

interface AuthRepository {
    val authState: Flow<AuthState>
    suspend fun sendOtp(phoneNumber: String): Result<String>
    suspend fun verifyOtp(otp: String): Result<Boolean>
    suspend fun resendOtp(): Result<String>
    fun logout()
    fun isUserLoggedIn(): Boolean
    fun getSavedPhoneNumber(): String
}

class AuthRepositoryImpl(
    private val preferences: FarmPreferences
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(
        if (preferences.isAuthenticated()) {
            AuthState.Authenticated(preferences.getSavedPhone().ifEmpty { "+91 98260 44123" })
        } else {
            AuthState.Unauthenticated
        }
    )
    override val authState: Flow<AuthState> = _authState.asStateFlow()

    private var currentPhone: String = preferences.getSavedPhone()
    private var generatedTestOtp: String = "123456"

    override suspend fun sendOtp(phoneNumber: String): Result<String> {
        val cleanPhone = phoneNumber.trim().replace(" ", "").replace("-", "").replace("+91", "")
        
        // Mobile Number Validation
        if (cleanPhone.length != 10) {
            val error = "Please enter a valid 10-digit mobile number."
            _authState.value = AuthState.Error(error, AuthState.Unauthenticated)
            return Result.failure(IllegalArgumentException(error))
        }
        if (!cleanPhone.all { it.isDigit() }) {
            val error = "Mobile number must only contain digits."
            _authState.value = AuthState.Error(error, AuthState.Unauthenticated)
            return Result.failure(IllegalArgumentException(error))
        }
        if (!cleanPhone.startsWith("6") && !cleanPhone.startsWith("7") && !cleanPhone.startsWith("8") && !cleanPhone.startsWith("9")) {
            val error = "Please enter a valid Indian mobile number starting with 6, 7, 8, or 9."
            _authState.value = AuthState.Error(error, AuthState.Unauthenticated)
            return Result.failure(IllegalArgumentException(error))
        }

        _authState.value = AuthState.Loading
        currentPhone = cleanPhone
        preferences.setSavedPhone("+91 $cleanPhone")

        // Simulate network/SMS gateway delay (Firebase/FastAPI ready)
        delay(800)

        // For dev verification, we provide standard 6-digit test code
        generatedTestOtp = "123456"
        _authState.value = AuthState.OtpSent(
            phoneNumber = cleanPhone,
            formattedPhone = "+91 $cleanPhone",
            resendSecondsLeft = 30,
            testOtpHint = generatedTestOtp
        )
        return Result.success("OTP sent successfully to +91 $cleanPhone")
    }

    override suspend fun verifyOtp(otp: String): Result<Boolean> {
        val cleanOtp = otp.trim().replace(" ", "")

        if (cleanOtp.length != 6 || !cleanOtp.all { it.isDigit() }) {
            val error = "Please enter a valid 6-digit OTP."
            _authState.value = AuthState.Error(
                error,
                AuthState.OtpSent(currentPhone, "+91 $currentPhone", 0, generatedTestOtp)
            )
            return Result.failure(IllegalArgumentException(error))
        }

        _authState.value = AuthState.Loading
        delay(700)

        // Accept test OTP "123456" or any matching 6-digit code for dev testing
        if (cleanOtp == generatedTestOtp || cleanOtp.length == 6) {
            preferences.setAuthenticated(true)
            _authState.value = AuthState.Authenticated("+91 $currentPhone")
            return Result.success(true)
        } else {
            val error = "Invalid OTP entered. Please try again or use 123456."
            _authState.value = AuthState.Error(
                error,
                AuthState.OtpSent(currentPhone, "+91 $currentPhone", 0, generatedTestOtp)
            )
            return Result.failure(IllegalArgumentException(error))
        }
    }

    override suspend fun resendOtp(): Result<String> {
        if (currentPhone.isBlank()) {
            return Result.failure(IllegalStateException("No phone number registered"))
        }
        _authState.value = AuthState.Loading
        delay(600)
        _authState.value = AuthState.OtpSent(
            phoneNumber = currentPhone,
            formattedPhone = "+91 $currentPhone",
            resendSecondsLeft = 30,
            testOtpHint = generatedTestOtp
        )
        return Result.success("New OTP sent to +91 $currentPhone")
    }

    override fun logout() {
        preferences.logout()
        currentPhone = ""
        _authState.value = AuthState.Unauthenticated
    }

    override fun isUserLoggedIn(): Boolean = preferences.isAuthenticated()

    override fun getSavedPhoneNumber(): String = preferences.getSavedPhone()
}
