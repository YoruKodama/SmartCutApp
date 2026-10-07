package com.example.smartcutapp.data.local

import android.content.Context
import com.example.smartcutapp.domain.model.CutType

object PreferencesManager {

    private const val PREFS_NAME = "smartcut_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_ATTACHMENTS = "attachments"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_AUTO_CONFIRM = "auto_confirm"
    private const val KEY_DAILY_KCAL_GOAL = "daily_kcal_goal"
    private const val KEY_RECIPES_CACHE = "recipes_cache"
    private const val KEY_JOURNAL = "journal"
    private const val KEY_NUTRITION_CACHE = "nutrition_cache"

    private lateinit var ctx: Context

    fun init(context: Context) {
        ctx = context.applicationContext
    }

    private val prefs get() = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var token: String
        get() = prefs.getString(KEY_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var themeMode: Int
        get() = prefs.getInt(KEY_THEME_MODE, 0)
        set(value) = prefs.edit().putInt(KEY_THEME_MODE, value).apply()


    /** Насадки, которые есть у пользователя (по умолчанию — все). */
    var availableAttachments: Set<CutType>
        get() {
            val raw = prefs.getString(KEY_ATTACHMENTS, null) ?: return CutType.entries.toSet()
            return raw.split(",").mapNotNull { CutType.fromApi(it) }.toSet()
        }
        set(value) = prefs.edit().putString(KEY_ATTACHMENTS, value.joinToString(",") { it.apiName }).apply()

    /** Код привязанного устройства (из QR или введённый вручную). Пусто — общие топики по умолчанию. */
    var deviceId: String
        get() = prefs.getString(KEY_DEVICE_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()

    /** Режим «байпасс»: в команде нарезки просим устройство подтверждать старт автоматически. */
    var autoConfirm: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CONFIRM, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CONFIRM, value).apply()

    var dailyKcalGoal: Int
        get() = prefs.getInt(KEY_DAILY_KCAL_GOAL, 2000)
        set(value) = prefs.edit().putInt(KEY_DAILY_KCAL_GOAL, value).apply()

    var recipesCache: String
        get() = prefs.getString(KEY_RECIPES_CACHE, "[]") ?: "[]"
        set(value) = prefs.edit().putString(KEY_RECIPES_CACHE, value).apply()

    var journalJson: String
        get() = prefs.getString(KEY_JOURNAL, "[]") ?: "[]"
        set(value) = prefs.edit().putString(KEY_JOURNAL, value).apply()

    var nutritionCacheJson: String
        get() = prefs.getString(KEY_NUTRITION_CACHE, "{}") ?: "{}"
        set(value) = prefs.edit().putString(KEY_NUTRITION_CACHE, value).apply()
}
