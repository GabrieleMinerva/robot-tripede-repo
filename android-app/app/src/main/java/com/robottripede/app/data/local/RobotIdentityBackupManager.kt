package com.robottripede.app.data.local

import com.robottripede.app.domain.memory.EvolutionLogEntry
import com.robottripede.app.domain.memory.RobotMemoryRepository
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class RobotIdentityBackupManager(
    private val repository: RobotMemoryRepository,
) {
    fun exportBackup(outputStream: OutputStream) {
        val snapshot = repository.loadSnapshot()
        val exportedAt = System.currentTimeMillis()
        ZipOutputStream(outputStream).use { zip ->
            zip.writeJson("backup_manifest.json", manifestJson(exportedAt).toString(2))
            zip.writeJson("robot_profile.json", RobotMemoryJson.profileToJson(snapshot.profile).toString(2))
            zip.writeJson("robot_personality.json", RobotMemoryJson.personalityToJson(snapshot.personality).toString(2))
            zip.writeJson("persons.json", org.json.JSONArray(snapshot.persons.map(RobotMemoryJson::personToJson)).toString(2))
            zip.writeJson("relationships.json", org.json.JSONArray(snapshot.relationships.map(RobotMemoryJson::relationshipToJson)).toString(2))
            zip.writeJson("conversations.json", org.json.JSONArray(snapshot.conversations.map(RobotMemoryJson::conversationToJson)).toString(2))
            zip.writeJson("conversation_summaries.json", org.json.JSONArray(snapshot.conversations.map {
                JSONObject().put("id", it.id).put("summary", it.transcriptSummary)
            }).toString(2))
            zip.writeJson("messages.json", org.json.JSONArray(snapshot.messages.map(RobotMemoryJson::messageToJson)).toString(2))
            zip.writeJson("preferences.json", org.json.JSONArray(snapshot.preferences.map(RobotMemoryJson::preferenceToJson)).toString(2))
            zip.writeJson("privacy_settings.json", RobotMemoryJson.privacyToJson(snapshot.privacySettings).toString(2))
            zip.writeJson("consents.json", RobotMemoryJson.privacyToJson(snapshot.privacySettings).toString(2))
            zip.writeJson("evolution_log.json", org.json.JSONArray(snapshot.evolutionLog.map(RobotMemoryJson::evolutionToJson)).toString(2))
            zip.writeJson("device_configuration.json", JSONObject().put("deviceRole", "Samsung Galaxy S20 head").toString(2))
            zip.writeJson("snapshot.json", RobotMemoryJson.encode(snapshot))
        }
        repository.replaceSnapshot(
            repository.loadSnapshot().let { snapshot ->
                snapshot.copy(
                    evolutionLog = snapshot.evolutionLog + EvolutionLogEntry(
                        id = "event-${UUID.randomUUID()}",
                        timestamp = exportedAt,
                        type = "backup_exported",
                        title = "Backup esportato",
                        description = "Archivio .robotbackup generato localmente.",
                        relatedPersonId = null,
                        relatedConversationId = null,
                    ),
                )
            },
        )
    }

    fun importBackup(inputStream: InputStream) {
        var snapshotJson: String? = null
        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "snapshot.json") {
                    snapshotJson = zip.readBytes().toString(Charsets.UTF_8)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        val imported = RobotMemoryJson.decode(requireNotNull(snapshotJson) { "Backup non valido: snapshot.json mancante" })
        val now = System.currentTimeMillis()
        repository.replaceSnapshot(
            imported.copy(
                evolutionLog = imported.evolutionLog + EvolutionLogEntry(
                    id = "event-${UUID.randomUUID()}",
                    timestamp = now,
                    type = "backup_imported",
                    title = "Backup importato",
                    description = "Identita robot importata in modalita replace.",
                    relatedPersonId = null,
                    relatedConversationId = null,
                ),
            ),
        )
    }

    fun manifestPreview(): String = manifestJson(System.currentTimeMillis()).toString(2)

    private fun manifestJson(exportedAt: Long): JSONObject {
        val snapshot = repository.loadSnapshot()
        return JSONObject()
            .put("backupType", "RobotIdentityBackup")
            .put("backupVersion", 1)
            .put("robotId", snapshot.profile.robotId)
            .put("robotName", snapshot.profile.robotName)
            .put("createdAt", snapshot.profile.createdAt)
            .put("exportedAt", exportedAt)
            .put("appVersion", "0.1.0")
            .put("containsPersonalData", snapshot.persons.isNotEmpty())
            .put("containsBiometricData", false)
            .put("containsConversationTranscripts", snapshot.messages.isNotEmpty())
            .put("containsAudioFiles", false)
            .put("containsImages", false)
            .put("encryption", JSONObject().put("enabled", false).put("method", "none"))
    }

    private fun ZipOutputStream.writeJson(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }
}
