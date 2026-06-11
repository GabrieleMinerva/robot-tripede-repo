package com.robottripede.app.assistant

import com.robottripede.app.privacy.AssistantPayload

class MockAssistantClient : AssistantClient {
    override suspend fun respond(payload: AssistantPayload): AssistantResponse {
        val command = payload.userText.ifBlank { "nessun comando" }
        return AssistantResponse(
            message = "Mock AI: ho ricevuto '$command'. Payload privacy-safe con ${payload.telemetrySummary}.",
        )
    }
}
