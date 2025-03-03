package com.fd.ltl.service

import androidx.lifecycle.MutableLiveData

object LiveTrackingServiceLiveData {

    const val START = "start"
    const val STOP = "stop"
    const val UPDATE = "update"

    val state = MutableLiveData<String>()
}