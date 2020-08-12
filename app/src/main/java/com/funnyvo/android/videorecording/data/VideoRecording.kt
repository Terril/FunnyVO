package com.funnyvo.android.videorecording.data

import com.google.gson.annotations.SerializedName

data class VideoRecording(val isStarted: Boolean? = false,
                          val isInProgress: Boolean? = false,
                          val hasCompleted: Boolean? = false,
                          val isCancelled: Boolean? = false,
                          val hasFailed: Boolean? = false)

data class VideoFilters(@SerializedName("msg") val msg: List<RecordingFilters> = mutableListOf())

data class RecordingFilters(@SerializedName("filter_id") val filterId: String? = "",
                   @SerializedName("name") val name: String? = "",
                   @SerializedName("description") val description: String? = "",
                   @SerializedName("main_image") val main_image: String? = "",
                   @SerializedName("image_url") val image_url: List<String>? = mutableListOf())