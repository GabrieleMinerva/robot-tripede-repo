package com.robottripede.app.assistant

import com.robottripede.app.privacy.AssistantPayload

interface AssistantClient {
    suspend fun respond(payload: AssistantPayload): AssistantResponse
}

data class AssistantResponse(
    val message: String,
)
