package com.example.slogger.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.io.File

class SensorHeart (
    private val context: Context,
    private val sensorManager: SensorManager,
    private val deviceName: String,
    private val expId: String,
    private val freq: Int,
    private val maxRecordInFile: Int,
) {
    private val sensorType = "Heart"
    private var sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)!!
    private lateinit var fileHandler: File

    private var filename = ""
    private var fileSeqNum = 1
    private var currRecordCount = 0

    private var isRunning = false

    private val listener = object: SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {

                if (currRecordCount == 0) {
                    // 1. Get current wall clock timestamp.
                    val millis = System.currentTimeMillis().toString()

                    // 2. Create a new file for storing logging data.
                    filename = genLogFileName(
                        deviceName,
                        sensorType,
                        freq,
                        fileSeqNum,
                        expId,
                        millis)

                    // open the new file
                    fileHandler = File(context.filesDir, filename)
                }

                // Timestamp in nanoseconds
                val t = event.timestamp

                val x = event.values[0]

                write2File("$t,$x\n")
                currRecordCount += 1

                if (currRecordCount >= maxRecordInFile) {
                    currRecordCount = 0
                    fileSeqNum += 1
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            return
        }
    }

    private fun write2File(data: String) {
        try {
            fileHandler.appendText(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public fun isRunning(): Boolean {
        return isRunning
    }

    public fun start() {
        if (isRunning) {
            throw java.lang.Exception("The HeartRate sensor is running! Can't start it again!")
        }

        isRunning = true

        try {
            Log.d("sensor", "start/reg heart sensor")
            // register event to start accelerometer
            sensorManager.registerListener(
                listener,
                sensor,
                getSensorMode(freq)
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public fun reset() {
        Log.d("Schedule", "Unregister Heart")
        if (isRunning) {
            // Unregister sensor events
            sensorManager.unregisterListener(listener, sensor)

            // reset parameters
            // Any other parameters? How about experiment Id?

            filename = ""
            fileSeqNum = 1
            currRecordCount = 0
            isRunning = false
        }
    }

}