package com.example.slogger.presentation

import android.content.Context
import android.hardware.SensorManager

class SensorAccelerometer(
    context: Context,
    sensorManager: SensorManager,
    type: Int,
    deviceName: String,
    expId: String,
    freq: Int,
    maxRecordInFile: Int
) : GeneralSensor(
    context,
    sensorManager,
    type,
    deviceName,
    expId,
    freq,
    maxRecordInFile)
{}