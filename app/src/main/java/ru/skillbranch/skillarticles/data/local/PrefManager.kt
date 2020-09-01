package ru.skillbranch.skillarticles.data.local

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate
import ru.skillbranch.skillarticles.data.models.AppSettings

object PrefManager {

    internal val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    private var isDarkMode by PrefDelegate(false)
    private var isBigText by PrefDelegate(false)
    private var isAuth by PrefDelegate(false)

    private val appSettings = MutableLiveData<AppSettings>()
    private val auth = MutableLiveData<Boolean>()

    init {
        appSettings.postValue(AppSettings(isDarkMode!!, isBigText!!))
        auth.postValue(isAuth!!)
    }

    fun clearAll() {
        preferences.edit().clear().apply()
    }

    fun getAppSettings(): LiveData<AppSettings> = appSettings

    fun setAppSettings(sett: AppSettings) {
        isDarkMode = sett.isDarkMode
        isBigText = sett.isBigText
        appSettings.value = sett
    }

    fun isAuth(): MutableLiveData<Boolean> = auth

    fun setAuth(auth: Boolean) {
        isAuth = auth
        this.auth.value = auth
    }

}