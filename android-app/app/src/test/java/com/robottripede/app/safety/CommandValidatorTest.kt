package com.robottripede.app.safety

import com.robottripede.app.data.model.Esp32Telemetry
import com.robottripede.app.data.model.ImuTelemetry
import com.robottripede.app.data.model.RobotCommand
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandValidatorTest {
    private val validator = CommandValidator()

    @Test
    fun stopBlocksSimulatedMovement() {
        val telemetry = telemetry(stop = true)

        val result = validator.validate(
            RobotCommand.SimulateMoveForward(speed = "slow", durationMs = 500),
            telemetry,
        )

        assertFalse(result.allowed)
    }

    @Test
    fun simulatedStopIsAllowedWhileStopIsActive() {
        val telemetry = telemetry(stop = true)

        val result = validator.validate(RobotCommand.SimulateStop, telemetry)

        assertTrue(result.allowed)
    }

    private fun telemetry(stop: Boolean) = Esp32Telemetry(
        stop = stop,
        imu = ImuTelemetry(tilt = "stable", fallDetected = false, accelX = 0.0, accelY = 0.0, accelZ = 9.8),
        ble = "connected",
        status = "ready",
    )
}
