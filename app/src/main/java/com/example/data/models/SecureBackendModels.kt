package com.example.data.models

data class BackendAiRequest(
    val firebaseAuthToken: String,
    val farmId: String,
    val userQuery: String,
    val languageCode: String = "hi",
    val requestTimestampMs: Long = System.currentTimeMillis()
)

data class BackendStructuredResponse(
    val responseText: String,
    val priority: String,
    val actions: List<FarmAiAction>,
    val warnings: List<String>,
    val supportingData: Map<String, String>,
    val uncertainty: String,
    val executionMetadata: BackendExecutionMetadata
)

data class BackendExecutionMetadata(
    val authStatus: String,
    val farmOwnershipVerified: Boolean,
    val rateLimitStatus: String,
    val dataIsolationVerified: Boolean,
    val geminiSecurityMode: String,
    val piiScrubbedLogging: Boolean,
    val latencyMs: Long
)
