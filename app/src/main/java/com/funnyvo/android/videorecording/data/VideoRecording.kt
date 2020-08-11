package com.funnyvo.android.videorecording.data

data class VideoRecording(val isStarted: Boolean? = false,
                          val isInProgress: Boolean? = false,
                          val hasCompleted: Boolean? = false,
                          val isCancelled: Boolean? = false,
                          val hasFailed: Boolean? = false)

data class VideoFilters(val msg: List<Filters> = mutableListOf())

data class Filters(val filter_id: String? = "",
                   val name: String? = "",
                   val description: String? = "",
                   val main_image: String? = "",
                   val image_url: List<String>? = mutableListOf())