package com.example.slogger.presentation


import kotlinx.serialization.Serializable


@Serializable
data class ConfigParams(
    var deviceName:String="None",
    var startDate: Long = 0,
    var startTimestamp: Long = 0,
    var endDate: Long = 0,
    var endTimestamp: Long = 0,
    var accelFreq: Int = 0,
    var gyroFreq: Int = 0,
    var heartFreq: Int = 1,
    //var xferLink: String = "https://weardatadl.com:8443/android_xfer/"
    var xferLink: String = "http://192.168.1.214:8000/android_xfer/"
) {
    fun getStartYear(): Int {
        return (startDate / 10000).toInt()
    }

    fun getStartMonth(): Int {
        val md = startDate % 10000
        return (md / 100).toInt()
    }

    fun getStartDay(): Int {
        return (startDate % 100).toInt()
    }

    fun getEndYear(): Int {
        return (endDate / 10000).toInt()
    }

    fun getEndMonth(): Int {
        val md = endDate % 10000
        return (md / 100).toInt()
    }

    fun getEndDay(): Int {
        return (endDate % 100).toInt()
    }

    fun getStartSecond(): Int {
        val x = startTimestamp % 60
        return x.toInt()
    }

    fun getStartMinute(): Int {
        val x = (startTimestamp / 60) % 60
        return x.toInt()
    }

    fun getStartHour(): Int {
        val x = (startTimestamp / 3600) % 24
        return x.toInt()
    }

    fun getEndSecond(): Int {
        val x = endTimestamp % 60
        return x.toInt()
    }

    fun getEndMinute(): Int {
        val x = (endTimestamp / 60) % 60
        return x.toInt()
    }

    fun getEndHour(): Int {
        val x = (endTimestamp / 3600) % 24
        return x.toInt()
    }

    fun getStartDate():String {
        val day = String.format("%02d", getStartDay())
        val month = String.format("%02d", getStartMonth())
        var year = getStartYear()
        if (year < 100) {
            year += 2000
        }
        return "$year-$month-$day"
    }

    fun getEndDate():String {
        val day = String.format("%02d", getEndDay())
        val month = String.format("%02d", getEndMonth())
        var year = getEndYear()
        if (year < 100) {
            year += 2000
        }
        return "$year-$month-$day"
    }

    fun getStartTime(): String {
        val startHour = String.format("%02d", getStartHour())
        val startMinute = String.format("%02d", getStartMinute())
        val startSecond = String.format("%02d", getStartSecond())

        return "$startHour:$startMinute:$startSecond"
    }

    fun getEndTime(): String {
        val endHour = String.format("%02d", getEndHour())
        val endMinute = String.format("%02d", getEndMinute())
        val endSecond = String.format("%02d", getEndSecond())

        return "$endHour:$endMinute:$endSecond"
    }
}