package com.funnyvo.android.home.datamodel

import com.google.gson.annotations.SerializedName

data class AudioPath (

		@SerializedName("mp3") val mp3 : String,
		@SerializedName("acc") val acc : String
)