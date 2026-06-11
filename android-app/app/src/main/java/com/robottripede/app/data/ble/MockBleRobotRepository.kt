package com.robottripede.app.data.ble

import com.robottripede.app.data.model.Esp32Telemetry
import com.robottripede.app.data.model.ImuTelemetry
import com.robottripede.app.data.model.RobotCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockBleRobotRepository : BleRobotRepository {
    private val _connectionState = MutableStateFlow("Mock disconnesso")
    override val connectionState: StateFlow<String> = _connectionState

    private val _telemetry = MutableStateFlow(Esp32Telemetry.initial())
    override val telemetry: StateFlow<Esp32Telemetry> = _telemetry

    override suspend fun connect() {
        _connectionState.value = "Mock connesso a ESP32"
        _telemetry.value = Esp32Telemetry(
            stop = false,
            imu = ImuTelemetry(tilt = "stable", fallDetected = false, accelX = 0.0, accelY = 0.0, accelZ = 9.8),
            ble = "connected",
            status = "ready",
            lastCommand = "connect",
        )
    }

    override suspend fun sendCommand(command: RobotCommand) {
        _telemetry.value = when (command) {
            is RobotCommand.SetLed -> _telemetry.value.copy(
                status = "led_${command.color.wireValue}_${command.mode.wireValue}",
                lastCommand = command.type,
            )
            RobotCommand.GetStatus -> _telemetry.value.copy(lastCommand = command.type)
            RobotCommand.Ping -> _telemetry.value.copy(status = "pong", lastCommand = command.type)
            RobotCommand.ResetError -> _telemetry.value.copy(status = "ready", lastCommand = command.type)
            RobotCommand.SimulateStop -> _telemetry.value.copy(stop = true, status = "stop_active", lastCommand = command.type)
            is RobotCommand.SimulateMoveForward -> _telemetry.value.copy(
                status = "simulated_forward_${command.speed}_${command.durationMs}ms",
                lastCommand = command.type,
            )
        }
    }
}
