package com.genglxy.clipperboard

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.genglxy.clipperboard.logic.ResultRepository

class ClipperboardApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        ResultRepository.initialize(this)
    }
}