package com.funnyvo.android.videorecording.phototemplate

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.funnyvo.android.R
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.FishBun.Companion.INTENT_PATH
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import kotlinx.android.synthetic.main.fragment_photo_template.*


class PhotoTemplateFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo_template, null, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSelectPhoto.setOnClickListener {
            activity?.let { it1 ->
                FishBun.with(it1).setImageAdapter(GlideAdapter()).setMaxCount(5)
                        .setMinCount(1)
                        .setPickerSpanCount(4)
                        .setActionBarColor(Color.parseColor("#ffffff"), Color.parseColor("#fd771c"), true)
                        .setActionBarTitleColor(Color.parseColor("#FDCA1B")).setActionBarTitle(getString(R.string.image_library))
                        .setDoneButtonDrawable(context?.let { it2 -> ContextCompat.getDrawable(it2, R.drawable.ic_done) })
                        .textOnImagesSelectionLimitReached(getString(R.string.limit_reached)).startAlbum()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FishBun.FISHBUN_REQUEST_CODE -> if (resultCode == RESULT_OK) {
                val path = data?.getParcelableArrayListExtra<Uri>(INTENT_PATH)
            }
        }
    }
}