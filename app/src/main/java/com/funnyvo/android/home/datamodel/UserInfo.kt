package com.funnyvo.android.home.datamodel

import com.google.gson.annotations.SerializedName

data class UserInfo (
		@SerializedName("first_name") val first_name : String,
		@SerializedName("last_name") val last_name : String,
		@SerializedName("profile_pic") val profile_pic : String,
		@SerializedName("username") val username : String,
		@SerializedName("verified") val verified : Int
)