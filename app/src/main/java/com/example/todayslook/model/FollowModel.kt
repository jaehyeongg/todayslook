package com.example.todayslook.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FollowModel(
    var followerCount : Int = 0,
    var followers : MutableMap<String,String> = hashMapOf(),


    var followingCount : Int = 0,
    var followings : MutableMap<String,String> = hashMapOf()

) : Parcelable