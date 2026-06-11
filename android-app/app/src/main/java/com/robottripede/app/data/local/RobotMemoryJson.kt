package com.robottripede.app.data.local

import com.robottripede.app.domain.memory.ChatMessage
import com.robottripede.app.domain.memory.ConversationMemory
import com.robottripede.app.domain.memory.EvolutionLogEntry
import com.robottripede.app.domain.memory.PersonMemory
import com.robottripede.app.domain.memory.PersonPreference
import com.robottripede.app.domain.memory.PrivacySettings
import com.robottripede.app.domain.memory.RelationshipMemory
import com.robottripede.app.domain.memory.RobotMemorySnapshot
import com.robottripede.app.domain.memory.RobotPersonalityMemory
import com.robottripede.app.domain.memory.RobotProfile
import org.json.JSONArray
import org.json.JSONObject

object RobotMemoryJson {
    fun encode(snapshot: RobotMemorySnapshot): String {
        return JSONObject()
            .put("profile", profileToJson(snapshot.profile))
            .put("personality", personalityToJson(snapshot.personality))
            .put("persons", JSONArray(snapshot.persons.map(::personToJson)))
            .put("relationships", JSONArray(snapshot.relationships.map(::relationshipToJson)))
            .put("conversations", JSONArray(snapshot.conversations.map(::conversationToJson)))
            .put("messages", JSONArray(snapshot.messages.map(::messageToJson)))
            .put("preferences", JSONArray(snapshot.preferences.map(::preferenceToJson)))
            .put("privacySettings", privacyToJson(snapshot.privacySettings))
            .put("evolutionLog", JSONArray(snapshot.evolutionLog.map(::evolutionToJson)))
            .toString()
    }

    fun decode(value: String): RobotMemorySnapshot {
        val root = JSONObject(value)
        return RobotMemorySnapshot(
            profile = jsonToProfile(root.getJSONObject("profile")),
            personality = jsonToPersonality(root.getJSONObject("personality")),
            persons = root.getJSONArray("persons").mapObjects(::jsonToPerson),
            relationships = root.getJSONArray("relationships").mapObjects(::jsonToRelationship),
            conversations = root.getJSONArray("conversations").mapObjects(::jsonToConversation),
            messages = root.optJSONArray("messages")?.mapObjects(::jsonToMessage).orEmpty(),
            preferences = root.getJSONArray("preferences").mapObjects(::jsonToPreference),
            privacySettings = jsonToPrivacy(root.getJSONObject("privacySettings")),
            evolutionLog = root.getJSONArray("evolutionLog").mapObjects(::jsonToEvolution),
        )
    }

    fun profileToJson(value: RobotProfile) = JSONObject()
        .put("robotId", value.robotId)
        .put("robotName", value.robotName)
        .put("createdAt", value.createdAt)
        .put("lastActivatedAt", value.lastActivatedAt)
        .put("identityVersion", value.identityVersion)
        .put("appVersionAtCreation", value.appVersionAtCreation)
        .put("description", value.description)
        .put("ownerLabel", value.ownerLabel)

    fun personalityToJson(value: RobotPersonalityMemory) = JSONObject()
        .put("id", value.id)
        .put("name", value.name)
        .put("tone", value.tone)
        .put("behaviorRules", JSONArray(value.behaviorRules))
        .put("forbiddenBehaviors", JSONArray(value.forbiddenBehaviors))
        .put("updatedAt", value.updatedAt)

    fun personToJson(value: PersonMemory) = JSONObject()
        .put("id", value.id)
        .put("displayName", value.displayName)
        .put("nickname", value.nickname)
        .put("consentFaceRecognition", value.consentFaceRecognition)
        .put("consentVoiceRecognition", value.consentVoiceRecognition)
        .put("consentConversationMemory", value.consentConversationMemory)
        .put("notes", JSONArray(value.notes))
        .put("preferences", JSONArray(value.preferences.map(::preferenceToJson)))
        .put("firstSeenAt", value.firstSeenAt)
        .put("lastSeenAt", value.lastSeenAt)
        .put("recognitionConfidence", value.recognitionConfidence)

