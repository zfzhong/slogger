package com.example.sloggerlib

import kotlinx.serialization.Serializable


@Serializable
data class ConfigParams(
    var deviceName:String="None",
    var protocol:String="None",
    var startDate: Int = 0,
    var startTimestamp: Int = 0,
    var endDate: Int = 0,
    var endTimestamp: Int = 0,
    var accelFreq: Int = 0,
    var gyroFreq: Int = 0,
    var heartFreq: Int = 0,
    var offbodyFreq: Int = 0,
    var batchSize: Int = 1,
    var baseURL: String ="https://weardatadl.com:8443",
    var suffixURL: String = "/android_xfer/",
    var lastUploadedCount:Int = 0
) {
    fun getServerURL(): String {
        return baseURL + suffixURL
    }

    fun getBaseDomain(): String{
        val tokens = baseURL.split(":")
        if (tokens.size > 1) {
            return tokens[1]
        }
        return ""
    }

    fun getStartDate():String {
        val day = String.format("%02d", com.example.sloggerlib.getDay(startDate))
        val month = String.format("%02d", com.example.sloggerlib.getMonth(startDate))
        var year = com.example.sloggerlib.getYear(startDate)
        if (year < 100) {
            year += 2000
        }
        return "$year-$month-$day"
    }

    fun getEndDate():String {
        val day = String.format("%02d", com.example.sloggerlib.getDay(endDate))
        val month = String.format("%02d", com.example.sloggerlib.getMonth(endDate))
        var year = com.example.sloggerlib.getYear(endDate)
        if (year < 100) {
            year += 2000
        }
        return "$year-$month-$day"
    }

    fun getStartTime(): String {
        val startHour = String.format("%02d", com.example.sloggerlib.getHour(startTimestamp))
        val startMinute = String.format("%02d", com.example.sloggerlib.getMinute(startTimestamp))
        val startSecond = String.format("%02d", com.example.sloggerlib.getSecond(startTimestamp))

        return "$startHour:$startMinute:$startSecond"
    }

    fun getEndTime(): String {
        val endHour = String.format("%02d", com.example.sloggerlib.getHour(endTimestamp))
        val endMinute = String.format("%02d", com.example.sloggerlib.getMinute(endTimestamp))
        val endSecond = String.format("%02d", com.example.sloggerlib.getSecond(endTimestamp))

        return "$endHour:$endMinute:$endSecond"
    }
}