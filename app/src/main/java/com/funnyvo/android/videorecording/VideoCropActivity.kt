package com.funnyvo.android.videorecording

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.funnyvo.android.R
import com.funnyvo.android.base.BaseActivity
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.simpleclasses.Variables.OUTPUT_FILE_TRIMMED
import com.lb.video_trimmer_library.interfaces.VideoTrimmingListener
import kotlinx.android.synthetic.main.activity_video_crop.*
import java.io.File


class VideoCropActivity : BaseActivity(), VideoTrimmingListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_crop)
        val inputVideoUri: Uri? = Uri.fromFile(File(Variables.outputfile2))
        if (inputVideoUri == null) {
            finish()
            return
        }
        videoTrimmerView.setMaxDurationInMs(15 * 1000)
        videoTrimmerView.setOnK4LVideoListener(this)
        val trimmedVideoFile = File(OUTPUT_FILE_TRIMMED)
        videoTrimmerView.setDestinationFile(trimmedVideoFile)
        videoTrimmerView?.setVideoURI(inputVideoUri)
        videoTrimmerView.setVideoInformationVisibility(true)

        btnBackCrop.setOnClickListener {
            finish()
        }
    }

    override fun onTrimStarted() {
        showProgressDialog()
    }

    override fun onFinishedTrimming(uri: Uri?) {
        dismissProgressDialog()
        if (uri == null) {
            Toast.makeText(this@VideoCropActivity, getString(R.string.failed_trimming), Toast.LENGTH_SHORT).show()
        } else {
            val returnIntent = Intent()
            returnIntent.putExtra("result", OUTPUT_FILE_TRIMMED);
            setResult(Activity.RESULT_OK, returnIntent)
        }

        finish()
    }

    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        dismissProgressDialog()
        Toast.makeText(this@VideoCropActivity, getString(R.string.error_view_trim_video), Toast.LENGTH_SHORT).show()
    }

    override fun onVideoPrepared() {

    }
}