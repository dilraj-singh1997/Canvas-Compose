package com.example.myapplication

import android.app.Application

class MyApp : Application() {
    companion object {
        var instance: MyApp? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
