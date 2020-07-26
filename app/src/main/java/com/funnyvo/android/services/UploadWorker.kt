package com.funnyvo.android.services

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
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
import com.funnyvo.android.simpleclasses.Variables.APP_NAME
import com.funnyvo.android.simpleclasses.Variables.sharedPreferences
import dagger.hilt.android.scopes.ServiceScoped
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

@ServiceScoped
class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {

    override fun doWork(): Result {

        val uri = inputData.getString("uri")
        val uriGif = inputData.getString("uriGif")
        val desc = inputData.getString("desc").orEmpty()

        // Do the work here--in this case, upload the images.
        val uriData = Uri.parse(uri)
        val uriGifData = Uri.parse(uriGif)

        uploadFunnyVOVideo(uriData, uriGifData, desc)

        return Result.success()
    }

    private fun uploadFunnyVOVideo(uri: Uri, uriGif: Uri, desc: String) {
        val bmThumbnail: Bitmap = ThumbnailUtils.createVideoThumbnail(uri.path,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
        val bmThumbnailResized = Bitmap.createScaledBitmap(bmThumbnail, (bmThumbnail.width * 0.4).toInt(), (bmThumbnail.height * 0.4).toInt(), true)

        val videoFile = File(uri.path)
        val gifFile = File(uriGif.path)
        val thumbNail: File = saveBitmapInFile(bmThumbnailResized)

        val headers = mutableMapOf<String, String>()
        headers["fb_id"] = sharedPreferences.getString(Variables.u_id, "0").orEmpty()
        headers["version"] = applicationContext.resources.getString(R.string.version)
        headers["device"] = applicationContext.resources.getString(R.string.device)
        headers["tokon"] = sharedPreferences.getString(Variables.api_token, "").orEmpty()
        headers["deviceid"] = sharedPreferences.getString(Variables.device_id, "").orEmpty()

        val stringRequest = mutableMapOf<String, String>()
        stringRequest["fb_id"] = sharedPreferences.getString(Variables.u_id, "").orEmpty()
        stringRequest["sound_id"] = Variables.Selected_sound_id.orEmpty()
        stringRequest["description"] = desc
        stringRequest["upload_video"] = "public"

        val fileRequest = HashMap<String, File>()
        fileRequest["video"] = videoFile
        fileRequest["thum"] = thumbNail
        fileRequest["gif"] = gifFile
        val multipartRequest = MultipartRequest(Variables.UPLOAD_VIDEO, Response.ErrorListener { error ->
            Log.e(APP_NAME, "Video Upload Error : " + error.message)
            videoFile.deleteOnExit()
            gifFile.deleteOnExit()
            thumbNail.deleteOnExit()

            return@ErrorListener
        }, Response.Listener<String> { response ->
            Log.e(APP_NAME, "Video Upload Success : $response")
            videoFile.deleteOnExit()
            gifFile.deleteOnExit()
            thumbNail.deleteOnExit()
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

    private fun deleteFiles() {

    }
}