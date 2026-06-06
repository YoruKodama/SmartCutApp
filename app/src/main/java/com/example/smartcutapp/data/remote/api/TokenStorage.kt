package com.example.smartcutapp.data.remote.api

import com.example.smartcutapp.data.local.PreferencesManager

object TokenStorage {
    var token: String
        get() = PreferencesManager.token
        set(value) { PreferencesManager.token = value }
}
