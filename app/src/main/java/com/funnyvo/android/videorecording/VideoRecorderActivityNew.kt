package com.funnyvo.android.videorecording

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.funnyvo.android.R
import com.funnyvo.android.accounts.LoginActivity
import com.funnyvo.android.base.BaseActivity
import com.funnyvo.android.customview.FunnyVOEditTextView
import com.funnyvo.android.extensions.filterNull
import com.funnyvo.android.filter.CameraFilter
import com.funnyvo.android.filter.CameraFilterAdapter
import com.funnyvo.android.filter.CameraFilterAdapter.OnItemClickListener
import com.funnyvo.android.helper.FileUtils
import com.funnyvo.android.helper.PermissionUtils
import com.funnyvo.android.helper.PermissionUtils.checkPermissions
import com.funnyvo.android.simpleclasses.FragmentCallback
import com.funnyvo.android.simpleclasses.Functions
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.simpleclasses.Variables.APP_NAME
import com.funnyvo.android.soundlists.SoundListMainActivity
import com.funnyvo.android.videorecording.merge.MergeVideoAudio
import com.funnyvo.android.videorecording.merge.MergeVideoAudioCallBack
import com.funnyvo.android.videorecording.viewModel.VideoRecordingViewModel
import com.lb.video_trimmer_library.interfaces.VideoTrimmingListener
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.overlay.OverlayLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_video_recoder_new.*
import java.io.File
import java.io.IOException
import java.util.*


@AndroidEntryPoint
class VideoRecorderActivityNew : BaseActivity(), OnClickListener, VideoTrimmingListener, OnDragListener, MergeVideoAudioCallBack {
    private var fileName: String = ""
    private var speedValue: String = "1.0"
    private val recordingViewModel: VideoRecordingViewModel by viewModels()

    private var isSlided = false
    private var isFilterUp = false
    private var isSpeedViewDispalyed = true
    private var isRecordingTimerEnable = false
    private var isRecording = false
    private var secondsPassed = 0
    private var recordingTime = 3
    private var audio: MediaPlayer? = null
    private var number = 0;

    private val SOUND_RECORD_CODE = 151
    private var _xDelta = 0F
    private var _yDelta = 0F

