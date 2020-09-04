package com.funnyvo.android.videorecording

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.work.*
import com.funnyvo.android.R
import com.funnyvo.android.base.BaseActivity
import com.funnyvo.android.helper.PlayerEventListener
import com.funnyvo.android.main_menu.MainMenuActivity
import com.funnyvo.android.services.UploadWorker
import com.funnyvo.android.services.UploadWorker.Companion.Progress
import com.funnyvo.android.simpleclasses.Functions
import com.funnyvo.android.simpleclasses.Variables
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_post_video.*
import java.io.*


class PostVideoActivity : BaseActivity(), View.OnClickListener {
    private var video_path: String? = null
    private var descriptionEdit: EditText? = null
    private var draft_file: String? = null
    private var eventListener: PlayerEventListener? = null
    private var gifFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideNavigation()
        setContentView(R.layout.activity_post_video)
        var isScaleModeSet = true
        val intent = intent
        if (intent != null) {
            draft_file = intent.getStringExtra("draft_file")
            isScaleModeSet = intent.getBooleanExtra("isScaleMode", true)
        }
        eventListener = PlayerEventListener()
        video_path = Variables.GALLERY_TRIMMED_VIDEO
        val gpuPlayerView = setPlayer(this, Uri.parse(video_path), eventListener!!, isScaleModeSet)
        (findViewById<View>(R.id.layout_post_movie_wrapper) as MovieWrapperView).addView(gpuPlayerView)
        gpuPlayerView.onResume()
        findViewById<View>(R.id.btnGoBackPost).setOnClickListener { onBackPressed() }
        val btnUploadVideo = findViewById<FloatingActionButton>(R.id.btnUploadVideo)
        btnUploadVideo.setOnClickListener {
            btnUploadVideo.isEnabled = false
            // showProgressDialog();
            startService()
        }
        findViewById<View>(R.id.btnSaveLocal).setOnClickListener(this)
        descriptionEdit = findViewById(R.id.edtDescriptionAndHashTags)
        gifFile = createGifFile()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSaveLocal -> saveFileInDraft()
        }
    }

    // this will start the service for uploading the video into database
    private fun startService() {
        val uploadWork = OneTimeWorkRequest.Builder(UploadWorker::class.java)
        val data = Data.Builder()
        //Add parameter in Data class. just like bundle. You can also add Boolean and Number in parameter.
        data.putString("uri", "" + Uri.fromFile(File(video_path)))
        data.putString("uriGif", "" + Uri.fromFile(gifFile))
        data.putString("desc", descriptionEdit!!.text.toString())
        //Set Input Data
        uploadWork.setInputData(data.build())
        val uploadWorkRequest: WorkRequest = uploadWork.build()
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(uploadWorkRequest)
        workManager.getWorkInfoByIdLiveData(uploadWorkRequest.id)
                .observe(this, Observer { workInfo: WorkInfo? ->
                    if (workInfo != null) {
                        val progress = workInfo.progress
                        val value = progress.getInt(Progress, 0)
                        progressBar.visibility = VISIBLE
                        progressBar.progress = value
                        // Do something with progress information
                    }
                })

        Toast.makeText(this@PostVideoActivity, R.string.continue_using_app, Toast.LENGTH_LONG).show()
        if (player != null) {
            player.removeListener(eventListener!!)
            player.release()
        }
        deleteDraftFile()
        val intent = Intent(this@PostVideoActivity, MainMenuActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun createGifFile(): File {
        val mmRetriever = MediaMetadataRetriever()
        mmRetriever.setDataSource(video_path)
        val frames = ArrayList<Bitmap>()
        var i = 1000000
        while (i < 2000 * 1000) {
            val bitmap = mmRetriever.getFrameAtTime(i.toLong(), MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            val resized = bitmap?.let { Bitmap.createScaledBitmap(it, (bitmap.width * 0.4).toInt(), (bitmap.height * 0.4).toInt(), true) }
            resized?.let { frames.add(it) }
            i += 100000
        }

        // Base64.encodeToString(), Base64.DEFAULT);
        return generateGIF(frames)
    }

    private fun generateGIF(bitmaps: ArrayList<Bitmap>): File {
        val bos = ByteArrayOutputStream()
        val encoder = AnimatedGifEncoder()
        encoder.start(bos)
        for (bitmap in bitmaps) {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, out)
            val decoded = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
            encoder.addFrame(decoded)
        }
        encoder.finish()
        val gifFilePath = File(Variables.APP_FOLDER, "upload" + Functions.getRandomString() + ".gif")
        val outputStream: FileOutputStream
        try {
            outputStream = FileOutputStream(gifFilePath)
            outputStream.write(bos.toByteArray())
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
        }
        return gifFilePath
    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            player.playWhenReady = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (player != null) {
            player.playWhenReady = true
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (player != null) {
            player.playWhenReady = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player.removeListener(eventListener!!)
            player.release()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        deleteGifFile()
        finish()
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right)
    }

    // when the video is uploading successfully it will restart the application
    private fun saveFileInDraft() {
        val source = File(video_path)
        val destination = File(Variables.draft_app_folder + Functions.getRandomString() + ".mp4")
        try {
            if (source.exists()) {
                val inputStream: InputStream = FileInputStream(source)
                val out: OutputStream = FileOutputStream(destination)
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                inputStream.close()
                out.close()
                Toast.makeText(this@PostVideoActivity, R.string.file_saved_in_draft, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@PostVideoActivity, MainMenuActivity::class.java))
            } else {
                Toast.makeText(this@PostVideoActivity, R.string.save_failed_into_draft, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun deleteDraftFile() {
        if (draft_file != null) {
            val file = File(draft_file)
            file.delete()
        }
    }

    private fun deleteGifFile() {
        try {
            if (gifFile != null) {
                gifFile!!.delete()
            }
        } catch (e: Exception) {
        }
    }
}