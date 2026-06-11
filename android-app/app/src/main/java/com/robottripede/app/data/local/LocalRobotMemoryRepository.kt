package com.robottripede.app.data.local

import android.content.Context
import com.robottripede.app.domain.memory.ChatMessage
import com.robottripede.app.domain.memory.ConversationMemory
import com.robottripede.app.domain.memory.EvolutionLogEntry
import com.robottripede.app.domain.memory.PersonMemory
import com.robottripede.app.domain.memory.PrivacySettings
import com.robottripede.app.domain.memory.RelationshipMemory
import com.robottripede.app.domain.memory.RobotMemoryRepository
import com.robottripede.app.domain.memory.RobotMemorySnapshot
import java.util.UUID

class LocalRobotMemoryRepository(context: Context) : RobotMemoryRepository {
    private val preferences = context.getSharedPreferences("robot_memory", Context.MODE_PRIVATE)

    override fun loadSnapshot(): RobotMemorySnapshot {
        val stored = preferences.getString(KEY_SNAPSHOT, null)
        if (stored != null) {
            return RobotMemoryJson.decode(stored)
        }
        val snapshot = RobotMemorySnapshot.create(now = System.currentTimeMillis(), robotId = "robot-${UUID.randomUUID()}")
        replaceSnapshot(snapshot)
        return snapshot
    }

    override fun replaceSnapshot(snapshot: RobotMemorySnapshot) {
        preferences.edit().putString(KEY_SNAPSHOT, RobotMemoryJson.encode(snapshot)).apply()
    }

    override fun updateRobotName(name: String) {
        mutate("robot_renamed", "Nome robot aggiornato", "Nuovo nome: $name") { snapshot, now ->
            snapshot.copy(
                profile = snapshot.profile.copy(robotName = name.ifBlank { "Robot Tripede" }, lastActivatedAt = now),
                personality = snapshot.personality.copy(name = name.ifBlank { "Robot Tripede" }, updatedAt = now),
            )
        }
    }

    override fun savePrivacySettings(settings: PrivacySettings) {
        mutate("privacy_updated", "Privacy aggiornata", "Impostazioni consenso aggiornate localmente.") { snapshot, now ->
            snapshot.copy(privacySettings = settings, profile = snapshot.profile.copy(lastActivatedAt = now))
        }
    }

    override fun createPerson(displayName: String): PersonMemory {
        val now = System.currentTimeMillis()
        val person = PersonMemory(
            id = "person-${UUID.randomUUID()}",
            displayName = displayName.ifBlank { "Persona senza nome" },
            nickname = null,
            consentFaceRecognition = false,
            consentVoiceRecognition = false,
            consentConversationMemory = true,
            notes = emptyList(),
            preferences = emptyList(),
            firstSeenAt = now,
            lastSeenAt = now,
            recognitionConfidence = null,
        )
        mutate("person_created", "Persona creata", "Profilo manuale creato per ${person.displayName}.", relatedPersonId = person.id) { snapshot, _ ->
            snapshot.copy(
                persons = snapshot.persons + person,
                relationships = snapshot.relationships + RelationshipMemory(
                    id = "relationship-${UUID.randomUUID()}",
                    personId = person.id,
                    relationshipLabel = null,
                    familiarityLevel = 1,
                    trustLevel = 1,
                    preferredTone = null,
                    recurringTopics = emptyList(),
                    lastInteractionSummary = null,
                    updatedAt = now,
                ),
            )
        }
        return person
    }

    override fun deletePerson(personId: String) {
        mutate("person_deleted", "Persona eliminata", "Profilo persona eliminato localmente.") { snapshot, _ ->
            snapshot.copy(
                persons = snapshot.persons.filterNot { it.id == personId },
                relationships = snapshot.relationships.filterNot { it.personId == personId },
                conversations = snapshot.conversations.map { conversation ->
                    conversation.copy(participantIds = conversation.participantIds.filterNot { it == personId })
                },
            )
        }
    }

    override fun startConversation(personId: String?): ConversationMemory {
        val now = System.currentTimeMillis()
        val conversation = ConversationMemory(
            id = "conversation-${UUID.randomUUID()}",
            startedAt = now,
            endedAt = null,
            participantIds = listOfNotNull(personId),
            title = "Conversazione Live Robot",
            transcriptSummary = "",
            fullTranscriptLocalPath = null,
            tags = listOf("live_robot", "mock_ai"),
            createdAt = now,
            updatedAt = now,
        )
        mutate("conversation_started", "Conversazione avviata", "Nuova conversazione Live Robot.", personId, conversation.id) { snapshot, _ ->
            snapshot.copy(conversations = snapshot.conversations + conversation)
        }
        return conversation
    }

    override fun appendMessage(conversationId: String, sender: String, text: String): ChatMessage {
        val now = System.currentTimeMillis()
        val message = ChatMessage(
            id = "message-${UUID.randomUUID()}",
            conversationId = conversationId,
            sender = sender,
            text = text.take(1000),
            createdAt = now,
        )
        mutateWithoutLog { snapshot ->
            snapshot.copy(
                messages = snapshot.messages + message,
                conversations = snapshot.conversations.map { conversation ->
                    if (conversation.id == conversationId) {
                        conversation.copy(updatedAt = now)
                    } else {
                        conversation
                    }
                },
            )
        }
        return message
    }

    override fun finishConversation(conversationId: String, summary: String) {
        mutate("conversation_saved", "Conversazione salvata", "Sintesi locale aggiornata.", relatedConversationId = conversationId) { snapshot, now ->
            snapshot.copy(
                conversations = snapshot.conversations.map { conversation ->
                    if (conversation.id == conversationId) {
                        conversation.copy(endedAt = now, transcriptSummary = summary.take(500), updatedAt = now)
                    } else {
                        conversation
                    }
                },
            )
        }
    }

    override fun deleteConversation(conversationId: String) {
        mutate("conversation_deleted", "Conversazione eliminata", "Conversazione e messaggi locali eliminati.") { snapshot, _ ->
            snapshot.copy(
                conversations = snapshot.conversations.filterNot { it.id == conversationId },
                messages = snapshot.messages.filterNot { it.conversationId == conversationId },
            )
        }
    }

    override fun clearMemory() {
        val current = loadSnapshot()
        replaceSnapshot(
            RobotMemorySnapshot.create(now = System.currentTimeMillis(), robotId = current.profile.robotId).copy(
                profile = current.profile,
                personality = current.personality,
            ),
        )
    }

    override fun deleteIdentity() {
        preferences.edit().remove(KEY_SNAPSHOT).apply()
        loadSnapshot()
    }

    private fun mutate(
        type: String,
        title: String,
        description: String,
        relatedPersonId: String? = null,
        relatedConversationId: String? = null,
        block: (RobotMemorySnapshot, Long) -> RobotMemorySnapshot,
    ) {
        val now = System.currentTimeMillis()
        val updated = block(loadSnapshot(), now)
        replaceSnapshot(
            updated.copy(
                evolutionLog = updated.evolutionLog + EvolutionLogEntry(
                    id = "event-${UUID.randomUUID()}",
                    timestamp = now,
                    type = type,
                    title = title,
                    description = description,
                    relatedPersonId = relatedPersonId,
                    relatedConversationId = relatedConversationId,
                ),
            ),
        )
    }

    private fun mutateWithoutLog(block: (RobotMemorySnapshot) -> RobotMemorySnapshot) {
        replaceSnapshot(block(loadSnapshot()))
    }

    companion object {
        private const val KEY_SNAPSHOT = "snapshot_json"
    }
}
