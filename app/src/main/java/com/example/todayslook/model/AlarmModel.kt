package com.example.todayslook.model

data class AlarmModel(
    var destinationUid: String? = null,
    var userId: String? = null,
    var uid: String? = null,
    var kind: Int? = null, // 0 좋아요 1 코멘트 2 팔로우
    var message: String? = null,
    var timestamp: Long? = null,
    var userUid: String? = null
)