    private val arrayOfVideoPaths = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_recoder_new)

        setListeners()

        cameraRecording.setLifecycleOwner(this)
        slideLeft(layoutCameraOptions)
        initializeVideoProgress()

        loadFilters()
        observeLiveEvents()
        observeVideoRecordingEvent()

        if (intent.hasExtra("sound_name")) {
            btnAddMusicRecord.text = intent.getStringExtra("sound_name")
            Variables.Selected_sound_id = intent.getStringExtra("sound_id")
            prepareAudio()
        }

        if (checkPermissions(this)) {
            if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                openSharedVideo()
            } else {
                openLoginScreen()
            }
        }
    }

    private fun openSharedVideo() {
        val uri = onSharedIntent()
        if (uri != null) {
            try {
                val path = FileUtils(this).getPath(uri)
                if (path != null) {
                    val videoFile = File(path)
                    if (getFileDuration(uri) < 19500) {
                        recordingViewModel.changeVideoSize(path, Variables.gallery_resize_video)
                    } else {
                        try {
                            recordingViewModel.trimVideo(this, uri, this)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onSharedIntent(): Uri? {
        val receivedIntent = intent
        val receivedAction = receivedIntent.action
        val receivedType = receivedIntent.type
        if (receivedAction != null && receivedAction == Intent.ACTION_SEND) {
            // check mime type
            if (receivedType!!.startsWith("video/")) {
                //do your stuff
                return receivedIntent
                        .getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri // save to your own Uri object
            }
        }
        return null
    }

    private fun setListeners() {
        btnRotateCamera.setOnClickListener(this)
        btnFlashCamera.setOnClickListener(this)
        btnTimer.setOnClickListener(this)
        btnRecodingSpeed.setOnClickListener(this)
        //   btnFastMotion.setOnClickListener(this)
        btnFilter.setOnClickListener(this)
        btnCrop.setOnClickListener(this)
        btnRecord.setOnClickListener(this)
        imvGallery.setOnClickListener(this)
        btnCloseRecordVideo.setOnClickListener(this)
        btnDone.setOnClickListener(this)
        btnAddMusicRecord.setOnClickListener(this)
//        btnTextEditor.setOnClickListener(this)
        btn0_5x.setOnClickListener(this)
        btn0_75x.setOnClickListener(this)
        btn1x.setOnClickListener(this)
        btn1_5x.setOnClickListener(this)
        btn2x.setOnClickListener(this)
        sliderZoom.addOnChangeListener { slider, value, fromUser ->
            cameraRecording.zoom = value
        }
        toggleButton.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            // Respond to button selection
            toggleButton.isSingleSelection = true
            toggleView(checkedId)
        }
    }

    private fun toggleView(viewId: Int) {
        speedValue = when (viewId) {
            R.id.btn0_5x -> "0.5"
            R.id.btn0_75x -> "0.75"
            R.id.btn1x -> "1.0"
            R.id.btn1_5x -> "1.5"
            R.id.btn2x -> "2.0"
            else -> "1.0"
        }

    }

    private fun loadFilters() {
        val filterTypes = CameraFilter.createFilterList()
        //    val bmThumbnailResized = Bitmap.createScaledBitmap(bmThumbnail, (bmThumbnail.width * 0.4).toInt(), (bmThumbnail.height * 0.4).toInt(), true)

        val adapter = CameraFilterAdapter(this, filterTypes, object : OnItemClickListener {
            override fun onItemClick(view: View?, postion: Int, item: Filters) {
                // PreviewVideoActivity.selectPostion = postion
                cameraRecording.filter = CameraFilter.createFilter(filterTypes[postion])
            }
        })
        recylerviewFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recylerviewFilter.adapter = adapter
    }

    private fun initializeVideoProgress() {
        videoProgress.enableAutoProgressView(Variables.recording_duration.toLong())
        videoProgress.setDividerColor(Color.WHITE)
        videoProgress.setDividerEnabled(true)
        videoProgress.setDividerWidth(4f)
        videoProgress.setShader(intArrayOf(Color.CYAN, Color.CYAN, Color.CYAN))
        videoProgress.setListener { mills ->
            secondsPassed = (mills / 1000).toInt()
            if (secondsPassed > Variables.recording_duration / 1000 - 1) {
                startOrStopRecording()
            }
            if (isRecordingTimerEnable && secondsPassed >= recordingTime) {
                isRecordingTimerEnable = false
                startOrStopRecording()
            }
        }
    }

    private fun startOrStopRecording() {
        sliderZoom.visibility = VISIBLE
        layoutVideoSpeed.visibility = INVISIBLE
        if (!isRecording && secondsPassed < Variables.recording_duration / 1000 - 1) {
            isRecording = true
            number += 1
            fileName = if (speedValue != "1.0") {
                Variables.APP_FOLDER + number + ".mp4"
            } else {
                Variables.APP_FOLDER + "myvideo" + number + ".mp4"
            }

            val file = File(fileName)
            arrayOfVideoPaths.add(fileName)
            cameraRecording.takeVideoSnapshot(file)
            //   cameraRecording.captureVideo(file)
            if (audio != null) audio?.start()
            videoProgress.resume()
            btnDone.isEnabled = false
            btnRecord.setImageDrawable(resources.getDrawable(R.drawable.ic_record_video_post))
            slideCameraOptions()
            //  btnAddMusicRecord.isClickable = false
            // cameraRecording.open()
        } else if (isRecording) {
            isRecording = false
            videoProgress.pause()
            videoProgress.addDivider()
            if (speedValue != "1.0") {
                showProgressDialog()
                number += 1
                recordingViewModel.applyFastMoSlowMoVideo(fileName, Variables.APP_FOLDER + number + ".mp4", speedValue.toFloat())
            }
            if (audio != null) audio?.pause()
            if (secondsPassed > Variables.recording_duration / 1000 / 4) {
                btnDone.visibility = VISIBLE
                btnDone.isEnabled = true
                btnDone.setOnClickListener(this)
            }
            cameraRecording.stopVideo()
            btnRecord.setImageDrawable(resources.getDrawable(R.drawable.ic_record_video_pre))
            slideCameraOptions()
            //            rotate_camera.setVisibility(View.VISIBLE);
        } else if (secondsPassed > Variables.recording_duration / 1000) {
            Functions.showAlert(this, "Alert", "Video only can be a " + Variables.recording_duration / 1000 + " S")
        }
    }

    private fun prepareAudio() {
        val file = File(Variables.APP_FOLDER + Variables.SELECTED_AUDIO_AAC)
        if (file.exists()) {
            audio = MediaPlayer()
            try {
                audio?.setDataSource(Variables.APP_FOLDER + Variables.SELECTED_AUDIO_AAC)
                audio?.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(this, Uri.fromFile(file))
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val fileDuration = durationStr.toInt()
            if (fileDuration < Variables.max_recording_duration) {
                Variables.recording_duration = fileDuration
                initializeVideoProgress()
            }
        }
    }

    private fun loadTimerViewForRecording() {
        if (secondsPassed + 1 < Variables.recording_duration / 1000) {
            val recordingTimeRangeFragment = RecordingTimeRangeFragment(FragmentCallback { bundle ->
                if (bundle != null) {
                    isRecordingTimerEnable = true
                    recordingTime = bundle.getInt("end_time")
                    txtCountdownTimer.text = "3"
                    txtCountdownTimer.visibility = View.VISIBLE
                    btnRecord.isClickable = false
                    val scaleAnimation: Animation = ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    object : CountDownTimer(4000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            txtCountdownTimer.text = "" + millisUntilFinished / 1000
                            txtCountdownTimer.animation = scaleAnimation
                        }

                        override fun onFinish() {
                            btnRecord.isClickable = true
                            txtCountdownTimer.visibility = View.GONE
                            startOrStopRecording()
                        }
                    }.start()
                }
            })
            val bundle = Bundle()
            if (secondsPassed < Variables.recording_duration / 1000 - 3) bundle.putInt("end_time", secondsPassed + 3) else bundle.putInt("end_time", secondsPassed + 1)
            bundle.putInt("total_time", Variables.recording_duration / 1000)
            recordingTimeRangeFragment.arguments = bundle
            recordingTimeRangeFragment.show(supportFragmentManager, "")
        }
    }

    private fun closeVideoView() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.are_you_sure))
                .setMessage(getString(R.string.cant_undo))
                .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    dialog.dismiss()
                    recordingViewModel.deleteFile()
                    finish()
                    overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom)
                }.show()
    }

    private fun observeLiveEvents() {
        recordingViewModel.motionFilter.observe(this) {
            dismissProgressDialog()
            if(it.filterNull()) {
                arrayOfVideoPaths.add(Variables.APP_FOLDER + number + ".mp4")
            }
            Log.e(APP_NAME, "MotionFilter is : " + it.filterNull())
        }

        recordingViewModel.videoAppendEvent.observe(this) {
            if (it.filterNull()) {
                if (audio != null) mergeWithAudio() else {
                    goToPreviewActivity()
                }
            }
            dismissProgressDialog()
        }
    }

    private fun observeVideoRecordingEvent() {
        recordingViewModel.videoRecordingLiveEvent.observe(this) {
            when {
                it.isInProgress.filterNull() -> {
                    showProgressDialog()
                }
                it.hasCompleted.filterNull() -> {
                    dismissProgressDialog()
                    val intent = Intent(this, PreviewVideoActivity::class.java)
                    intent.putExtra("video_path", Variables.gallery_resize_video)
                    intent.putExtra("isFromGallery", true)
                    startActivity(intent)
                }
                it.hasFailed.filterNull() -> {
                    dismissProgressDialog()
                    Toast.makeText(this, R.string.try_again, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startWritingOnVideo() {
        edtTxtVideoMessage.visibility = View.VISIBLE
        edtTxtVideoMessage.requestFocus()
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edtTxtVideoMessage, InputMethodManager.SHOW_IMPLICIT)
        edtTxtVideoMessage.setOnDragListener(this@VideoRecorderActivityNew)
//        edtTxtVideoMessage.addTextChangedListener(object: TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//        })

        edtTxtVideoMessage.setOnLongClickListener { v ->
            val item: ClipData.Item = ClipData.Item(v.tag as CharSequence)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val dragData = ClipData(v.tag.toString(), mimeTypes, item)
            val myShadow = DragShadowBuilder(edtTxtVideoMessage)
            v.startDrag(dragData, myShadow, null, 0)
            true
        }

        edtTxtVideoMessage.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action === MotionEvent.ACTION_DOWN) {
                val data = ClipData.newPlainText("", "")
                val shadowBuilder = DragShadowBuilder(edtTxtVideoMessage)
                edtTxtVideoMessage.startDrag(data, shadowBuilder, edtTxtVideoMessage, 0)
                edtTxtVideoMessage.visibility = View.INVISIBLE
                true
            } else {
                false
            }
        })
    }

    private fun slideUp(view: View) {
        view.visibility = VISIBLE
        val animate = TranslateAnimation(
                0.0F,  // fromXDelta
                0.0F,  // toXDelta
                view.height.toFloat(),  // fromYDelta
                0.0F) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
        view.isClickable = true
    }

    // slide the view from its current position to below itself
    private fun slideDown(view: View) {
        val animate = TranslateAnimation(
                0.0F,  // fromXDelta
                0.0F,  // toXDelta
                0.0F,  // fromYDelta
                view.height.toFloat()) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
        view.isClickable = false
    }

    private fun slideLeft(view: View) {
        view.visibility = View.VISIBLE
        val animate = TranslateAnimation(
                view.width.toFloat(),  // fromXDelta
                0.0F,  // toXDelta
                0.0F,  // fromYDelta
                0.0F) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    // slide the view from its current position to below itself
    private fun slideRight(view: View) {
        val animate = TranslateAnimation(
                0.0F, // fromXDelta
                view.width.toFloat(),  // toXDelta
                0.0F,  // fromYDelta
                0.0F) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        startActivityForResult(intent, Variables.PICK_VIDEO_FROM_GALLERY)
    }

    private fun slideCameraOptions() {
        if (isSlided) {
            slideLeft(layoutCameraOptions)
        } else {
            slideRight(layoutCameraOptions)
        }
        isSlided = !isSlided
    }

    private fun slideFilterView() {
        if (isFilterUp) {
            slideDown(recylerviewFilter)
        } else {
            slideUp(recylerviewFilter)
        }
        isFilterUp = !isFilterUp
    }

    private fun toggleSpeedView() {
        if (isSpeedViewDispalyed) {
            layoutVideoSpeed.visibility = VISIBLE
        } else {
            layoutVideoSpeed.visibility = INVISIBLE
        }
        isSpeedViewDispalyed = !isSpeedViewDispalyed
    }

    override fun onClick(v: View?) {
        when (v) {
            btnRecord -> {
                imvCollage.visibility = GONE
                imvGallery.visibility = GONE
                startOrStopRecording()
            }
            imvGallery -> {
                if (checkPermissions(this)) {
                    if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                        pickVideoFromGallery()
                    } else {
                        openLoginScreen()
                    }
                }
            }
            btnAddMusicRecord -> {
                val intent = Intent(this, SoundListMainActivity::class.java)
                startActivityForResult(intent, SOUND_RECORD_CODE)
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
            }
            btnTimer -> {
                loadTimerViewForRecording()
            }
            btnFilter -> {
                slideFilterView()
            }
            btnCloseRecordVideo ->
                closeVideoView()
            btnRotateCamera ->
                cameraRecording.toggleFacing()
            btnDone -> {
                var outputFilePath: String? = null
                outputFilePath = if (audio != null) {
                    Variables.outputfile
                } else {
                    Variables.outputfile2
                }
                showProgressDialog()
                recordingViewModel.appendTheContent(this, arrayOfVideoPaths, outputFilePath)
            }
            btnRecodingSpeed -> toggleSpeedView()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SOUND_RECORD_CODE) {
                if (data != null) {
                    if (data.getStringExtra("isSelected") == getString(R.string.yes)) {
                        btnAddMusicRecord.text = data.getStringExtra("sound_name")
                        Variables.Selected_sound_id = data.getStringExtra("sound_id")
                        prepareAudio()
                    }
                }
            } else if (requestCode == Variables.PICK_VIDEO_FROM_GALLERY) {
                val uri: Uri = data?.data!!
                try {
                    val path = FileUtils(this).getPath(uri)!!
                    val videoFile = File(path)
                    if (getFileDuration(uri) < 19500) {
                        recordingViewModel.changeVideoSize(videoFile.absolutePath, Variables.gallery_resize_video)
                    } else {
                        try {
                            recordingViewModel.trimVideo(this, uri, this)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun mergeWithAudio(): Unit {
        val audioFile = Variables.APP_FOLDER + Variables.SELECTED_AUDIO_AAC
        val mergeVideoAudio = MergeVideoAudio(this)
        mergeVideoAudio.doInBackground(audioFile, Variables.outputfile, Variables.outputfile2)
    }

    private fun getFileDuration(uri: Uri): Long {
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(this, uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val fileDuration = durationStr.toInt()
            return fileDuration.toLong()
        } catch (e: java.lang.Exception) {
        }
        return 0
    }

    private fun goToPreviewActivity() {
        val intent = Intent(this, PreviewVideoActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openLoginScreen() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    override fun onResume() {
        super.onResume()
        cameraRecording.open()
    }

    override fun onPause() {
        super.onPause()
        cameraRecording.close()
        sliderZoom.visibility = INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraRecording.destroy()
    }

    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        Toast.makeText(this, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
    }

    override fun onFinishedTrimming(uri: Uri?) {
        recordingViewModel.changeVideoSize(Variables.gallery_trimed_video, Variables.gallery_resize_video)
    }

    override fun onTrimStarted() {
        showProgressDialog()
    }

    override fun onVideoPrepared() {
        cameraRecording.overlay
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        when (event!!.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
            }
            DragEvent.ACTION_DRAG_EXITED -> {
            }
            DragEvent.ACTION_DROP -> {
                // Dropped, reassign View to ViewGroup
                val view = event.localState as FunnyVOEditTextView
                val owner: ViewGroup = view.parent as ViewGroup
                owner.removeView(view)
                val container: OverlayLayout = v as OverlayLayout
                container.addView(view)
                view.visibility = View.VISIBLE
            }
            DragEvent.ACTION_DRAG_ENDED -> {
            }
            else -> {
            }
        }
        return true
    }

    override fun onCompletion(state: Boolean, draftFile: String?) {
        val intent = Intent(this, PreviewVideoActivity::class.java)
        intent.putExtra("video_path", Variables.outputfile2)
        intent.putExtra("draft_file", draftFile)
        startActivity(intent)
    }

}
