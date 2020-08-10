package com.funnyvo.android.videorecording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.funnyvo.android.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShowStickerFragment : BottomSheetDialogFragment() {
    private object HOLDER {
        val INSTANCE = ShowStickerFragment()
    }

    companion object {
        val instance: ShowStickerFragment by lazy { HOLDER.INSTANCE }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_stickers_bottom_sheet, container, false)
    }
}