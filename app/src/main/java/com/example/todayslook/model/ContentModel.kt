package com.example.todayslook.model

data class ContentModel(
    var explain: String? = null,    // 사진 설명
    var imageUrl: String? = null,   // 사진 다운로드 주소
    var uId: String? = null,        // 팔로잉 팔로우
    var userId: String? = null,      // email
    var timestamp: Long? = null,     // 업로드 시간
    var favoriteCount: Int = 0,      // 좋아요 카운트
    var favorites: MutableMap<String, Boolean> = HashMap()// 좋아요 적용, 취소 기능 위해서 존재
) : Comparable<ContentModel> {

    override fun compareTo(other: ContentModel): Int {
        // 내림차순으로 정렬하도록 설정
        return other.favoriteCount.compareTo(this.favoriteCount)
    }

    data class Comment(
        var uid: String? = null,
        var userId: String? = null,
        var comment: String? = null,
        var timestamp: Long? = null
    )
}