package ru.skillbranch.skillarticles.data.remote.req

data class LoginReq(val login:String, val password:String)

data class RefreshReq(val refreshToken:String)