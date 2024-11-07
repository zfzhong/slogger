package com.application.sloggerlib

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

class LoggingScheduler(
    val context: Context,
    val mainInterface: SloggerMainInterface,
    val startDate: Int,
    val startTimestamp: Int,
    val endDate: Int,
    val endTimestamp: Int) {

    private val startTime = LocalDateTime.of(
        getYear(startDate),
        getMonth(startDate),
        getDay(startDate),
        getHour(startTimestamp),
        getMinute(startTimestamp),
        getSecond(startTimestamp)
    )

    private val endTime = LocalDateTime.of(
        getYear(endDate),
        getMonth(endDate),
        getDay(endDate),
        getHour(endTimestamp),
        getMinute(endTimestamp),
        getSecond(endTimestamp)
    )

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntentStart: PendingIntent
    private lateinit var pendingIntentStop: PendingIntent


    @RequiresApi(Build.VERSION_CODES.S)
    private fun scheduleStartFuture(ms: Long) {
        val action = "StartLogging"
        //Log.d("Debug", "Logging starts in $ms milliseconds.")

        if (!this::alarmManager.isInitialized) {
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
        val intent = Intent(context, StartLoggingAlarmReceiver::class.java)

        // Pass action as extra: StartLogging/StopLogging
        //intent.putExtra("Action", action)

        pendingIntentStart = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_MUTABLE
        )

        // Set the alarm to trigger after a specific time
        val alarmTimeMillis = System.currentTimeMillis() + ms

        /**
         * setExactAndAllowWhileIdle() only works with android 12 or higher;
         * Kindle Fire with Fire OS 8 is with android 11 (api 30).
         */

        if(alarmManager.canScheduleExactAlarms()) {
            val logger = mainInterface.getLogger()
            logger.logDebug("Debug", "[StartLogging] starts in $alarmTimeMillis milliseconds.")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntentStart)
        }
        else {
            Log.d("Debug", "cant schedule exact alarms.")
        }


        /*
        val logger = mainInterface.getLogger()
        logger.logDebug("Debug", "[StartLogging] starts in $alarmTimeMillis milliseconds.")
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntentStart)
        */
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun scheduleStopFuture(ms: Long) {
        //val action = "StopLogging"
        //Log.d("Debug", "[$action] starts in $ms milliseconds.")

        if (!this::alarmManager.isInitialized) {
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
        val intent = Intent(context, StopLoggingAlarmReceiver::class.java)

        // Pass action as extra: StartLogging/StopLogging
        //intent.putExtra("Action", action)

        pendingIntentStop = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_MUTABLE
        )

        // Set the alarm to trigger after a specific time
        val alarmTimeMillis = System.currentTimeMillis() + ms

        /**
         * setExactAndAllowWhileIdle() only works with android 12 or higher;
         * Kindle Fire with Fire OS 8 is with android 11 (api 30).
         */

        if(alarmManager.canScheduleExactAlarms()) {
            val logger = mainInterface.getLogger()
            logger.logDebug("Debug", "[StopLogging] starts in $alarmTimeMillis milliseconds.")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntentStop)
        }


        /*
        val logger = mainInterface.getLogger()
        logger.logDebug("Debug", "[StopLogging] starts in $alarmTimeMillis milliseconds.")
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntentStop)

         */
    }


    fun cancelPendingStart() {
        val logger = mainInterface.getLogger()
        logger.logDebug("Debug", "cancel pendingStart")
        alarmManager.cancel(pendingIntentStart)
    }

    fun cancelPendingStop() {
        val logger = mainInterface.getLogger()
        logger.logDebug("Debug", "cancel pendingStop")
        alarmManager.cancel(pendingIntentStop)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun scheduleLogging() {
        val now = LocalDateTime.now()

        val logger = mainInterface.getLogger()
        logger.logDebug("Debug","start: $startTime, end: $endTime, now:$now")

        if (endTime.isBefore(startTime) || endTime.isBefore(now)){
            // Time configuration wrong. Do nothing. Change state.
            Log.d("Debug", "Time configured wrong.")
            mainInterface.updateState(AppStates.IDLE)
        } else {
            // The startTime is in the future. Start timing.
            // if delay is 0 or negative, the alarm will be fired immediately.

            //Log.d("Debug", "Schedule in future")
            var delay = getTimeDifferenceInSeconds(now, startTime)
            scheduleStartFuture(delay * 1000)

            delay = getTimeDifferenceInSeconds(now, endTime)
            scheduleStopFuture(delay * 1000)
        }
    }
}