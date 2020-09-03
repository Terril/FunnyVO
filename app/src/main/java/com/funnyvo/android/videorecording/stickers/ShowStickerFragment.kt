package com.funnyvo.android.videorecording.stickers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.funnyvo.android.R
// import com.funnyvo.android.videorecording.data.RecordingFilters
// import com.funnyvo.android.videorecording.stickers.StickerAdapter.OnItemClickListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_stickers_bottom_sheet.*

class ShowStickerFragment : BottomSheetDialogFragment() {

   // private var recordingFilters: List<RecordingFilters> = mutableListOf()
//    private lateinit var callBack: StickerCallBack

    private object HOLDER {
        val INSTANCE = ShowStickerFragment()
    }

    companion object {
        val instance: ShowStickerFragment by lazy { HOLDER.INSTANCE }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_stickers_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = GridLayoutManager(context, 6)
        recyclerViewStickers.layoutManager = layoutManager
        recyclerViewStickers.setHasFixedSize(false)
//        val adapter = StickerAdapter(recordingFilters, object : OnItemClickListener {
//            override fun onItemClick(item: RecordingFilters?) {
//                if (item != null) {
//                    callBack.onStickerClicked(item)
//                }
//                dismissAllowingStateLoss()
//            }
//        })
//        recyclerViewStickers.adapter = adapter
    }

//    fun set(filters: List<RecordingFilters>, stickerCallBack: StickerCallBack) {
//        if (filters.isNotEmpty()) {
//            recordingFilters = filters
//        }
//        callBack = stickerCallBack
//    }
}