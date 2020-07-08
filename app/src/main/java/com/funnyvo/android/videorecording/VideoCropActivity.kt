package com.funnyvo.android.videorecording

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.funnyvo.android.R
import com.funnyvo.android.base.BaseActivity
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.simpleclasses.Variables.app_folder
import com.lb.video_trimmer_library.interfaces.VideoTrimmingListener
import kotlinx.android.synthetic.main.activity_video_crop.*
import java.io.File


class VideoCropActivity : BaseActivity(), VideoTrimmingListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_crop)
        val inputVideoUri: Uri? = Uri.parse(Variables.outputfile2)
        if (inputVideoUri == null) {
            finish()
            return
        }
        videoTrimmerView.setMaxDurationInMs(15 * 1000)
        videoTrimmerView.setOnK4LVideoListener(this)
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
        val trimmedVideoFile = File(app_folder, fileName)
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
//            val msg = getString(R.string.video_saved_at, uri.path)
//            Toast.makeText(this@TrimmerActivity, msg, Toast.LENGTH_SHORT).show()
//            val intent = Intent(Intent.ACTION_VIEW, uri)
//            intent.setDataAndType(uri, "video/mp4")
//            startActivity(intent)
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