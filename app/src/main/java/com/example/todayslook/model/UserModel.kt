package com.example.todayslook.model

import java.io.Serializable

data class UserModel(
    val id: String? = null,
    val phoneNumber: String? = null,
    val uid: String? = null,
    val username : String? =""

) : Serializable