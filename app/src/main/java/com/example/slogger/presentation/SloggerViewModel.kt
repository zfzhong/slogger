@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.slogger.presentation

import android.content.Context
import android.content.Intent
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



class SloggerViewModel(): ViewModel() {
    private var activityReference: WeakReference<MainActivity>? = null

    // Initialize the App state: IDLE
    var stateText: String = "Idle"
    private val _appState = MutableStateFlow(AppStates.IDLE)
    val appState = _appState.asStateFlow()


    fun setMainActivityReference(activity: MainActivity) {
        activityReference = WeakReference(activity)
    }

    fun start() {
        Log.d("debug", "start() called")
        // Event "Start", fired when the Start button is clicked.

        // 1. Verifying that the App state is IDLE.
        if (appState.value != AppStates.IDLE) {
            return
        }

        // Schedule logging and update the App state
        _appState.update { AppStates.LOGGING }

        val activity = activityReference?.get()
    }

    fun upload() {
        Log.d("debug", "upload() called")
        // Event "Upload", fired when the Upload button is clicked.

        // 1. Verifying that the App state is IDLE.
        if (appState.value != AppStates.IDLE) {
            return
        }

        _appState.update { AppStates.TRANSFER }

    }

    fun reset() {
        Log.d("debug", "reset() called")
        // Event "Reset", fired when the Reset button is clicked.

        // Return if the App state is IDLE
        if (appState.value == AppStates.IDLE) {
            return
        }

        // Reset the App

        // Change the App state to IDLE
        _appState.update {AppStates.IDLE}
    }

    fun finishUploading() {
        reset()
    }


}


