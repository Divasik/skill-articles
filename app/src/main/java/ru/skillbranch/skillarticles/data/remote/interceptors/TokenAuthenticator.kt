package ru.skillbranch.skillarticles.data.remote.interceptors

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.RefreshReq

class TokenAuthenticator : Authenticator {

    private val pref = PrefManager
    private val network by lazy { NetworkManager.api }

    override fun authenticate(route: Route?, response: Response): Request? {
        if(response.code == 401) {
            runBlocking {
                return@runBlocking try {
                    val refreshRes = network.refresh(RefreshReq(pref.refreshToken))
                    pref.accessToken = "Bearer ${refreshRes.accessToken}"
                    pref.refreshToken = refreshRes.refreshToken
                    response.request.newBuilder()
                            .header("Authorization", pref.accessToken)
                            .build()
                } catch (e: Exception) {
                    Log.d("TokenAuthenticator", e.toString())
                    null
                }
            }
        }
        return null
    }
}