package com.mrmustard.activelistening.data.guidance

data class OpenAiGuidanceConfig(
    val apiKey: String,
    val model: String,
) {
    val isConfigured: Boolean = apiKey.isNotBlank()
}

