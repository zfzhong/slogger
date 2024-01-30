/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.slogger.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.SendToMobile
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SendToMobile
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.FileFilter
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.time.LocalDateTime
import java.util.Timer
import java.util.TimerTask
import kotlin.math.exp


class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private lateinit var scheduler: LoggingScheduler
    private lateinit var sensorAccel: SensorAccelerometer
    private lateinit var sensorGyro: SensorGyroscope
    private lateinit var sensorHeart: SensorHeart

    private lateinit var httpController: HttpController

    private lateinit var allFiles: List<File>

    private var foregroundServiceOn = false
    private var configFile = "config.txt"
    private lateinit var configParams: ConfigParams

    private val maxRecordCount = 6000
    private var expId: String = ""

    private val _appState = MutableStateFlow(AppStates.IDLE)
    private val appState = _appState.asStateFlow()

    private fun hasBodySensorsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasPostNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestBodySensorsPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.BODY_SENSORS), 1
        )
    }

    private fun requestPostNotificationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPostNotificationPermission()) {
                requestPostNotificationPermission()
            }
        }

        // Load existing configuration parameters
        loadConfigFile()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        //gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
        //heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)!!

        // request permission for accessing heart rate sensor
        if (!hasBodySensorsPermission()) {
            requestBodySensorsPermission()
        }

        // debug
        //deleteAllFiles()

        setContent {
            val _state by appState.collectAsStateWithLifecycle()

            Scaffold(
                timeText = {
                    TimeText(
                        timeTextStyle = TimeTextDefaults.timeTextStyle(
                            fontSize = 10.sp
                        )
                    )
                },
                vignette = {
                    Vignette(vignettePosition = VignettePosition.TopAndBottom)
                }
            ) {
                SloggerWatchFace(
                    state = _state,
                    onStart = this::start,
                    onReset = this::reset,
                    modifier = Modifier.fillMaxSize(),
                    onUpload = this::upload,
                    onConfigure = this::configure
                )
            }
        }
    }


    private fun createExpId() {
        // Use the current timestamp as expId
        val ts = System.currentTimeMillis()
        expId = ts.toString()
    }

    fun loadConfigFile() {
        val file = File(filesDir, configFile)
        configParams = if (file.exists()) {
            val s = file.bufferedReader().readLine()
            Json.decodeFromString(s)
        } else {
            ConfigParams("None")
        }
    }

    private fun configure() {
        var intent = Intent(this, ConfigListActivity::class.java)
        startActivity(intent)
    }


    fun reset() {
        // 1. previous state is TIMING
        // -> action: cancel startTask
        if (appState.value == AppStates.TIMING) {
            scheduler.cancelStartTask()
        }

        // 2. previous state is LOGGING
        // action: cancel endTask, stop sensors
        if (appState.value == AppStates.LOGGING) {
            scheduler.cancelEndTask()
            stopSensors()
        }

        // 3. If previous state is TRANSFER
        // -> Action: changing state to IDLE will abort uploading files.
        _appState.update { AppStates.IDLE}
    }

    fun start() {
        // This function is called immediately when the Start button is clicked.

        // Start foreground service.
        // Note that we should set permission and SensorLoggingService in the Manifest.xml
        startForegroundService()

        // Load configuration
        loadConfigFile()

        // Change state to TIMING
        _appState.update { AppStates.TIMING}

        // Create a scheduler
        scheduler = LoggingScheduler(WeakReference(this),
            configParams.startDate, configParams.startTimestamp,
            configParams.endDate, configParams.endTimestamp
        )

        // Schedule logging tasks
        scheduler.scheduleLogging()
    }

    fun startSensors() {
        Log.d("Schedule", "Start sensors now...")

        _appState.update { AppStates.LOGGING}

        // Create experiment id before starting sensors
        createExpId()

        if (configParams.accelFreq > 0) { startAccel() }
        if (configParams.gyroFreq > 0) { startGyro() }
        if (configParams.heartFreq > 0) { startHeart() }
    }

    fun finishLogging() {
        Log.d("Logging", "Finish logging.")
        stopSensors()
        _appState.update{AppStates.IDLE}
    }

    private fun stopSensors() {
        Log.d("sensor", "stopSensors()")

        stopAccel()
        stopGyro()
        stopHeart()
    }

    private fun startAccel() {
        sensorAccel = SensorAccelerometer(
            this,
            sensorManager,
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

    private fun startForegroundService() {
        if (!foregroundServiceOn) {
            Intent(this, SensorLoggingService::class.java).also {
                it.action = SensorLoggingService.Actions.START.toString()
                startService(it)
            }
            foregroundServiceOn = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        stopSensors()

        // Stop foreground service
        Intent(this, SensorLoggingService::class.java).also {
            it.action = SensorLoggingService.Actions.STOP.toString()
            startService(it)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("onPause", "onPause called.")
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume", "onResume called.")
    }


    fun upload() {
        _appState.update {AppStates.TRANSFER}

        // Initialize httpController
        if (!this::httpController.isInitialized) {
            httpController = HttpController(WeakReference(this),configParams.xferLink)
        }

        // List all the files in current directory and upload them to the server.
        val excludeConfigFilter = ExcludeConfigFileFilter()
        val files = filesDir.listFiles(excludeConfigFilter)
        Log.d("Upload", "total files: ${files.size}")

        if (files != null) {
            allFiles = files.sortedWith(
                object : Comparator<File> {
                    override fun compare(f0: File, f1: File): Int {
                        val idx = 4 // file number

                        val i = f0.name.split('_')[idx].toInt()
                        val j = f1.name.split('_')[idx].toInt()

                        if (i > j) {
                            return 1
                        }
                        if (i == j) {
                            return 0
                        }
                        return -1
                    }
                })
        }

        // Everytime the upload() function is called, we reset the numOfSentFiles. So
        // uploading starts from the very first file, even though that some files might
        // be uploaded before.
        httpController.resetNumOfSentFiles()

        uploadNext(0)
    }

    fun uploadNext(i: Int) {
        if (appState.value != AppStates.TRANSFER) {
            Log.d("Upload", "Uploading aborted.")
            return
        }

        if (i >= allFiles.size) {
            Log.d("Finish Uploading", "uploaded $i Files")
            finishUploading()
            return
        }

        val file = allFiles[i]
        Log.d("Upload", "$i, ${file.name}")
        lifecycleScope.launch(Dispatchers.IO) {
            httpController.sendFileRequest(file)
            //httpController.sendGetRequest()
        }
    }

    private fun finishUploading() {
        _appState.update { AppStates.IDLE}
    }

    fun deleteAllFiles() {
        // For debugging purpose
        var files = filesDir.listFiles()
        for (file in files!!) {
            Log.d("filename", file.name)
            if (file.name != "config.txt") {
                file.delete()
            }
        }
    }
}


@Composable
private fun SloggerWatchFace(
    state: AppStates,
    onStart: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    onUpload: () -> Unit,
    onConfigure: () -> Unit
) {
    // Button color when the App state is TRANSFER
    var colorTransfer = MaterialTheme.colors.surface

    // Button color when the App state is LOGGING
    var colorLogging = MaterialTheme.colors.surface

    var colorText = Color.White
    var stateText = "Idle"

    when (state) {
        AppStates.IDLE -> {
            colorText = Color.White
            stateText = "Idle"
        }
        AppStates.TIMING -> {
            colorText = Color.Yellow
            stateText = "Timing"
        }

        AppStates.LOGGING -> {
            //colorLogging = MaterialTheme.colors.primary
            colorText = Color.Green
            stateText = "Logging"
        }

        AppStates.TRANSFER -> {
            //colorTransfer = MaterialTheme.colors.primary
            colorText = Color.Cyan
            stateText = "Uploading"
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stateText,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = colorText
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Two buttons in the first row: Start/Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // 1. The Start button
            Button(
                onClick = onStart,

                // The Start button is disabled only when the App state is TRANSFER.
                enabled = (state == AppStates.IDLE),

                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorLogging
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
            }

            // Space between buttons
            Spacer(modifier = Modifier.width(8.dp))

            // 2. The Reset button
            Button(
                onClick = onReset,

                // The Reset button is disabled if the App state is IDLE;
                // The Reset button is enabled only when the App state is not IDLE.
                enabled = (state != AppStates.IDLE),

                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null
                )
            }
        }

        // Space between the 1st row and the 2nd row
        Spacer(modifier = Modifier.height(4.dp))

        // Two buttons in the second row: Upload/Configure
        Row() {
            // 1. The Upload button
            Button(
                onClick = onUpload,

                // The Upload button is enabled only when the App state is IDLE.
                enabled = (state == AppStates.IDLE),

                colors = ButtonDefaults.buttonColors(
                    //backgroundColor = MaterialTheme.colors.surface
                    backgroundColor = colorTransfer
                )
            ) {
                Icon(
                    // imageVector = Icons.Default.SendToMobile, // deprecated
                    imageVector = Icons.AutoMirrored.Filled.SendToMobile,
                    contentDescription = null
                )
            }

            // Space between two buttons in this row
            Spacer(modifier = Modifier.width(8.dp))

            // 2. The Configure button
            Button(
                onClick = onConfigure,

                // The Configure button is disabled if the App state is not IDLE;
                // The Configure button is enabled only when the App state is IDLE.
                enabled = (state == AppStates.IDLE),

                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null
                )
            }
        }
    }
}

class ExcludeConfigFileFilter : FileFilter {
    override fun accept(file: File): Boolean {
        // Return true to include the file, false to exclude it

        // For google pixel watch 2, there is a "profileInstalled" file on the device.
        //return file.isFile && file.name != "config.txt" && file.name !="profileInstalled"

        return (file.name.split('_').size == 7)
    }
}