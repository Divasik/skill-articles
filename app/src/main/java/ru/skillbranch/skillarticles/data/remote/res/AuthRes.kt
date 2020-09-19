package ru.skillbranch.skillarticles.data.remote.res

import ru.skillbranch.skillarticles.data.models.User

data class AuthRes(
    val user: User,
    val refreshToken:String,
    val accessToken:String
)

data class RefreshRes(
    val refreshToken:String,
    val accessToken:String
)