package com.robottripede.app.domain.memory

data class RobotProfile(
    val robotId: String,
    val robotName: String,
    val createdAt: Long,
    val lastActivatedAt: Long,
    val identityVersion: Int,
    val appVersionAtCreation: String?,
    val description: String?,
    val ownerLabel: String?,
)

data class ConversationMemory(
    val id: String,
    val startedAt: Long,
    val endedAt: Long?,
    val participantIds: List<String>,
    val title: String?,
    val transcriptSummary: String,
    val fullTranscriptLocalPath: String?,
    val tags: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val sender: String,
    val text: String,
    val createdAt: Long,
)

data class PersonMemory(
    val id: String,
    val displayName: String,
    val nickname: String?,
    val consentFaceRecognition: Boolean,
    val consentVoiceRecognition: Boolean,
    val consentConversationMemory: Boolean,
    val notes: List<String>,
    val preferences: List<PersonPreference>,
    val firstSeenAt: Long,
    val lastSeenAt: Long,
    val recognitionConfidence: Float?,
)

data class PersonPreference(
    val id: String,
    val personId: String,
    val category: String,
    val value: String,
    val confidence: Float,
    val sourceConversationId: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

data class RobotPersonalityMemory(
    val id: String,
    val name: String,
    val tone: String,
    val behaviorRules: List<String>,
    val forbiddenBehaviors: List<String>,
    val updatedAt: Long,
)

data class RelationshipMemory(
    val id: String,
    val personId: String,
    val relationshipLabel: String?,
    val familiarityLevel: Int,
    val trustLevel: Int,
    val preferredTone: String?,
    val recurringTopics: List<String>,
    val lastInteractionSummary: String?,
    val updatedAt: Long,
)

data class EvolutionLogEntry(
    val id: String,
    val timestamp: Long,
    val type: String,
    val title: String,
    val description: String,
    val relatedPersonId: String?,
    val relatedConversationId: String?,
)

data class PrivacySettings(
    val consentConversationMemory: Boolean,
    val consentPersonProfile: Boolean,
    val consentFaceRecognition: Boolean,
    val consentVoiceRecognition: Boolean,
    val consentSendImagesToAI: Boolean,
    val consentSaveFullTranscript: Boolean,
    val consentSaveAudio: Boolean,
)

data class RobotMemorySnapshot(
    val profile: RobotProfile,
    val personality: RobotPersonalityMemory,
    val persons: List<PersonMemory>,
    val relationships: List<RelationshipMemory>,
    val conversations: List<ConversationMemory>,
    val messages: List<ChatMessage>,
    val preferences: List<PersonPreference>,
    val privacySettings: PrivacySettings,
    val evolutionLog: List<EvolutionLogEntry>,
) {
    companion object {
        fun create(now: Long, robotId: String): RobotMemorySnapshot {
            return RobotMemorySnapshot(
                profile = RobotProfile(
                    robotId = robotId,
                    robotName = "Robot Tripede",
                    createdAt = now,
                    lastActivatedAt = now,
                    identityVersion = 1,
                    appVersionAtCreation = "0.1.0",
                    description = "Compagno di laboratorio su Samsung Galaxy S20",
                    ownerLabel = null,
                ),
                personality = RobotPersonalityMemory(
                    id = "default-personality",
                    name = "Robot Tripede",
                    tone = "curioso, pratico, leggermente ironico",
                    behaviorRules = listOf(
                        "Aiuta a costruire il progetto passo dopo passo",
                        "Chiede conferma prima di ricordare informazioni personali",
                        "Non finge sensori o motori non disponibili",
                    ),
                    forbiddenBehaviors = listOf(
                        "Non inviare video continuo",
                        "Non inviare audio grezzo continuo",
                        "Non inviare comandi hardware reali",
                        "Non riconoscere volti o voci senza consenso",
                    ),
                    updatedAt = now,
                ),
                persons = emptyList(),
                relationships = emptyList(),
                conversations = emptyList(),
                messages = emptyList(),
                preferences = emptyList(),
                privacySettings = PrivacySettings(
                    consentConversationMemory = true,
                    consentPersonProfile = true,
                    consentFaceRecognition = false,
                    consentVoiceRecognition = false,
                    consentSendImagesToAI = false,
                    consentSaveFullTranscript = false,
                    consentSaveAudio = false,
                ),
                evolutionLog = listOf(
                    EvolutionLogEntry(
                        id = "created-$robotId",
                        timestamp = now,
                        type = "identity_created",
                        title = "Identita creata",
                        description = "Prima identita locale del Robot Tripede creata sul telefono.",
                        relatedPersonId = null,
                        relatedConversationId = null,
                    ),
                ),
            )
        }
    }
}
