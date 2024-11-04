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


class BLEScanner (
    private val context: Context,
    private val deviceName: String,
    private val bleMode: BLEMode,
    private val protocol: String,
    private val expId: String,
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


    private val scanIntervalMillis: Long = (interval * 1000).toLong() // Interval between scans
    private val scanDurationMillis: Long = 300000 // Duration always 2000 ms
    private val handler = Handler(Looper.getMainLooper())


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
            //.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        val scanFilters: MutableList<ScanFilter> = ArrayList()

        // Optionally, you can also filter by device name
        scanFilters.add(
            ScanFilter.Builder()
                .setDeviceName("Pix07")
                .build()
        )

        bleScanner.startScan(scanFilters, scanSettings, scanCallback)
    }

    // Start periodic scanning
    private fun startPeriodicScan() {
        Log.d("Debug", "start BLE periodic scan -> ")

        handler.post(scanRunnable)
        isRunning = true
    }

    // Stop periodic scanning
    private fun stopPeriodicScan() {
        Log.d("Debug", "Cancel BLE periodic scan")
        handler.removeCallbacks(scanRunnable)
        stopScan()
    }

    // Runnable to handle periodic scanning
    private val scanRunnable = object : Runnable {
        override fun run() {
            startScan()
            handler.postDelayed({ stopScan() }, scanDurationMillis) // Stop scan after scanDurationMillis
            handler.postDelayed(this, scanIntervalMillis) // Schedule next scan
        }
    }


    // Stop BLE scan
    @SuppressLint("MissingPermission")
    private fun stopScan() {
        Log.d("Debug", "stop BLE scan ...")
        bleScanner.stopScan(scanCallback)

        if (!isRunning) {
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
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
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
            Log.d("debug", msg)

            if (currRecordCount == 0) {
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

                Log.d("debug", "$millis, $currRecordCount: $filename")

                // open the new file
                fileHandler = File(context.filesDir, filename)

                // close previous bufferedWriter
                bufferedWriter?.flush()
                bufferedWriter?.close()
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
        bufferedWriter?.flush()
        bufferedWriter?.close()
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