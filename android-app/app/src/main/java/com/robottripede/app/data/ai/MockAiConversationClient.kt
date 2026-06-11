package com.robottripede.app.data.ai

import com.robottripede.app.domain.ai.AiConversationClient
import com.robottripede.app.domain.ai.AiConversationRequest
import com.robottripede.app.domain.ai.AiConversationResponse
import com.robottripede.app.domain.ai.MemoryCandidate

class MockAiConversationClient : AiConversationClient {
    override fun respond(request: AiConversationRequest): AiConversationResponse {
        val person = request.selectedPersonName ?: "persona non selezionata"
        val text = request.userText.ifBlank { "messaggio vuoto" }
        return AiConversationResponse(
            assistantText = "Mock Live Robot: ho ricevuto \"$text\" da $person. Per ora uso solo testo locale e memoria sintetica.",
            memoryCandidates = listOf(
                MemoryCandidate(
                    type = "conversation_summary",
                    text = "Conversazione mock su: ${text.take(80)}",
                    requiresConfirmation = true,
                ),
            ),
            requiresImage = false,
            requiresMemoryConfirmation = true,
        )
    }
}
