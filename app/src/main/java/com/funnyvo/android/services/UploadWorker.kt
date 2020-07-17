package com.funnyvo.android.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Response
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import com.funnyvo.android.R
import com.funnyvo.android.apirequest.MultipartRequest
import com.funnyvo.android.simpleclasses.ApiRequest
import com.funnyvo.android.simpleclasses.Functions
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.simpleclasses.Variables.sharedPreferences
import com.funnyvo.android.videorecording.AnimatedGifEncoder
import dagger.hilt.android.scopes.ServiceScoped
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

@ServiceScoped
class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {

    private lateinit var gifFilePath: File
    override fun doWork(): Result {

        val uri = inputData.getString("uri")
        val desc = inputData.getString("desc").orEmpty()
        // Do the work here--in this case, upload the images.
        val uriData = Uri.parse(uri)

        lateinit var result: Result
        uploadFunnyVOVideo(uriData, desc) { it -> result = it }

        // Indicate whether the work finished successfully with the Result
        return result
    }

    private fun uploadFunnyVOVideo(uri: Uri, desc: String, response: (Result) -> Unit) {
        val bmThumbnail: Bitmap = ThumbnailUtils.createVideoThumbnail(uri.path,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
        val bmThumbnailResized = Bitmap.createScaledBitmap(bmThumbnail, (bmThumbnail.width * 0.4).toInt(), (bmThumbnail.height * 0.4).toInt(), true)

        val videoFile = File(uri.path)
        val thumbNail: File = saveBitmapInFile(bmThumbnailResized)

        val mmRetriever = MediaMetadataRetriever()
        mmRetriever.setDataSource(videoFile.absolutePath)

        val mutableListFrame: MutableList<Bitmap> = mutableListOf()

        var i = 1000000
        while (i < 2000 * 1000) {
            val bitmap = mmRetriever.getFrameAtTime(i.toLong(), MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            val resized = Bitmap.createScaledBitmap(bitmap, (bitmap.width * 0.4).toInt(), (bitmap.height * 0.4).toInt(), true)
            mutableListFrame.add(resized)
            i += 100000
        }


        Base64.encodeToString(generateGIF(mutableListFrame), Base64.DEFAULT)
        // + Functions.getRandomString()

        // + Functions.getRandomString()
        val headers = mutableMapOf<String, String>()
        headers["fb_id"] = sharedPreferences.getString(Variables.u_id, "0").orEmpty()
        headers["version"] = applicationContext.resources.getString(R.string.version)
        headers["device"] = applicationContext.resources.getString(R.string.device)
        headers["tokon"] = sharedPreferences.getString(Variables.api_token, "").orEmpty()
        headers["deviceid"] = sharedPreferences.getString(Variables.device_id, "").orEmpty()

        val stringRequest = mutableMapOf<String, String>()
        stringRequest["fb_id"] = sharedPreferences.getString(Variables.u_id, "").orEmpty()
        stringRequest["sound_id"] = Variables.Selected_sound_id
        stringRequest["description"] = desc

        val fileRequest = HashMap<String, File>()
        fileRequest["video"] = videoFile
        fileRequest["thum"] = thumbNail
        fileRequest["gif"] = gifFilePath
        val multipartRequest = MultipartRequest(Variables.uploadVideo, Response.ErrorListener { error ->
//            stopForeground(true)
//            stopSelf()
//            Callback.showResponse(getString(R.string.server_error_upload_video))
            response(Result.failure())
            return@ErrorListener
        }, Response.Listener<String> { response ->
//            if (!Variables.is_secure_info) Log.d(Variables.APP_NAME, response)
//            stopForeground(true)
//            stopSelf()
//            Callback.showResponse(getString(R.string.video_uploaded_successfully))
            response(Result.success())
            return@Listener
        }, fileRequest, stringRequest, headers)
        val hurlStack: HurlStack = object : HurlStack() {
            @Throws(IOException::class)
            override fun createConnection(url: URL): HttpURLConnection {
                val httpsURLConnection = super.createConnection(url) as HttpsURLConnection
                try {
                    httpsURLConnection.sslSocketFactory = ApiRequest.getSSLSocketFactory(applicationContext)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return httpsURLConnection
            }
        }
        val request = Volley.newRequestQueue(applicationContext, hurlStack)
        request.cache.clear()
        request.add<String>(multipartRequest)
    }

    private fun generateGIF(bitmaps: MutableList<Bitmap>): ByteArray? {
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
        gifFilePath = File(Variables.app_folder, "upload" + Functions.getRandomString() + ".gif")
        val outputStream: FileOutputStream
        try {
            outputStream = FileOutputStream(gifFilePath)
            outputStream.write(bos.toByteArray())
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
        }
        return bos.toByteArray()
    }

    private fun saveBitmapInFile(bmp: Bitmap): File {
        // + Functions.getRandomString()
        val fileName = File(Variables.app_folder, "thumbnail" + Functions.getRandomString() + ".jpg")
        try {
            FileOutputStream(fileName).use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return fileName
    }
}