package com.funnyvo.android.home.datamodel

import com.google.gson.annotations.SerializedName

data class Count (

		@SerializedName("like_count") val like_count : Int,
		@SerializedName("video_comment_count") val video_comment_count : Int
)