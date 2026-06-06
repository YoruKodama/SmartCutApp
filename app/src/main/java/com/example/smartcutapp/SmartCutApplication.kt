package com.example.smartcutapp

import android.app.Application
import com.example.smartcutapp.data.local.PreferencesManager

class SmartCutApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
    }
}
