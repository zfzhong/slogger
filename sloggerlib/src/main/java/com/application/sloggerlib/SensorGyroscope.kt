package com.application.sloggerlib

import android.content.Context
import android.hardware.SensorManager
import com.application.sloggerlib.GeneralSensor

class SensorGyroscope(
    context: Context,
    sensorManager: SensorManager,
    type: Int,
    deviceName: String,
    protocol: String,
    expId: String,
    freq: Int,
    maxRecordInFile: Int,
    batchSize: Int
) : GeneralSensor(
    context,
    sensorManager,
    type,
    deviceName,
    protocol,
    expId,
    freq,
    maxRecordInFile,
    batchSize)

{}