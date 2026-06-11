package com.robottripede.app.data.model

sealed interface RobotCommand {
    val type: String

    data object Ping : RobotCommand {
        override val type = "ping"
    }

    data object GetStatus : RobotCommand {
        override val type = "get_status"
    }

    data class SetLed(val color: LedColor, val mode: LedMode) : RobotCommand {
        override val type = "set_led"
    }

    data object ResetError : RobotCommand {
        override val type = "reset_error"
    }

    data class SimulateMoveForward(val speed: String, val durationMs: Int) : RobotCommand {
        override val type = "simulate_move_forward"
    }

    data object SimulateStop : RobotCommand {
        override val type = "simulate_stop"
    }
}

enum class LedColor(val wireValue: String) {
    RED("red"),
    GREEN("green"),
    BLUE("blue"),
}

enum class LedMode(val wireValue: String) {
    SOLID("solid"),
    BLINK("blink"),
    OFF("off"),
}
