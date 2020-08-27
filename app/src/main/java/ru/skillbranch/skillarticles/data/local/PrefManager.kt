package ru.skillbranch.skillarticles.data.local

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.models.AppSettings

object PrefManager {

    internal val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    private val appSettings = MutableLiveData<AppSettings>()
    private val isAuth = MutableLiveData<Boolean>()

    init {
        val isDarkMode = preferences.getBoolean("darkMode", false)
        val isBigText = preferences.getBoolean("bigText", false)
        appSettings.value = AppSettings(isDarkMode, isBigText)

        isAuth.value = preferences.getBoolean("auth", false)
    }

    fun clearAll() {
        preferences.edit().clear().apply()
    }

    fun getAppSettings(): LiveData<AppSettings> = appSettings

    fun setAppSettings(sett: AppSettings) {
        preferences.edit()
                .putBoolean("darkMode", sett.isDarkMode)
                .putBoolean("bigText", sett.isBigText)
                .apply()
        appSettings.value = sett
    }

    fun isAuth(): MutableLiveData<Boolean> = isAuth

    fun setAuth(auth: Boolean) {
        preferences.edit()
                .putBoolean("auth", auth)
                .apply()
        isAuth.value = auth
    }

}