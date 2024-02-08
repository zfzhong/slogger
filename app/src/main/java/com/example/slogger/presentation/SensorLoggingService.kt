package com.example.slogger.presentation

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
import com.example.slogger.R
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.ref.WeakReference


class SensorLoggingService: Service() {
    private lateinit var sensorManager:SensorManager

    private lateinit var configParams: ConfigParams
    private lateinit var sensorAccel: SensorAccelerometer
    private lateinit var sensorGyro: SensorGyroscope
    private lateinit var sensorHeart: SensorHeart


    private var configFile = "config.txt"

    private val maxRecordCount = 6000
    private var expId: String = ""

    val channelId = "LoggingChanel"
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Logging Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    private fun start() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setContentTitle("WearSlog On:")
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

    fun startSensors() {
        Log.d("Debug", "Service: Start sensors now...")

        // Create experiment id before starting sensors
        createExpId()

        if (configParams.accelFreq > 0) { startAccel() }
        if (configParams.gyroFreq > 0) { startGyro() }
        if (configParams.heartFreq > 0) { startHeart() }
    }

    private fun stopSensors() {
        // Stop all sensors and signal to update App state.
        stopAccel()
        stopGyro()
        stopHeart()

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
            expId,
            configParams.accelFreq,
            maxRecordCount
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
            expId,
            configParams.gyroFreq,
            maxRecordCount
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
            expId,
            configParams.heartFreq,
            maxRecordCount
        )
        sensorHeart.start()
    }

    private fun stopHeart() {
        if (this::sensorHeart.isInitialized) {
            sensorHeart.reset()
        }
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