    fun relationshipToJson(value: RelationshipMemory) = JSONObject()
        .put("id", value.id)
        .put("personId", value.personId)
        .put("relationshipLabel", value.relationshipLabel)
        .put("familiarityLevel", value.familiarityLevel)
        .put("trustLevel", value.trustLevel)
        .put("preferredTone", value.preferredTone)
        .put("recurringTopics", JSONArray(value.recurringTopics))
        .put("lastInteractionSummary", value.lastInteractionSummary)
        .put("updatedAt", value.updatedAt)

    fun conversationToJson(value: ConversationMemory) = JSONObject()
        .put("id", value.id)
        .put("startedAt", value.startedAt)
        .put("endedAt", value.endedAt)
        .put("participantIds", JSONArray(value.participantIds))
        .put("title", value.title)
        .put("transcriptSummary", value.transcriptSummary)
        .put("fullTranscriptLocalPath", value.fullTranscriptLocalPath)
        .put("tags", JSONArray(value.tags))
        .put("createdAt", value.createdAt)
        .put("updatedAt", value.updatedAt)

    fun messageToJson(value: ChatMessage) = JSONObject()
        .put("id", value.id)
        .put("conversationId", value.conversationId)
        .put("sender", value.sender)
        .put("text", value.text)
        .put("createdAt", value.createdAt)

    fun preferenceToJson(value: PersonPreference) = JSONObject()
        .put("id", value.id)
        .put("personId", value.personId)
        .put("category", value.category)
        .put("value", value.value)
        .put("confidence", value.confidence.toDouble())
        .put("sourceConversationId", value.sourceConversationId)
        .put("createdAt", value.createdAt)
        .put("updatedAt", value.updatedAt)

    fun privacyToJson(value: PrivacySettings) = JSONObject()
        .put("consentConversationMemory", value.consentConversationMemory)
        .put("consentPersonProfile", value.consentPersonProfile)
        .put("consentFaceRecognition", value.consentFaceRecognition)
        .put("consentVoiceRecognition", value.consentVoiceRecognition)
        .put("consentSendImagesToAI", value.consentSendImagesToAI)
        .put("consentSaveFullTranscript", value.consentSaveFullTranscript)
        .put("consentSaveAudio", value.consentSaveAudio)

    fun evolutionToJson(value: EvolutionLogEntry) = JSONObject()
        .put("id", value.id)
        .put("timestamp", value.timestamp)
        .put("type", value.type)
        .put("title", value.title)
        .put("description", value.description)
        .put("relatedPersonId", value.relatedPersonId)
        .put("relatedConversationId", value.relatedConversationId)

    private fun jsonToProfile(value: JSONObject) = RobotProfile(
        robotId = value.getString("robotId"),
        robotName = value.getString("robotName"),
        createdAt = value.getLong("createdAt"),
        lastActivatedAt = value.getLong("lastActivatedAt"),
        identityVersion = value.getInt("identityVersion"),
        appVersionAtCreation = value.optNullableString("appVersionAtCreation"),
        description = value.optNullableString("description"),
        ownerLabel = value.optNullableString("ownerLabel"),
    )

    private fun jsonToPersonality(value: JSONObject) = RobotPersonalityMemory(
        id = value.getString("id"),
        name = value.getString("name"),
        tone = value.getString("tone"),
        behaviorRules = value.getJSONArray("behaviorRules").toStringList(),
        forbiddenBehaviors = value.getJSONArray("forbiddenBehaviors").toStringList(),
        updatedAt = value.getLong("updatedAt"),
    )

