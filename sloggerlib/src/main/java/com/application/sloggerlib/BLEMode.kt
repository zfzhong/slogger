package com.application.sloggerlib

enum class BLEMode {
    ADVERTISE,
    SCAN,
    OFF
}

enum class BLEScanPower {
    LowPower,
    Balanced,
    LowLatency,
    Opportunistic
}

enum class BLEAdMode {
    LowPower,
    Balanced,
    LowLatency
}

enum class BLEAdPower {
    UltraLow,
    Low,
    Medium,
    High
}