package ru.irinavb.whatsupclone.util

data class User(
    val email: String? = "",
    val phone: String? = "",
    val name: String? = "",
    val imageUrl: String? = "",
    val status: String? = "",
    val statusUrl: String? = "",
    val statusTime: String? = ""
)

data class Contact (
    val name: String?,
    val phone: String?
)

data class Chat(
    val chatParticipants: ArrayList<String>
)