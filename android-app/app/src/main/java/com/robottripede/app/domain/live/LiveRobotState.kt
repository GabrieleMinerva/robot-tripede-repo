package com.robottripede.app.domain.live

enum class LiveRobotState {
    IDLE,
    CAMERA_READY,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR,
    PRIVACY_BLOCKED,
}
