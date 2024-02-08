package com.example.slogger.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StopLoggingAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        //val action = intent?.getStringExtra("Action")
        Log.d("Debug", "StopLoggingAlarmReceiver")

        val serviceIntent = Intent(context, SensorLoggingService::class.java)
        serviceIntent.action = SensorLoggingService.Actions.STOP.toString()

        context?.startService(serviceIntent)
    }
}
