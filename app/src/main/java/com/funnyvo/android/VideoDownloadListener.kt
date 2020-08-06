package com.funnyvo.android

import android.net.Uri

interface VideoDownloadedListener {
    fun onDownloadCompleted(ur: Uri)
}