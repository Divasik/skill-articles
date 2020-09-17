package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.models.User

object ProfileRepository {
    private val pref = PrefManager

    fun getProfile(): LiveData<User?> = pref.profileLive
}