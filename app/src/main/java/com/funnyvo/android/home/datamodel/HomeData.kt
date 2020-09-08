package com.funnyvo.android.home.datamodel

import com.google.gson.annotations.SerializedName

data class HomeData (

		@SerializedName("id") val id : Int,
		@SerializedName("fb_id") val fb_id : Int,
		@SerializedName("user_info") val user_info : UserInfo,
		@SerializedName("count") val count : Count,
		@SerializedName("liked") val liked : Int,
		@SerializedName("video") val video : String,
		@SerializedName("thum") val thum : String,
		@SerializedName("gif") val gif : String,
		@SerializedName("description") val description : String,
		@SerializedName("sound") val sound : Sound,
		@SerializedName("created") val created : String
)