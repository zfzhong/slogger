package com.application.sloggerlib

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.application.sloggerlib.R
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.exp

class SensorLoggingService: Service() {
    private lateinit var sensorManager:SensorManager

    private lateinit var configParams: ConfigParams
    private lateinit var sensorAccel: SensorAccelerometer
    private lateinit var sensorGyro: SensorGyroscope
    private lateinit var sensorHeart: SensorHeart
    private lateinit var sensorOffbody: SensorOffbody
    private lateinit var bleScanner: BLEScanner


    private var configFile = "config.txt"

    private val maxRecordCount = 6000
    private var expId: String = ""
    private val fgsId: Int = 1
    private val channelId = "LoggingChanel"
    //private var debugLogger = DebugLogger(File(filesDir, "debug.log"))

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        loadConfigFile()

        when (intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelId,
            "Logging Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        Log.d("Debug", "service destroyed")
        super.onDestroy()
    }

    private fun start() {
        sensorManager = (getSystemService(Context.SENSOR_SERVICE) as SensorManager?)!!

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setContentTitle("Slogger On:")
            .setContentText("Logging is active")
            .build()

        startForeground(1, notification)

        broadcastStateChange(AppStates.LOGGING)
        startSensors()
    }

    private fun stop() {
        stopSensors()
    }

    private fun createExpId() {
        // Use the current timestamp as expId
        val ts = System.currentTimeMillis()
        expId = ts.toString()
    }

    private fun startSensors() {
        Log.d("Debug", "Service: Start sensors now...")

        // Create experiment id before starting sensors
        createExpId()

        if (configParams.accelFreq > 0) { startAccel() }
        if (configParams.gyroFreq > 0) { startGyro() }
        if (configParams.heartFreq > 0) { startHeart() }
        if (configParams.offbodyFreq > 0) { startOffbody() }

        if (configParams.bleMode != BLEMode.OFF) { startBLE() }
    }

    private fun stopSensors() {
        // Stop all sensors and signal to update App state.
        stopAccel()
        stopGyro()
        stopHeart()
        stopOffbody()
        stopBLE()

        // Stop foreground service and cancel notification
        stopForeground(STOP_FOREGROUND_REMOVE)

        // Change App state to IDLE
        broadcastStateChange(AppStates.IDLE)
    }

    private fun broadcastStateChange(state: AppStates) {
        // Broadcast to signal state change
        val broadcastIntent = Intent("sensor_status")
        broadcastIntent.putExtra("State", state.toString())
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }

    private fun startAccel() {
        sensorAccel = SensorAccelerometer(
            this.applicationContext,
            sensorManager,
            Sensor.TYPE_ACCELEROMETER,
            configParams.deviceName,
            configParams.protocol,
            expId,
            configParams.accelFreq,
            maxRecordCount,
            configParams.batchSize
        )
        sensorAccel.start()
    }

    private fun stopAccel() {
        if (this::sensorAccel.isInitialized) {
            sensorAccel.reset()
        }
    }

    private fun startGyro() {
        sensorGyro = SensorGyroscope(
            this,
            sensorManager,
            Sensor.TYPE_GYROSCOPE,
            configParams.deviceName,
            configParams.protocol,
            expId,
            configParams.gyroFreq,
            maxRecordCount,
            configParams.batchSize
        )
        sensorGyro.start()
    }

    private fun stopGyro() {
        if (this::sensorGyro.isInitialized) {
            sensorGyro.reset()
        }
    }

    private fun startHeart() {
        sensorHeart = SensorHeart(
            this,
            sensorManager,
            Sensor.TYPE_HEART_RATE,
            configParams.deviceName,
            configParams.protocol,
            expId,
            configParams.heartFreq,
            maxRecordCount,
            configParams.batchSize
        )
        sensorHeart.start()
    }

    private fun stopHeart() {
        if (this::sensorHeart.isInitialized) {
            sensorHeart.reset()
        }
    }

    private fun startOffbody() {
        sensorOffbody = SensorOffbody(
            this,
            sensorManager,
            Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT,
            configParams.deviceName,
            configParams.protocol,
            expId,
            configParams.offbodyFreq,
            maxRecordCount,
            configParams.batchSize
        )
        sensorOffbody.start()
    }

    private fun stopOffbody() {
        if (this::sensorOffbody.isInitialized) {
            sensorOffbody.reset()
        }
    }

    private fun startBLE() {
        Log.d("Debug", "Start BLE ...")
        bleScanner = BLEScanner(
            this,
            configParams.deviceName,
            configParams.bleMode,
            configParams.protocol,
            expId,
            configParams.bleInterval,
            maxRecordCount,
            1500
            )
        bleScanner.start()
    }

    private fun stopBLE() {
        bleScanner.stop()
    }

    private fun loadConfigFile() {
        val file = File(filesDir, configFile)
        configParams = if (file.exists()) {
            val s = file.bufferedReader().readLine()
            Json.decodeFromString(s)
        } else {
            ConfigParams("None")
        }
    }


    enum class Actions {
        START, STOP
    }

}