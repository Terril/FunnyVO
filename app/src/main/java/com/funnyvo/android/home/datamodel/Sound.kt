package com.funnyvo.android.home.datamodel

import com.google.gson.annotations.SerializedName

data class Sound (

        @SerializedName("id") val id : Int,
        @SerializedName("audio_path") val audio_path : AudioPath,
        @SerializedName("sound_name") val sound_name : String,
        @SerializedName("sound_url") val sound_url : String,
        @SerializedName("description") val description : String,
        @SerializedName("thum") val thum : String,
        @SerializedName("section") val section : Int,
        @SerializedName("created") val created : String
)