package com.robottripede.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.robottripede.app.data.ai.MockAiConversationClient
import com.robottripede.app.data.camera.CameraPreviewView
import com.robottripede.app.data.local.LocalRobotMemoryRepository
import com.robottripede.app.data.local.RobotIdentityBackupManager
import com.robottripede.app.data.model.LedColor
import com.robottripede.app.data.model.LedMode
import com.robottripede.app.domain.ai.AiConversationRequest
import com.robottripede.app.domain.live.LiveRobotState
import com.robottripede.app.domain.memory.PrivacySettings
import com.robottripede.app.domain.memory.RobotMemoryRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: RobotDashboardViewModel by viewModels()

    private lateinit var memoryRepository: RobotMemoryRepository
    private lateinit var backupManager: RobotIdentityBackupManager
    private val aiConversationClient = MockAiConversationClient()

    private var selectedPersonId: String? = null
    private var currentConversationId: String? = null
    private var liveCamera: CameraPreviewView? = null
    private var liveState = LiveRobotState.IDLE
    private var liveCameraMessage: String? = null
    private var livePreviewEnabled = false
    private val liveTransientMessages = mutableListOf<Pair<String, String>>()

    private lateinit var bleStatus: TextView
    private lateinit var stopStatus: TextView
    private lateinit var robotStatus: TextView
    private lateinit var imuStatus: TextView
    private lateinit var telemetry: TextView
    private lateinit var assistantResponse: TextView
    private lateinit var validationStatus: TextView
    private lateinit var commandInput: EditText

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            liveState = LiveRobotState.CAMERA_READY
            livePreviewEnabled = false
            liveCameraMessage = "Camera autorizzata. Avvia la preview solo quando vuoi usarla."
        } else {
            liveState = LiveRobotState.PRIVACY_BLOCKED
            livePreviewEnabled = false
            toast("Camera non autorizzata: preview locale bloccata")
        }
        showLiveRobotScreen()
    }

    private val exportBackupLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use(backupManager::exportBackup)
            toast("Backup .robotbackup esportato")
            setContentView(buildMemoryScreen())
        }
    }

    private val importBackupLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            contentResolver.openInputStream(uri)?.use(backupManager::importBackup)
            toast("Backup importato in modalita replace")
            setContentView(buildMemoryScreen())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memoryRepository = LocalRobotMemoryRepository(this)
        backupManager = RobotIdentityBackupManager(memoryRepository)
        setContentView(buildDashboard())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    override fun onDestroy() {
        liveCamera?.stopPreview()
        super.onDestroy()
    }

    private fun buildDashboard(): View {
        liveCamera?.stopPreview()
        liveCamera = null
        livePreviewEnabled = false
        val content = baseContent()

        content.addView(title("Robot Tripede"))
        content.addView(horizontalButtons(
            button("Live Robot") { openLiveRobot() },
            button("Memoria") { setContentView(buildMemoryScreen()) },
            button("Privacy") { setContentView(buildPrivacyScreen()) },
        ))

        content.addView(section("Connessione"))
        bleStatus = row("BLE", "Mock non connesso").also(content::addView)

        content.addView(section("Stato"))
        stopStatus = row("STOP", "Sconosciuto").also(content::addView)
        robotStatus = row("Robot", "Sconosciuto").also(content::addView)
        imuStatus = row("IMU", "Sconosciuto").also(content::addView)

        content.addView(section("Telemetria ESP32"))
        telemetry = body("In attesa di telemetria mock").also(content::addView)

        content.addView(section("LED"))
        content.addView(horizontalButtons(
            button("Blu blink") { viewModel.sendLedCommand(LedColor.BLUE, LedMode.BLINK) },
            button("Verde solid") { viewModel.sendLedCommand(LedColor.GREEN, LedMode.SOLID) },
            button("Rosso blink") { viewModel.sendLedCommand(LedColor.RED, LedMode.BLINK) },
        ))

        content.addView(section("Comando testuale"))
        commandInput = EditText(this).apply {
            hint = "Scrivi un comando, es. accendi led blu"
            minLines = 2
            setSingleLine(false)
        }
        content.addView(commandInput)
        content.addView(horizontalButtons(
            button("Invia") { viewModel.handleTypedCommand(commandInput.text.toString()) },
            button("Simula avanti") { viewModel.simulateMoveForward() },
            button("Simula STOP") { viewModel.simulateStop() },
        ))

        validationStatus = body("Validatore pronto").also(content::addView)

        content.addView(section("Risposta assistente mock"))
        assistantResponse = body("Nessuna risposta ancora").also(content::addView)

        return scroll(content)
    }

    private fun openLiveRobot() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            liveState = LiveRobotState.CAMERA_READY
            livePreviewEnabled = false
            liveCameraMessage = "Preview camera non avviata. La schermata Live funziona anche senza camera."
            showLiveRobotScreen()
        } else {
            liveState = LiveRobotState.PRIVACY_BLOCKED
            livePreviewEnabled = false
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun buildLiveRobotScreen(): View {
        val snapshot = memoryRepository.loadSnapshot()
        val content = baseContent()
        content.addView(title("Live Robot"))
        content.addView(horizontalButtons(
            button("Dashboard") { setContentView(buildDashboard()) },
            button("Memoria") { setContentView(buildMemoryScreen()) },
            button("Stop live") {
                currentConversationId?.let { conversationId ->
                    memoryRepository.finishConversation(conversationId, "Conversazione Live Robot salvata localmente.")
                }
                currentConversationId = null
                liveState = LiveRobotState.IDLE
                showLiveRobotScreen()
            },
        ))

        content.addView(section("Camera locale"))
        if (liveState == LiveRobotState.PRIVACY_BLOCKED) {
            content.addView(body("Preview bloccata: autorizza la camera per usarla localmente. Nessuna immagine viene inviata all'esterno."))
            content.addView(button("Autorizza camera") { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) })
        } else if (!livePreviewEnabled) {
            content.addView(body(liveCameraMessage ?: "Preview camera spenta. Nessuna immagine viene inviata all'esterno."))
            content.addView(button("Avvia preview camera") {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    livePreviewEnabled = true
                    liveState = LiveRobotState.CAMERA_READY
                    liveCameraMessage = null
                    showLiveRobotScreen()
                } else {
                    liveState = LiveRobotState.PRIVACY_BLOCKED
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            })
        } else {
            liveCameraMessage?.let { message ->
                content.addView(body(message))
            }
            liveCamera = CameraPreviewView(this) { message ->
                runOnUiThread {
                    liveState = LiveRobotState.ERROR
                    livePreviewEnabled = false
                    liveCameraMessage = "Preview camera non disponibile: $message"
                    toast("Preview camera non disponibile")
                }
            }.apply { post { startPreview() } }
            content.addView(liveCamera, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 520))
        }

        val selectedPerson = snapshot.persons.firstOrNull { it.id == selectedPersonId }
        content.addView(section("Sessione"))
        content.addView(body("Stato robot: $liveState"))
        content.addView(body("Microfono: push-to-talk futuro, non attivo"))
        content.addView(body("Memoria: ${if (snapshot.privacySettings.consentConversationMemory) "attiva" else "disattivata"}"))
        content.addView(body("Persona: ${selectedPerson?.displayName ?: "non selezionata"}"))

        content.addView(section("Persona manuale"))
        val personNameInput = EditText(this).apply { hint = "Nome persona, es. Gabriele" }
        content.addView(personNameInput)
        content.addView(horizontalButtons(
            button("Crea persona") {
                if (!memoryRepository.loadSnapshot().privacySettings.consentPersonProfile) {
                    toast("Profilo persona non salvato: consenso disattivato")
                    return@button
                }
                val person = memoryRepository.createPerson(personNameInput.text.toString())
                selectedPersonId = person.id
                showLiveRobotScreen()
            },
            button("Seleziona ultima") {
                selectedPersonId = memoryRepository.loadSnapshot().persons.lastOrNull()?.id
                showLiveRobotScreen()
            },
        ))

        content.addView(section("Chat locale mock"))
        val activeConversationId = ensureConversation(selectedPersonId)
        val persistedMessages = memoryRepository.loadSnapshot().messages
            .filter { it.conversationId == activeConversationId }
            .map { it.sender to it.text }
        val messages = persistedMessages + liveTransientMessages
        content.addView(body(messages.joinToString("\n\n") { "${it.first}: ${it.second}" }.ifBlank { "Nessun messaggio in questa sessione" }))

        val liveInput = EditText(this).apply {
            hint = "Scrivi al robot"
            minLines = 2
            setSingleLine(false)
        }
        content.addView(liveInput)
        content.addView(button("Invia a Mock AI") {
            sendLiveMessage(liveInput.text.toString(), activeConversationId)
        })

        return scroll(content)
    }

    private fun ensureConversation(personId: String?): String {
        if (!memoryRepository.loadSnapshot().privacySettings.consentConversationMemory) {
            return TRANSIENT_CONVERSATION_ID
        }
        currentConversationId?.let { return it }
        return memoryRepository.startConversation(personId).id.also { currentConversationId = it }
    }

    private fun sendLiveMessage(text: String, conversationId: String) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) {
            toast("Scrivi un messaggio prima di inviare")
            return
        }
        liveState = LiveRobotState.THINKING
        val snapshot = memoryRepository.loadSnapshot()
        val memoryAllowed = snapshot.privacySettings.consentConversationMemory
        if (memoryAllowed) {
            memoryRepository.appendMessage(conversationId, "utente", cleanText)
        } else {
            liveTransientMessages += "utente" to cleanText
        }
        val person = snapshot.persons.firstOrNull { it.id == selectedPersonId }
        val response = aiConversationClient.respond(
            AiConversationRequest(
                userText = cleanText,
                selectedPersonName = person?.displayName,
                memorySnapshot = snapshot,
            ),
        )
        if (memoryAllowed) {
            memoryRepository.appendMessage(conversationId, "robot", response.assistantText)
            memoryRepository.finishConversation(
                conversationId,
                "Ultimo scambio: utente='$cleanText'; robot='${response.assistantText.take(120)}'",
            )
        } else {
            liveTransientMessages += "robot" to response.assistantText
        }
        liveState = LiveRobotState.SPEAKING
        showLiveRobotScreen()
    }

    private fun showLiveRobotScreen() {
        try {
            setContentView(buildLiveRobotScreen())
        } catch (exception: Exception) {
            liveCamera?.stopPreview()
            liveCamera = null
            livePreviewEnabled = false
            liveState = LiveRobotState.ERROR
            liveCameraMessage = exception.message ?: "Errore apertura Live Robot"
            setContentView(buildLiveRobotErrorScreen(liveCameraMessage.orEmpty()))
        }
    }

    private fun buildLiveRobotErrorScreen(message: String): View {
        val content = baseContent()
        content.addView(title("Live Robot"))
        content.addView(section("Errore"))
        content.addView(body("La schermata Live non si e aperta correttamente: $message"))
        content.addView(body("Camera e microfono restano spenti. Nessun dato viene inviato all'esterno."))
        content.addView(horizontalButtons(
            button("Dashboard") { setContentView(buildDashboard()) },
            button("Riprova senza camera") {
                livePreviewEnabled = false
                liveCameraMessage = "Riprovo con preview camera spenta."
                showLiveRobotScreen()
            },
        ))
        return scroll(content)
    }

    private fun buildMemoryScreen(): View {
        liveCamera?.stopPreview()
        liveCamera = null
        livePreviewEnabled = false
        val snapshot = memoryRepository.loadSnapshot()
        val content = baseContent()
        content.addView(title("Memoria e identita"))
        content.addView(horizontalButtons(
            button("Dashboard") { setContentView(buildDashboard()) },
            button("Live Robot") { openLiveRobot() },
            button("Privacy") { setContentView(buildPrivacyScreen()) },
        ))

        content.addView(section("Identita robot"))
        content.addView(body("Nome: ${snapshot.profile.robotName}"))
        content.addView(body("robotId: ${snapshot.profile.robotId}"))
        content.addView(body("Creato: ${formatTime(snapshot.profile.createdAt)}"))
        content.addView(body("Persone conosciute: ${snapshot.persons.size}"))
        content.addView(body("Conversazioni: ${snapshot.conversations.size}"))
        content.addView(body("Messaggi locali: ${snapshot.messages.size}"))

        val robotNameInput = EditText(this).apply {
            hint = "Nuovo nome robot"
            setText(snapshot.profile.robotName)
        }
        content.addView(robotNameInput)
        content.addView(button("Cambia nome robot") {
            memoryRepository.updateRobotName(robotNameInput.text.toString())
            setContentView(buildMemoryScreen())
        })

        content.addView(section("Backup locale"))
        content.addView(body(backupManager.manifestPreview()))
        content.addView(horizontalButtons(
            button("Esporta") { exportBackupLauncher.launch(defaultBackupName()) },
            button("Importa") { importBackupLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
        ))

        content.addView(section("Persone"))
        snapshot.persons.forEach { person ->
            content.addView(body("${person.displayName} - memoria conversazione: ${person.consentConversationMemory}"))
            content.addView(button("Elimina ${person.displayName}") {
                if (selectedPersonId == person.id) selectedPersonId = null
                memoryRepository.deletePerson(person.id)
                setContentView(buildMemoryScreen())
            })
        }
        if (snapshot.persons.isEmpty()) content.addView(body("Nessuna persona salvata"))

        content.addView(section("Conversazioni"))
        snapshot.conversations.takeLast(10).reversed().forEach { conversation ->
            content.addView(body("${formatTime(conversation.startedAt)} - ${conversation.transcriptSummary.ifBlank { "senza sintesi" }}"))
            content.addView(button("Elimina conversazione") {
                memoryRepository.deleteConversation(conversation.id)
                setContentView(buildMemoryScreen())
            })
        }
        if (snapshot.conversations.isEmpty()) content.addView(body("Nessuna conversazione salvata"))

        content.addView(section("Azioni distruttive locali"))
        content.addView(horizontalButtons(
            button("Cancella memoria") {
                memoryRepository.clearMemory()
                selectedPersonId = null
                currentConversationId = null
                setContentView(buildMemoryScreen())
            },
            button("Elimina identita") {
                memoryRepository.deleteIdentity()
                selectedPersonId = null
                currentConversationId = null
                setContentView(buildMemoryScreen())
            },
        ))

        return scroll(content)
    }

    private fun buildPrivacyScreen(): View {
        liveCamera?.stopPreview()
        liveCamera = null
        livePreviewEnabled = false
        val settings = memoryRepository.loadSnapshot().privacySettings
        val content = baseContent()
        content.addView(title("Privacy"))
        content.addView(horizontalButtons(
            button("Dashboard") { setContentView(buildDashboard()) },
            button("Live Robot") { openLiveRobot() },
            button("Memoria") { setContentView(buildMemoryScreen()) },
        ))
        content.addView(body("Camera solo preview locale. Microfono, riconoscimento facciale, riconoscimento vocale e invio immagini all'AI restano disattivati in questa fase."))

        val conversationMemory = check("Memoria conversazioni", settings.consentConversationMemory).also(content::addView)
        val personProfile = check("Profili persona manuali", settings.consentPersonProfile).also(content::addView)
        val faceRecognition = check("Riconoscimento facciale", settings.consentFaceRecognition).also(content::addView)
        val voiceRecognition = check("Riconoscimento vocale identificativo", settings.consentVoiceRecognition).also(content::addView)
        val sendImages = check("Invio immagini all'AI", settings.consentSendImagesToAI).also(content::addView)
        val saveFullTranscript = check("Salva transcript completo", settings.consentSaveFullTranscript).also(content::addView)
        val saveAudio = check("Salva audio grezzo", settings.consentSaveAudio).also(content::addView)

        content.addView(button("Salva privacy") {
            memoryRepository.savePrivacySettings(
                PrivacySettings(
                    consentConversationMemory = conversationMemory.isChecked,
                    consentPersonProfile = personProfile.isChecked,
                    consentFaceRecognition = false,
                    consentVoiceRecognition = false,
                    consentSendImagesToAI = false,
                    consentSaveFullTranscript = saveFullTranscript.isChecked,
                    consentSaveAudio = false,
                ),
            )
            faceRecognition.isChecked = false
            voiceRecognition.isChecked = false
            sendImages.isChecked = false
            saveAudio.isChecked = false
            toast("Privacy salvata: funzioni biometriche e invio immagini restano bloccati")
            setContentView(buildPrivacyScreen())
        })

        return scroll(content)
    }

    private fun render(state: RobotUiState) {
        if (!::bleStatus.isInitialized) return
        bleStatus.text = state.bleState
        stopStatus.text = if (state.telemetry.stop) "ATTIVO" else "Non attivo"
        stopStatus.setTextColor(if (state.telemetry.stop) Color.rgb(185, 28, 28) else Color.rgb(22, 101, 52))
        robotStatus.text = state.telemetry.status
        imuStatus.text = "${state.telemetry.imu.tilt}, caduta: ${state.telemetry.imu.fallDetected}"
        telemetry.text = state.telemetry.toDisplayText()
        assistantResponse.text = state.assistantResponse
        validationStatus.text = state.validationMessage
    }

    private fun baseContent() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(32)
        setBackgroundColor(Color.rgb(247, 248, 250))
    }

    private fun title(text: String) = TextView(this).apply {
        this.text = text
        textSize = 28f
        setTextColor(Color.rgb(17, 24, 39))
        setPadding(0, 8, 0, 24)
    }

    private fun section(text: String) = TextView(this).apply {
        this.text = text
        textSize = 18f
        setTextColor(Color.rgb(31, 41, 55))
        setPadding(0, 24, 0, 8)
    }

    private fun row(label: String, value: String) = TextView(this).apply {
        text = "$label: $value"
        textSize = 16f
        setPadding(0, 4, 0, 4)
    }

    private fun body(value: String) = TextView(this).apply {
        text = value
        textSize = 15f
        setTextColor(Color.rgb(55, 65, 81))
        setPadding(0, 6, 0, 6)
    }

    private fun check(label: String, checked: Boolean) = CheckBox(this).apply {
        text = label
        isChecked = checked
        textSize = 15f
        setPadding(0, 6, 0, 6)
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setOnClickListener { action() }
    }

    private fun horizontalButtons(vararg buttons: Button) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        buttons.forEach { button ->
            addView(button, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun scroll(content: LinearLayout) = ScrollView(this).apply {
        addView(content)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun defaultBackupName(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "robot-tripede-$date.robotbackup"
    }

    private fun formatTime(value: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(value))
    }

    companion object {
        private const val TRANSIENT_CONVERSATION_ID = "transient-live-session"
    }
}
