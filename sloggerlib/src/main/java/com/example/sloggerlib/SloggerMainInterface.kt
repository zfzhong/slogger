package com.example.sloggerlib

interface SloggerMainInterface {
    fun uploadNext(i: Int)
    fun getLogger(): DebugLogger

    fun updateState(state: AppStates)

}