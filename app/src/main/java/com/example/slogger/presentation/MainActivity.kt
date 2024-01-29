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
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.FileFilter
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.util.Timer
import java.util.TimerTask
import kotlin.math.exp


class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager

    //private lateinit var sensorAccel: SensorAccelerometer
    private lateinit var accelSensor: Sensor
    private lateinit var gyroSensor: Sensor
    private lateinit var heartSensor: Sensor

    private lateinit var accelFile: FileOutputStream
    private lateinit var gyroFile: FileOutputStream
    private lateinit var heartFile: FileOutputStream

    private lateinit var allFiles: List<File>

    private var foregroundServiceOn = false
    private var configFile = "config.txt"
    private lateinit var configParams: ConfigParams

    //private val deviceName = "SAM01"
    //private val activityName = "Activity"
    private val sensorAccelType = "Accel"
    private val sensorGyroType = "Gyro"
    private val sensorHeartType = "Heart"
    //private val accelFreq = 50
    //private val gyroFreq = 50
    //private val heartFreq = 1
    private var accelRunning = false
    private var gyroRunning = false
    private var heartRunning = false

    private var isStartWaiting = false
    private var isEndWaiting = false

    private lateinit var taskStart: TimerTask
    private lateinit var taskEnd: TimerTask

    private var accelFileId = 1
    private var gyroFileId = 1
    private var heartFileId = 1

    private var expId: String = ""

    private val maxRecordCount = 6000
    private var accelRecordCount = 0
    private var gyroRecordCount = 0
    private var heartRecordCount = 0

    private var accelFileName = ""
    private var gyroFileName = ""
    private var heartFileName = ""

    private lateinit var httpController: HttpController
    private lateinit var viewModel: SloggerViewModel

    fun createExpId() {
        if (expId == "") {
            // Use the current timestamp as expId
            val ts = System.currentTimeMillis()
            expId = ts.toString()
        }
    }

    fun resetExperimentParams() {
        // rest expId
        expId = ""
        accelFileId = 1
        gyroFileId = 1
        heartFileId = 1

        accelRecordCount = 0
        gyroRecordCount = 0
        heartRecordCount = 0

        accelFileName = ""
        gyroFileName = ""
        heartFileName = ""
    }

    fun upload() {
        // Initialize httpController
        if (!this::httpController.isInitialized) {
            httpController = HttpController(configParams.xferLink)
            httpController.setMainActivityReference(this)
        }

        // List all the files in current directory and upload them to the server.
        val excludeConfigFilter = ExcludeConfigFileFilter()
        val files = filesDir.listFiles(excludeConfigFilter)

        if (files != null) {
            allFiles = files.sortedWith(
                object : Comparator <File> {
                    override fun compare (f0: File, f1: File) : Int {
                        val idx = 4 // file number

                        val i = f0.name.split('_')[idx].toInt()
                        val j = f1.name.split('_')[idx].toInt()

                        if (i > j)  { return 1 }
                        if (i == j) { return 0 }
                        return -1
                    }
                })
        }

        // Everytime the upload() function is called, we reset the numOfSentFiles. So
        // uploading starts from the very first file, even though that file got uploaded
        // before.
        httpController.resetNumOfSentFiles()
        uploadNext(0)
    }

    fun uploadNext(i: Int) {
        if (i >= allFiles.size) {
            Log.d("Finish Uploading", "uploaded $i Files")
            viewModel.finishUploading()
            return
        }

        val file = allFiles[i]
        Log.d("Upload", file.name)
        lifecycleScope.launch(Dispatchers.IO) {
            httpController.sendFileRequest(file)
        }
    }

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

    private fun loadConfigFile() {
        val file = File(filesDir, configFile)
        configParams = if (file.exists()) {
            val s = file.bufferedReader().readLine()
            Json.decodeFromString(s)
        } else {
            ConfigParams("None")
        }
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

        //window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //lifecycle.addObserver(ambientObserver)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
        heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)!!

        // request permission for accessing heart rate sensor
        if (!hasBodySensorsPermission()) {
            requestBodySensorsPermission()
        }

        // debug
        //deleteAllFiles()

        setContent {
            //val factory = StopWatchViewModelFactory(accelController)
            //val viewModel = ViewModelProvider(this, factory).get(StopWatchViewModel::class.java)
            viewModel = viewModel<SloggerViewModel>()
            viewModel.setMainActivityReference(this)

            val appState by viewModel.appState.collectAsStateWithLifecycle()

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
                    state = appState,
                    onStart = viewModel::start,
                    onReset = viewModel::reset,
                    modifier = Modifier.fillMaxSize(),
                    onUpload = viewModel::upload,
                    onConfigure = this::setConfigList
                )
            }
        }
    }

    // configuration
    fun setConfiguration() {
//        var intent = Intent(this, ConfigurationActivity::class.java)
//        startActivity(intent)
    }

    fun setConfigList() {
        var intent = Intent(this, ConfigListActivity::class.java)
        startActivity(intent)
    }

    // Start foreground service
    fun startService() {
//        if (!foregroundServiceOn) {
//            Intent(applicationContext, SensorLoggingService::class.java).also {
//                it.action = SensorLoggingService.Actions.START.toString()
//                startService(it)
//            }
//            foregroundServiceOn = true
//        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop Service
//        Intent(applicationContext, SensorLoggingService::class.java).also {
//            it.action = SensorLoggingService.Actions.STOP.toString()
//            startService(it)
//        }
    }
    override fun onPause() {
        super.onPause()
        Log.d("onPause", "onPause called.")
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume", "onResume called.")
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
            ){
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
        return file.isFile && file.name != "config.txt"
    }
}