package com.robottripede.app.data.ble

import com.robottripede.app.data.model.Esp32Telemetry
import com.robottripede.app.data.model.RobotCommand
import kotlinx.coroutines.flow.StateFlow

interface BleRobotRepository {
    val connectionState: StateFlow<String>
    val telemetry: StateFlow<Esp32Telemetry>

    suspend fun connect()
    suspend fun sendCommand(command: RobotCommand)
}
