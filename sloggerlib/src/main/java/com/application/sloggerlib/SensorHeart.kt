package com.application.sloggerlib

import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.application.sloggerlib.GeneralSensor

class SensorHeart(
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
{
    // The heart rate sensor differs from Accelerometer/Gyroscope in
    // the logging file format. We need the following function to get
    // heart rate readings.
    override fun getValues(event: SensorEvent): String {
        // Timestamp in nanoseconds
        val t = event.timestamp

        val x = event.values[0]

//        if (x.toInt() == 0) {
//            return ""
//        }

        return "$t,$x\n"
    }
}