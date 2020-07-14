package com.funnyvo.android.videorecording.data

data class VideoRecording(val isStarted: Boolean? = false,
                          val isInProgress: Boolean? = false,
                          val hasCompleted: Boolean? = false,
                          val isCancelled: Boolean? = false,
                          val hasFailed: Boolean? = false)