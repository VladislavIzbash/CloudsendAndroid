package ru.vizbash.cloudsend.data.persistence.settings

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val PREF_NAME = "settings"

class PreferencesSettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun <T> save(setting: Setting<T>, value: T) = sharedPrefs.edit(commit = true) {
        when (setting) {
            is Setting.Str -> putString(setting.key, value as String)
            is Setting.Bool -> putBoolean(setting.key, value as Boolean)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(setting: Setting<T>): T {
        return when (setting) {
            is Setting.Str -> sharedPrefs.getString(setting.key, setting.default) as T
            is Setting.Bool -> sharedPrefs.getBoolean(setting.key, setting.default) as T
        }
    }
}