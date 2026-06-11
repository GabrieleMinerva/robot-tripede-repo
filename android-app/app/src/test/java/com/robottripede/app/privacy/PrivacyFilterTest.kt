package com.robottripede.app.privacy

import com.robottripede.app.data.model.Esp32Telemetry
import com.robottripede.app.data.model.ImuTelemetry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyFilterTest {
    private val filter = PrivacyFilter()

    @Test
    fun payloadUsesTextAndTelemetrySummaryOnly() {
        val payload = filter.prepareTextOnlyPayload(
            userText = "  accendi\nil led blu  ",
            telemetry = Esp32Telemetry(
                stop = false,
                imu = ImuTelemetry(tilt = "stable", fallDetected = false, accelX = 0.0, accelY = 0.0, accelZ = 9.8),
                ble = "connected",
                status = "ready",
            ),
        )

        assertTrue(payload.userText == "accendi il led blu")
        assertTrue(payload.telemetrySummary.contains("stop=false"))
        assertFalse(payload.includesRawAudio)
        assertFalse(payload.includesRawVideo)
        assertFalse(payload.includesImage)
    }
}