    private fun jsonToPerson(value: JSONObject) = PersonMemory(
        id = value.getString("id"),
        displayName = value.getString("displayName"),
        nickname = value.optNullableString("nickname"),
        consentFaceRecognition = value.getBoolean("consentFaceRecognition"),
        consentVoiceRecognition = value.getBoolean("consentVoiceRecognition"),
        consentConversationMemory = value.getBoolean("consentConversationMemory"),
        notes = value.getJSONArray("notes").toStringList(),
        preferences = value.getJSONArray("preferences").mapObjects(::jsonToPreference),
        firstSeenAt = value.getLong("firstSeenAt"),
        lastSeenAt = value.getLong("lastSeenAt"),
        recognitionConfidence = if (value.isNull("recognitionConfidence")) null else value.getDouble("recognitionConfidence").toFloat(),
    )

    private fun jsonToRelationship(value: JSONObject) = RelationshipMemory(
        id = value.getString("id"),
        personId = value.getString("personId"),
        relationshipLabel = value.optNullableString("relationshipLabel"),
        familiarityLevel = value.getInt("familiarityLevel"),
        trustLevel = value.getInt("trustLevel"),
        preferredTone = value.optNullableString("preferredTone"),
        recurringTopics = value.getJSONArray("recurringTopics").toStringList(),
        lastInteractionSummary = value.optNullableString("lastInteractionSummary"),
        updatedAt = value.getLong("updatedAt"),
    )

    private fun jsonToConversation(value: JSONObject) = ConversationMemory(
        id = value.getString("id"),
        startedAt = value.getLong("startedAt"),
        endedAt = if (value.isNull("endedAt")) null else value.getLong("endedAt"),
        participantIds = value.getJSONArray("participantIds").toStringList(),
        title = value.optNullableString("title"),
        transcriptSummary = value.getString("transcriptSummary"),
        fullTranscriptLocalPath = value.optNullableString("fullTranscriptLocalPath"),
        tags = value.getJSONArray("tags").toStringList(),
        createdAt = value.getLong("createdAt"),
        updatedAt = value.getLong("updatedAt"),
    )

    private fun jsonToMessage(value: JSONObject) = ChatMessage(
        id = value.getString("id"),
        conversationId = value.getString("conversationId"),
        sender = value.getString("sender"),
        text = value.getString("text"),
        createdAt = value.getLong("createdAt"),
    )

    private fun jsonToPreference(value: JSONObject) = PersonPreference(
        id = value.getString("id"),
        personId = value.getString("personId"),
        category = value.getString("category"),
        value = value.getString("value"),
        confidence = value.getDouble("confidence").toFloat(),
        sourceConversationId = value.optNullableString("sourceConversationId"),
        createdAt = value.getLong("createdAt"),
        updatedAt = value.getLong("updatedAt"),
    )

    private fun jsonToPrivacy(value: JSONObject) = PrivacySettings(
        consentConversationMemory = value.getBoolean("consentConversationMemory"),
        consentPersonProfile = value.getBoolean("consentPersonProfile"),
        consentFaceRecognition = value.getBoolean("consentFaceRecognition"),
        consentVoiceRecognition = value.getBoolean("consentVoiceRecognition"),
        consentSendImagesToAI = value.getBoolean("consentSendImagesToAI"),
        consentSaveFullTranscript = value.getBoolean("consentSaveFullTranscript"),
        consentSaveAudio = value.getBoolean("consentSaveAudio"),
    )

    private fun jsonToEvolution(value: JSONObject) = EvolutionLogEntry(
        id = value.getString("id"),
        timestamp = value.getLong("timestamp"),
        type = value.getString("type"),
        title = value.getString("title"),
        description = value.getString("description"),
        relatedPersonId = value.optNullableString("relatedPersonId"),
        relatedConversationId = value.optNullableString("relatedConversationId"),
    )

    private fun JSONArray.toStringList(): List<String> {
        return (0 until length()).map { getString(it) }
    }

    private fun <T> JSONArray.mapObjects(mapper: (JSONObject) -> T): List<T> {
        return (0 until length()).map { mapper(getJSONObject(it)) }
    }

    private fun JSONObject.optNullableString(name: String): String? {
        return if (isNull(name)) null else optString(name)
    }
}
