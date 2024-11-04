package com.application.sloggerlib

import com.application.sloggerlib.AppStates
import com.application.sloggerlib.DebugLogger

interface SloggerMainInterface {
    fun uploadNext(i: Int)
    fun getLogger(): DebugLogger

    fun updateState(state: AppStates)

}