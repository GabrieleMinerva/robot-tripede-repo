package com.robottripede.app.privacy

import com.robottripede.app.data.model.Esp32Telemetry

class PrivacyFilter {
    fun prepareTextOnlyPayload(userText: String, telemetry: Esp32Telemetry): AssistantPayload {
        return AssistantPayload(
            userText = sanitizeUserText(userText),
            telemetrySummary = "stop=${telemetry.stop}; imu=${telemetry.imu.tilt}; status=${telemetry.status}",
            includesRawAudio = false,
            includesRawVideo = false,
            includesImage = false,
        )
    }

    private fun sanitizeUserText(value: String): String {
        return value
            .lineSequence()
            .joinToString(" ") { it.trim() }
            .replace(Regex("\\s+"), " ")
            .take(500)
    }
}

data class AssistantPayload(
    val userText: String,
    val telemetrySummary: String,
    val includesRawAudio: Boolean,
    val includesRawVideo: Boolean,
    val includesImage: Boolean,
)
