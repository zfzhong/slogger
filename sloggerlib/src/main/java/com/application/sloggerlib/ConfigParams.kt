package com.application.sloggerlib

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
    // The platform's own accelerometer-minus-gravity stream, recorded at the
    // accelerometer's rate as a check on the fusion the analysis does offline.
    // On by default: it is one extra file per session, and a fused estimate
    // nobody recorded cannot be compared against afterwards.
    var logLinearAccel: Boolean = true,
    // The magnetic field. A rate rather than a flag, chosen on the watch like
    // the accelerometer's and the gyroscope's, because the field is worth
    // different rates for different questions: Game (50 Hz) to compare it with
    // the other streams sample for sample, Normal (5 Hz) when it is only being
    // watched for a tablet coming near.
    //
    // It is the only sensor that sees heading - gravity fixes two angles of an
    // orientation and says nothing about the third - and the only one that
    // feels how close a tablet is, since a tablet carries magnets of its own.
    var magFreq: Int = 50,
    var heartFreq: Int = 0,
    var offbodyFreq: Int = 0,
    var bleScanInterval: Int = 20, // BLE scanning duration
    var bleRestInterval: Int = 20, // BLE scan resting interval
    var bleMode: BLEMode = BLEMode.OFF,
    var bleScanPower: BLEScanPower = BLEScanPower.LowLatency,
    var bleAdMode: BLEAdMode = BLEAdMode.LowLatency,
    var bleAdPower: BLEAdPower = BLEAdPower.High,
    var bleFilterDeviceNames: String = "",
    var batchSize: Int = 1,
    var baseURL: String ="https://withings.geosketch.art",
    //var baseURL: String ="https://weardatadl.com:8443",
    var suffixURL: String = "/cmii/upload/",
    // weardatadl.com serves a certificate the system store does not accept, so
    // that host needs the old skip-verification path. Leave this false for any
    // host with a real certificate - it disables ALL TLS checking.
    var allowInsecureTls: Boolean = false,
    var lastUploadedCount:Int = 0,
    var isWearable:Boolean = true
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