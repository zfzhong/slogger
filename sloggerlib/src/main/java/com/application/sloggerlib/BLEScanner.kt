package com.application.sloggerlib

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSetParameters
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.datatypes.Vo2MaxRecord
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import java.io.BufferedWriter
import java.io.File
import java.io.IOException


class BLEScanner (
    private val context: Context,
    private val deviceName: String,
    private val bleMode: BLEMode,
    private val protocol: String,
    private val expId: String,
    private val bleScanPower: BLEScanPower,
    private val bleAdMode: BLEAdMode,
    private val bleAdPower: BLEAdPower,
    private val bleFilterDeviceNames: String, // comma-separated, e.g. "Pix07,Pix08"; empty = no filter
    private val interval: Int, // default 10
    private val maxRecordInFile: Int, //6000
    private val write2fileMaxCount: Int //1500
    ){
    private var isRunning = false

    private lateinit var fileHandler: File

    private var filename = ""
    private var fileSeqNum = 1
    private var currRecordCount = 0
    private var bufferedWriter: BufferedWriter? = null

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private lateinit var requestBluetoothDiscoverableLauncher: ActivityResultLauncher<Intent>

    // Android throttles BLE scans after ~5 minutes of continuous scanning.
    // Restarting every 4 minutes resets the throttle timer and maintains full scan rate.
    private val scanRestartIntervalMillis: Long = 4 * 60 * 1000L
    private val handler = Handler(Looper.getMainLooper())

    // Parsed, trimmed list of device names to keep. Empty list = no filtering (keep all named devices).
    private val filterDeviceNameList: List<String> by lazy {
        bleFilterDeviceNames.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    fun start() {
        if (bleMode == BLEMode.ADVERTISE)
        {
            startBLEAdvertise()
        } else if (bleMode == BLEMode.SCAN) {
            startPeriodicScan()
        }
    }

    fun stop() {
        if (bleMode == BLEMode.ADVERTISE)
        {
            stopBLEAdvertise()
        } else if (bleMode == BLEMode.SCAN) {
            resetScanner()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        Log.d("Debug", "start BLE scan ...")
        val scanSettings = ScanSettings.Builder()
            .setScanMode(toAndroidScanMode(bleScanPower))
            .build()

        val scanFilters = buildScanFilters()
        bleScanner.startScan(scanFilters, scanSettings, scanCallback)
    }

    private fun buildScanFilters(): List<ScanFilter>? {
        if (filterDeviceNameList.isEmpty()) {
            return null
        }

        return filterDeviceNameList.map { deviceName ->
            ScanFilter.Builder()
                .setDeviceName(deviceName)
                .build()
        }
    }

    // Restarts the scan every 4 minutes to prevent Android OS throttling (kicks in after ~5 min).
    private val scanRestartRunnable = object : Runnable {
        @SuppressLint("MissingPermission")
        override fun run() {
            Log.d("Debug", "BLE scan restart (anti-throttle)")
            bleScanner.stopScan(scanCallback)
            startScan()
            handler.postDelayed(this, scanRestartIntervalMillis)
        }
    }

    private fun startPeriodicScan() {
        Log.d("Debug", "start BLE scan -> ")
        startScan()
        isRunning = true
        handler.postDelayed(scanRestartRunnable, scanRestartIntervalMillis)
    }

    private fun stopPeriodicScan() {
        Log.d("Debug", "stop BLE scan")
        handler.removeCallbacks(scanRestartRunnable)
        stopScan()
    }


    // Stop BLE scan
    @SuppressLint("MissingPermission")
    private fun stopScan() {
        Log.d("Debug", "stop BLE scan ...")
        bleScanner.stopScan(scanCallback)

        if (!isRunning) {
            Log.d("Debug", "flush BLE, scanning finished")
            flushBuffer()
        }
    }

    // Create an advertising callback
    private val advertisingCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d("debug", "Advertise: started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("debug", "Advertise: failed with error code $errorCode")
        }
    }
    @SuppressLint("MissingPermission")
    private fun stopBLEAdvertise() {
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        advertiser?.stopAdvertising(advertisingCallback)
        Log.d("Debug", "BLE Advertising stopped")
    }

    @SuppressLint("MissingPermission")
    private fun startBLEAdvertise() {
        if (bluetoothAdapter.isEnabled) {
            // customize the device name when advertising
            //bluetoothAdapter.name = "Google Pixel Watch 7"
            bluetoothAdapter.name = deviceName

            Log.d("Debug", "start BLE advertising: $deviceName")


            val advertiser = bluetoothAdapter.bluetoothLeAdvertiser

            // Build the advertising settings
            /*val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()*/

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(toAndroidAdvertiseMode(bleAdMode))
                .setTxPowerLevel(toAndroidAdvertiseTxPower(bleAdPower))
                .setConnectable(false) // Allow connections if true, otherwise not connectable
                .build()


            // Build the advertising data
            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true) // Include the customized device name
                .build()

            advertiser?.startAdvertising(settings, data, advertisingCallback)
        } else {
            Log.e("debug", "Bluetooth is not enabled.")
        }

    }

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val rssi = result.rssi

            val tsMillis = System.currentTimeMillis()

            val msg = "$tsMillis,$currRecordCount,${device.name},${device.address},$rssi\n"
            //Log.d("debug", msg)

            if (currRecordCount == 0 && bufferedWriter == null) {
                // 1. Get current wall clock timestamp.
                val millis = System.currentTimeMillis().toString()

                // 2. Create a new file for storing logging data.
                filename = genLogFileName(
                    deviceName,
                    protocol,
                    "BLE",
                    interval,
                    fileSeqNum,
                    expId,
                    millis
                )

                //Log.d("debug", "$millis, $currRecordCount: $filename")

                // open the new file
                fileHandler = File(context.filesDir, filename)
                bufferedWriter = fileHandler.bufferedWriter()
            }

            write2File(msg)
            currRecordCount += 1

            if (currRecordCount % write2fileMaxCount == 0) {
                bufferedWriter?.flush()
            }

            if (currRecordCount >= maxRecordInFile) {
                currRecordCount = 0
                fileSeqNum += 1

                // close previous bufferedWriter
                bufferedWriter?.flush()
                bufferedWriter?.close()
                bufferedWriter = null
            }

        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                Log.d("Debug", "Batch Device: ${result.device.name}, RSSI: ${result.rssi}")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("Debug", "Scan failed with error: $errorCode")
        }
    }

    private fun write2File(data: String) {
        try {
            bufferedWriter?.write(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun flushBuffer() {
        try {
            bufferedWriter?.flush()
            bufferedWriter?.close()
            bufferedWriter = null
        } catch (e: IOException) {
            // Handle the exception, e.g., log the error or display a message
            Log.d("Debug","flushBuffer() error: ${e.message}")
        }
    }

    private fun toAndroidScanMode(power: BLEScanPower): Int {
        return when (power) {
            BLEScanPower.LowPower -> ScanSettings.SCAN_MODE_LOW_POWER
            BLEScanPower.Balanced -> ScanSettings.SCAN_MODE_BALANCED
            BLEScanPower.LowLatency -> ScanSettings.SCAN_MODE_LOW_LATENCY
            BLEScanPower.Opportunistic -> ScanSettings.SCAN_MODE_OPPORTUNISTIC
        }
    }

    private fun toAndroidAdvertiseMode(mode: BLEAdMode): Int {
        return when (mode) {
            BLEAdMode.LowPower -> AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
            BLEAdMode.Balanced -> AdvertiseSettings.ADVERTISE_MODE_BALANCED
            BLEAdMode.LowLatency -> AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
        }
    }

    private fun toAndroidAdvertiseTxPower(power: BLEAdPower): Int {
        return when (power) {
            BLEAdPower.UltraLow -> AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW
            BLEAdPower.Low -> AdvertiseSettings.ADVERTISE_TX_POWER_LOW
            BLEAdPower.Medium -> AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM
            BLEAdPower.High -> AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
        }
    }

    private fun resetScanner() {
        Log.d("Debug","Reset BLE Scan")

        if (isRunning) {
            isRunning = false

            // stop scan
            stopPeriodicScan()

            // We dont close bufferedWriter, since the callback might still
            // invokes bufferedWriter after this.

            // reset parameters
            // Any other parameters? How about experiment Id?

            filename = ""
            fileSeqNum = 1
            currRecordCount = 0
        }
    }
}
