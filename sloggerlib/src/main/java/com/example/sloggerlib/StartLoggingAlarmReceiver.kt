package com.example.sloggerlib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StartLoggingAlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        //val action = intent?.getStringExtra("Action")
        Log.d("Debug", "StartLoggingAlarmReceiver")

        val action = intent?.action

        val serviceIntent = Intent(context, SensorLoggingService::class.java)
        serviceIntent.action = SensorLoggingService.Actions.START.toString()

        context?.startService(serviceIntent)
    }

}