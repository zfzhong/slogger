package com.example.slogger.presentation

import android.util.Log
import java.lang.ref.WeakReference
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class LoggingScheduler(
    val mainActivity: WeakReference<MainActivity>,
    val startDate: Int,
    val startTimestamp: Int,
    val endDate: Int,
    val endTimestamp: Int) {

    private val startTime = LocalDateTime.of(
        getYear(startDate), getMonth(startDate), getDay(startDate),
        getHour(startTimestamp), getMinute(startTimestamp), getSecond(startTimestamp)
    )

    private val endTime = LocalDateTime.of(
        getYear(endDate), getMonth(endDate), getDay(endDate),
        getHour(endTimestamp), getMinute(endTimestamp), getSecond(endTimestamp)
    )

    private lateinit var startTask: TimerTask
    private lateinit var endTask: TimerTask


    private fun scheduleFuture(ms: Long) {
        Log.d("Schedule", "Start in $ms milliseconds.")
        val timer = Timer()

        // Define your future/start task
        startTask = object : TimerTask() {
            override fun run() {
                scheduleCurrent()
            }
        }

        // Schedule the task to start after the delay
        timer.schedule(startTask, ms)
    }

    private fun scheduleCurrent() {
        // start sensors
        mainActivity.get()?.startSensors()

        // Schedule the timeout task
        val timer = Timer()
        endTask = object : TimerTask() {
            override fun run() {
                Log.d("Schedule", "endTask called -> finishLogging()")
                mainActivity.get()?.finishLogging()
            }
        }

        val now = LocalDateTime.now()
        var delay = getTimeDifferenceInSeconds(now, endTime)

        // delay can't be less than 1 second
        delay = maxOf(delay, 1)

        Log.d("Schedule", "Timeout:$delay")
        timer.schedule(endTask, delay * 1000)
    }

    fun cancelStartTask() {
        Log.d("Schedule", "Cancel startTask ...")
        startTask.cancel()
    }

    fun cancelEndTask() {
        Log.d("Schedule", "Cancel endTask ...")
        endTask.cancel()
    }

    fun scheduleLogging() {
        val now = LocalDateTime.now()

        Log.d("Schedule", "start:$startDate, "+getHour(startTimestamp)+":"
                + getMinute(startTimestamp)+":"+ getSecond(startTimestamp)
        )
        Log.d("Schedule", "end:$endDate, "+getHour(endTimestamp)+":"
                + getMinute(endTimestamp)+":"+ getSecond(endTimestamp)
        )
        Log.d("Schedule", "now: $now")

        if (endTime.isBefore(startTime) || endTime.isBefore(now)){
            // Time configuration wrong. Do nothing.

            //Action: reset()
            mainActivity.get()?.reset()
        } else if (now.isBefore(startTime)) {
            // The startTime is in the future. Start timing.
            var delay = getTimeDifferenceInSeconds(now, startTime)
            delay = maxOf(delay, 1)

            scheduleFuture(delay * 1000)
        } else {
            // Start logging immediately
            scheduleCurrent()
        }
    }
}