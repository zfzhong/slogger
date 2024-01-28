package com.example.slogger.presentation


import android.hardware.SensorManager
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Another version of code to generate filename;
 * Since Glenn said the device name could be "PIX01_Sleep".
 * He is the boss. So here it is.
 */
fun genLogFileName(
    deviceName: String, // "PIX014_Sleep"
    sensorType: String, // "Heart", "Accel"
    freq: Int, // 50, 1
    fileId: Int, // 1,2,3,...
    expId: String, //"1698278400157"
    millis: String //"1698278400254"
): String {

    return deviceName + "_" + sensorType + "_" +
            freq.toString() + "_" + fileId.toString() + "_" + expId + "_" + millis +".csv"
    //return "PIX014_Sleep_Heart_50_1_1698278400157_1698278400254.csv"
}

fun genLogFileName2(
    prefix: String, // PIX014_Sleep_Heart
    freq: Int, // 50, 1
    fileId: Int, // 1,2,3,...
    expId: String, //"1698278400157"
    millis: String //"1698278400254"
): String {

    return prefix + "_" +
            freq.toString() + "_" + fileId.toString() + "_" + expId + "_" + millis +".csv"
    //return "PIX014_Sleep_Heart_50_1_1698278400157_1698278400254.csv"
}

fun getCurrentESTMilli(): Long {
    val currentTime = LocalDateTime.now()

    // Convert the time to the Eastern Standard Time (EST) zone
    val estZoneId = ZoneId.of("America/New_York")
    val estTime = currentTime.atZone(estZoneId)

    // Get the time in milliseconds

    return estTime.toInstant().toEpochMilli()
}

fun getTimeDelaySeconds(startDate: Long, startTime: Long): Long {
    val year = (startDate / 10000).toInt()
    val month = ((startDate % 10000) / 100).toInt()
    val day = (startDate % 100).toInt()

    val d1 = LocalDate.now()
    val d2 = LocalDate.of(year, month, day)

    val days = ChronoUnit.DAYS.between(d1, d2)



    val now = LocalDateTime.now()
    val sec1 = now.hour*3600 + now.minute*60 +now.second

    Log.d("Diff", "$d1, $d2, $days, $startTime, $sec1")

    return days * 24 * 3600 + (startTime - sec1)
}

fun isBeforeCurrentTime(startDate: Long, startTime: Long): Boolean {
    val now = LocalDateTime.now()
    val yymmdd = (now.year * 10000 + now.monthValue * 100 + now.dayOfMonth).toLong()
    if (startDate < yymmdd) {
        return true
    }

    if (startDate == yymmdd) {
        val m = now.hour*3600 + now.minute*60 +now.second
        if (startTime <= m) {
            return true
        }
    }

    return false
}

fun getLocalSecondLevelTimestamp(): Int {
    val now = LocalDateTime.now()
    return now.hour*3600 + now.minute*60 +now.second
}


fun mode2freq(x: String): Int {
    if (x == "Normal") {
        return 5 // 200 ms
    }
    if (x == "UI") {
        return 16 // 60 ms
    }
    if (x == "Game") {
        return 50 // 20 ms
    }
    if (x == "Fastest") {
        return 100 // should be >= 100 Hz
    }

    return 0
}

fun freq2mode(i: Int): String {
    if (i == 5) {
        return "Normal"
    }
    if (i == 16) {
        return "UI"
    }
    if (i == 50) {
        return "Game"
    }
    if (i == 100) {
        return "Fastest"
    }
    return "None"
}

fun getSensorMode(freq: Int): Int {
    // We only deal with frequencies: 5, 50, 100
    // Note that SENSOR_DELAY_FASTEST might be 200 Hz.
    var mode = SensorManager.SENSOR_DELAY_NORMAL
    if (freq == 16) {
        mode = SensorManager.SENSOR_DELAY_UI
    }
    if (freq == 50) {
        mode = SensorManager.SENSOR_DELAY_GAME
    }
    if (freq == 100) {
        mode = SensorManager.SENSOR_DELAY_FASTEST
    }
    return mode
}