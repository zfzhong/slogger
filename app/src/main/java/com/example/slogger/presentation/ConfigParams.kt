package com.example.slogger.presentation


import kotlinx.serialization.Serializable

@Serializable
data class ConfigParams(
    var deviceName:String="None",
    var startDate: Int = 0,
    var startTimestamp: Int = 0,
    var endDate: Int = 0,
    var endTimestamp: Int = 0,
    var accelFreq: Int = 0,
    var gyroFreq: Int = 0,
    var heartFreq: Int = 1,
    var xferLink: String = "https://weardatadl.com:8443/android_xfer/"
    //var xferLink: String = "http://192.168.1.214:8000/android_xfer/"
) {
    fun getStartDate():String {
        val day = String.format("%02d", getDay(startDate))
        val month = String.format("%02d", getMonth(startDate))
        var year = getYear(startDate)
        if (year < 100) {
            year += 2000
        }
        return "$year-$month-$day"
    }

    fun getEndDate():String {
        val day = String.format("%02d", getDay(endDate))
        val month = String.format("%02d", getMonth(endDate))
        var year = getYear(endDate)
        if (year < 100) {
            year += 2000
        }
        return "$year-$month-$day"
    }

    fun getStartTime(): String {
        val startHour = String.format("%02d", getHour(startTimestamp))
        val startMinute = String.format("%02d", getMinute(startTimestamp))
        val startSecond = String.format("%02d", getSecond(startTimestamp))

        return "$startHour:$startMinute:$startSecond"
    }

    fun getEndTime(): String {
        val endHour = String.format("%02d", getHour(endTimestamp))
        val endMinute = String.format("%02d", getMinute(endTimestamp))
        val endSecond = String.format("%02d", getSecond(endTimestamp))

        return "$endHour:$endMinute:$endSecond"
    }
}