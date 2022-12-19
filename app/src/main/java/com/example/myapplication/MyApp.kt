package com.example.myapplication

import android.app.Application
import java.io.Closeable

class MyApp : Application() {
    companion object {
        const val a = 1
        val b: Closeable? = null
        var instance: MyApp? = null
    }

    object x {
        const val a = 1
        val b = 1
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

object Temp {
    const val x = ""

    val y = func()

    fun func(): String {
        println("wds")
        return ""
    }
}
