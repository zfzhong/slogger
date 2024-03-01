package com.example.slogger.presentation


import android.hardware.SensorManager
import android.util.Log
import java.lang.Exception
import java.time.Duration
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

fun getTimeDifferenceInSeconds(now: LocalDateTime, future: LocalDateTime): Long {
    var duration = Duration.between(now, future)
    return duration.seconds
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


fun mode2freq(x: String, sensorTag: String): Int {
    if (x == "Off") {
        return 0
    } else if (x == "Normal") {
        if (sensorTag == "Heart" || sensorTag == "OffBody") {
            return 1
        }
        return 5// 200 ms
    } else if (x == "UI") {
        return 16 // 60 ms
    } else if (x == "Game") {
        return 50 // 20 ms
    } else if (x == "Fastest") {
        return 100 // should be >= 100 Hz
    }

    return 0
}

fun freq2mode(i: Int): String {
    if (i == 0) {
        return "Off"
    } else if (i <= 5) {
        return "Normal"
    } else if (i == 16) {
        return "UI"
    } else if (i == 50) {
        return "Game"
    } else if (i == 100) {
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

fun getSensorMode(mode: String): Int {
    if (mode == "Normal") {
        return SensorManager.SENSOR_DELAY_NORMAL
    } else if (mode == "UI") {
        return SensorManager.SENSOR_DELAY_UI
    } else if (mode == "Game") {
        return SensorManager.SENSOR_DELAY_GAME
    } else if (mode == "Fastest") {
        return SensorManager.SENSOR_DELAY_FASTEST
    }

    throw Exception("Undefined sensor Mode: $mode")
}

fun getYear(yyyymmdd: Int): Int {
    return yyyymmdd / 10000
}

fun getMonth(yyyymmdd: Int): Int {
    val md = yyyymmdd % 10000
    return md / 100
}

fun getDay(yyyymmdd: Int): Int {
    return yyyymmdd % 100
}

fun getHour(timestamp: Int): Int {
    return timestamp / 3600
}

fun getMinute(timestamp: Int): Int {
    val x = timestamp % 3600
    return x / 60
}

fun getSecond(timestamp: Int): Int {
    return timestamp % 60
}





