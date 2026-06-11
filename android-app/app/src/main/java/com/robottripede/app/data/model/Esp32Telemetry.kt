package com.robottripede.app.data.model

data class Esp32Telemetry(
    val stop: Boolean,
    val imu: ImuTelemetry,
    val ble: String,
    val status: String,
    val lastCommand: String? = null,
) {
    fun toDisplayText(): String {
        return """
            stop=$stop
            imu.tilt=${imu.tilt}
            imu.fall_detected=${imu.fallDetected}
            accel=(${imu.accelX}, ${imu.accelY}, ${imu.accelZ})
            ble=$ble
            status=$status
            last_command=${lastCommand ?: "none"}
        """.trimIndent()
    }

    companion object {
        fun initial() = Esp32Telemetry(
            stop = false,
            imu = ImuTelemetry(tilt = "unknown", fallDetected = false, accelX = 0.0, accelY = 0.0, accelZ = 0.0),
            ble = "disconnected",
            status = "booting",
        )
    }
}

data class ImuTelemetry(
    val tilt: String,
    val fallDetected: Boolean,
    val accelX: Double,
    val accelY: Double,
    val accelZ: Double,
)
