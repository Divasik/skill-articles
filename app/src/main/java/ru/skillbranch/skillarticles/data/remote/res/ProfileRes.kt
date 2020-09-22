package ru.skillbranch.skillarticles.data.remote.res

import java.util.*

data class ProfileRes(
        val id: String,
        val name: String,
        val about: String,
        val avatar: String,
        val rating: Int,
        val respect: Int,
        val updatedAt: Date = Date()
)