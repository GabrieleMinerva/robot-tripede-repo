package com.robottripede.app.domain.ai

import com.robottripede.app.domain.memory.RobotMemorySnapshot

interface AiConversationClient {
    fun respond(request: AiConversationRequest): AiConversationResponse
}

data class AiConversationRequest(
    val userText: String,
    val selectedPersonName: String?,
    val memorySnapshot: RobotMemorySnapshot,
)

data class AiConversationResponse(
    val assistantText: String,
    val memoryCandidates: List<MemoryCandidate>,
    val requiresImage: Boolean,
    val requiresMemoryConfirmation: Boolean,
)

data class MemoryCandidate(
    val type: String,
    val text: String,
    val requiresConfirmation: Boolean,
)
