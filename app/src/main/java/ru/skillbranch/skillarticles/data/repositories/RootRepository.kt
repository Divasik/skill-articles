package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.LoginReq
import ru.skillbranch.skillarticles.data.remote.req.RegisterReq

object RootRepository {

    private val preferences = PrefManager
    private val network = NetworkManager.api

    fun isAuth() : LiveData<Boolean> = PrefManager.isAuthLive

    suspend fun login(login: String, password: String) {
        val auth = network.login(LoginReq(login, password))
        preferences.profile = auth.user
        preferences.accessToken = "Bearer ${auth.accessToken}"
        preferences.refreshToken = auth.refreshToken
    }

    suspend fun register(name: String, login: String, password: String) {
        val auth = network.register(RegisterReq(name, login, password))
        preferences.profile = auth.user
        preferences.accessToken = "Bearer ${auth.accessToken}"
        preferences.refreshToken = auth.refreshToken
    }
}