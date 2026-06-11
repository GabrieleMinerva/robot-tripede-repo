package com.robottripede.app.safety

import com.robottripede.app.data.model.Esp32Telemetry
import com.robottripede.app.data.model.RobotCommand

class CommandValidator {
    fun validate(command: RobotCommand, telemetry: Esp32Telemetry): CommandValidation {
        if (telemetry.stop && command is RobotCommand.SimulateMoveForward) {
            return CommandValidation(false, "STOP attivo: movimento simulato rifiutato")
        }

        return when (command) {
            is RobotCommand.SimulateMoveForward -> validateSimulatedMove(command)
            is RobotCommand.SetLed,
            RobotCommand.GetStatus,
            RobotCommand.Ping,
            RobotCommand.ResetError,
            RobotCommand.SimulateStop -> CommandValidation(true, "Comando consentito")
        }
    }

    private fun validateSimulatedMove(command: RobotCommand.SimulateMoveForward): CommandValidation {
        if (command.speed != "slow") {
            return CommandValidation(false, "Solo velocita simulata slow consentita in fase 1")
        }
        if (command.durationMs !in 1..1000) {
            return CommandValidation(false, "Durata simulazione fuori limite: 1..1000 ms")
        }
        return CommandValidation(true, "Movimento simulato consentito")
    }
}

data class CommandValidation(
    val allowed: Boolean,
    val reason: String,
)
