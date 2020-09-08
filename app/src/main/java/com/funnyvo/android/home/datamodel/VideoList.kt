package com.funnyvo.android.home.datamodel

import com.google.gson.annotations.SerializedName

data class VideoList (

		@SerializedName("code") val code : Int,
		@SerializedName("msg") val msg : List<HomeData>
)