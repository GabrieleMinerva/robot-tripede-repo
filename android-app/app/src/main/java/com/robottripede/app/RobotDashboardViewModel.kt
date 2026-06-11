package com.robottripede.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robottripede.app.assistant.AssistantClient
import com.robottripede.app.assistant.MockAssistantClient
import com.robottripede.app.data.ble.BleRobotRepository
import com.robottripede.app.data.ble.MockBleRobotRepository
import com.robottripede.app.data.model.Esp32Telemetry
import com.robottripede.app.data.model.LedColor
import com.robottripede.app.data.model.LedMode
import com.robottripede.app.data.model.RobotCommand
import com.robottripede.app.privacy.PrivacyFilter
import com.robottripede.app.safety.CommandValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RobotDashboardViewModel(
    private val repository: BleRobotRepository = MockBleRobotRepository(),
    private val assistantClient: AssistantClient = MockAssistantClient(),
    private val privacyFilter: PrivacyFilter = PrivacyFilter(),
    private val commandValidator: CommandValidator = CommandValidator(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(RobotUiState())
    val uiState: StateFlow<RobotUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.connect()
            _uiState.update { it.copy(bleState = repository.connectionState.value) }
        }
        viewModelScope.launch {
            repository.telemetry.collect { telemetry ->
                _uiState.update {
                    it.copy(
                        bleState = repository.connectionState.value,
                        telemetry = telemetry,
                    )
                }
            }
        }
    }

    fun sendLedCommand(color: LedColor, mode: LedMode) {
        sendCommand(RobotCommand.SetLed(color, mode))
    }

    fun simulateMoveForward() {
        sendCommand(RobotCommand.SimulateMoveForward(speed = "slow", durationMs = 500))
    }

    fun simulateStop() {
        sendCommand(RobotCommand.SimulateStop)
    }

    fun handleTypedCommand(rawText: String) {
        viewModelScope.launch {
            val payload = privacyFilter.prepareTextOnlyPayload(
                userText = rawText,
                telemetry = uiState.value.telemetry,
            )
            val response = assistantClient.respond(payload)
            _uiState.update {
                it.copy(
                    assistantResponse = response.message,
                    validationMessage = "Payload filtrato: solo testo e telemetria riassunta",
                )
            }
        }
    }

    private fun sendCommand(command: RobotCommand) {
        viewModelScope.launch {
            val validation = commandValidator.validate(command, uiState.value.telemetry)
            if (!validation.allowed) {
                _uiState.update { it.copy(validationMessage = validation.reason) }
                return@launch
            }

            repository.sendCommand(command)
            _uiState.update { it.copy(validationMessage = "Comando validato e inviato: ${command.type}") }
        }
    }
}

data class RobotUiState(
    val bleState: String = "Mock disconnesso",
    val telemetry: Esp32Telemetry = Esp32Telemetry.initial(),
    val assistantResponse: String = "Nessuna risposta ancora",
    val validationMessage: String = "Validatore pronto",
)
