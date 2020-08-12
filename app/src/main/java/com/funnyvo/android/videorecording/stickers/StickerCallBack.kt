package com.funnyvo.android.videorecording.stickers

import com.funnyvo.android.videorecording.data.RecordingFilters

interface StickerCallBack {

    fun onStickerClicked(filter: RecordingFilters)
}