package com.robottripede.app

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.setPadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.robottripede.app.data.model.LedColor
import com.robottripede.app.data.model.LedMode
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: RobotDashboardViewModel by viewModels()

    private lateinit var bleStatus: TextView
    private lateinit var stopStatus: TextView
    private lateinit var robotStatus: TextView
    private lateinit var imuStatus: TextView
    private lateinit var telemetry: TextView
    private lateinit var assistantResponse: TextView
    private lateinit var validationStatus: TextView
    private lateinit var commandInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun buildContent(): View {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32)
            setBackgroundColor(Color.rgb(247, 248, 250))
        }

        content.addView(title("Robot Tripede"))
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

        return ScrollView(this).apply {
            addView(content)
        }
    }

    private fun render(state: RobotUiState) {
        bleStatus.text = state.bleState
        stopStatus.text = if (state.telemetry.stop) "ATTIVO" else "Non attivo"
        stopStatus.setTextColor(if (state.telemetry.stop) Color.rgb(185, 28, 28) else Color.rgb(22, 101, 52))
        robotStatus.text = state.telemetry.status
        imuStatus.text = "${state.telemetry.imu.tilt}, caduta: ${state.telemetry.imu.fallDetected}"
        telemetry.text = state.telemetry.toDisplayText()
        assistantResponse.text = state.assistantResponse
        validationStatus.text = state.validationMessage
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
}
