package com.funnyvo.android.videorecording.phototemplate

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.funnyvo.android.R
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import kotlinx.android.synthetic.main.fragment_photo_template.*
import kotlinx.android.synthetic.main.item_chat_alert.*


class PhotoTemplateFragment(private val message: String, private val maxPhotoAllowed: Int) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo_template, null, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtTemplateMessage.text = message
        btnSelectPhoto.setOnClickListener {
            activity?.let { it1 ->
                FishBun.with(it1).setImageAdapter(GlideAdapter()).setMaxCount(maxPhotoAllowed)
                        .setMinCount(maxPhotoAllowed)
                        .setPickerSpanCount(4)
                        .setActionBarColor(Color.parseColor("#ffffff"), Color.parseColor("#fd771c"), true)
                        .setActionBarTitleColor(Color.parseColor("#FDCA1B")).setActionBarTitle(getString(R.string.image_library))
                        .setDoneButtonDrawable(context?.let { it2 -> ContextCompat.getDrawable(it2, R.drawable.ic_done) })
                        .setHomeAsUpIndicatorDrawable(context?.let { it2 -> ContextCompat.getDrawable(it2, R.drawable.ic_baseline_arrow_back) })
                        .textOnImagesSelectionLimitReached(getString(R.string.limit_reached)).startAlbum()
            }
        }
    }

}