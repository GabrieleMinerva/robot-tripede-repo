package com.robottripede.app.domain.memory

interface RobotMemoryRepository {
    fun loadSnapshot(): RobotMemorySnapshot
    fun replaceSnapshot(snapshot: RobotMemorySnapshot)
    fun updateRobotName(name: String)
    fun savePrivacySettings(settings: PrivacySettings)
    fun createPerson(displayName: String): PersonMemory
    fun deletePerson(personId: String)
    fun startConversation(personId: String?): ConversationMemory
    fun appendMessage(conversationId: String, sender: String, text: String): ChatMessage
    fun finishConversation(conversationId: String, summary: String)
    fun deleteConversation(conversationId: String)
    fun clearMemory()
    fun deleteIdentity()
}
