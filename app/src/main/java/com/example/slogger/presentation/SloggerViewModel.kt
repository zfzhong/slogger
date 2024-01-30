@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.slogger.presentation

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import androidx.lifecycle.lifecycleScope
import kotlinx.serialization.json.Json
import java.io.File


class SloggerViewModel(
    private val context: Context,
    private val sensorManager: SensorManager
)
    : ViewModel()
{
    private lateinit var httpController: HttpController
    private lateinit var sensorAccel: SensorAccelerometer
    private lateinit var scheduler: LoggingScheduler


    private var configFile = "config.txt"
    private lateinit var configParams: ConfigParams

    private var expId: String = ""
    private val maxRecordCount = 6000

    // Initialize the App state: IDLE
    private val _appState = MutableStateFlow(AppStates.IDLE)
    val appState = _appState.asStateFlow()



}


