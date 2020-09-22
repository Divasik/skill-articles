package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import okhttp3.MultipartBody
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.models.User
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.EditProfileReq

interface IProfileRepository {
    suspend fun uploadAvatar(body: MultipartBody.Part)
    suspend fun removeAvatar()
    suspend fun editProfile(name: String, about: String)
}

object ProfileRepository: IProfileRepository {
    private val pref = PrefManager
    private val network = NetworkManager.api

    fun getProfile(): LiveData<User?> = pref.profileLive

    override suspend fun uploadAvatar(body: MultipartBody.Part) {
        val (url) = network.upload(body, pref.accessToken)
        pref.replaceAvatarUrl(url)
    }

    override suspend fun removeAvatar() {
        val (url) = network.remove(pref.accessToken)
        pref.replaceAvatarUrl(url)
    }

    override suspend fun editProfile(name: String, about: String) {
        pref.profile ?: return

        val profile = network.profile(EditProfileReq(name, about), pref.accessToken)
        pref.profile = pref.profile!!.copy(
                name = profile.name,
                about = profile.about
        )
    }
}