package com.funnyvo.android.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.android.volley.Response
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import com.funnyvo.android.BuildConfig
import com.funnyvo.android.R
import com.funnyvo.android.apirequest.MultipartRequest
import com.funnyvo.android.simpleclasses.ApiRequest
import com.funnyvo.android.simpleclasses.Functions
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.simpleclasses.Variables.APP_NAME
import com.funnyvo.android.simpleclasses.Variables.sharedPreferences
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

@ServiceScoped
class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
        CoroutineWorker(appContext, workerParams) {

    @RequiresApi(Build.VERSION_CODES.M)
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)

    companion object {
        const val Progress = "Progress"
        private const val delayDuration = 100L
        const val NOTIFICATION_ID = 42
        const val TAG = "ForegroundWorker"
        const val channelId = "Job progress"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun doWork(): Result {

        val uri = inputData.getString("uri")
        val uriGif = inputData.getString("uriGif")
        val desc = inputData.getString("desc").orEmpty()

        // Do the work here--in this case, upload the images.
        val uriData = Uri.parse(uri)
        val uriGifData = Uri.parse(uriGif)

        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Uploading video to FunnyVO")
                .build()

        val foregroundInfo = ForegroundInfo(NOTIFICATION_ID, notification)
        setForeground(foregroundInfo)

        for (i in 0..100) {
            setProgress(workDataOf(Progress to i))
            showProgress(i)
            delay(delayDuration)
        }

        uploadFunnyVOVideo(uriData, uriGifData, desc)

        return Result.success()
    }

    private fun uploadFunnyVOVideo(uri: Uri, uriGif: Uri, desc: String) {
        val bmThumbnail: Bitmap = uri.path?.let {
            ThumbnailUtils.createVideoThumbnail(it,
                    MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
        }!!
        val bmThumbnailResized = Bitmap.createScaledBitmap(bmThumbnail, (bmThumbnail.width * 0.4).toInt(), (bmThumbnail.height * 0.4).toInt(), true)

        //  Log.d(APP_NAME, "Video path :" + uri.path)
        val videoFile = File(uri.path)
        val gifFile = File(uriGif.path)
        val thumbNail: File = saveBitmapInFile(bmThumbnailResized)

        val headers = mutableMapOf<String, String>()
        headers["fb_id"] = sharedPreferences.getString(Variables.u_id, "0").orEmpty()
        headers["version"] = BuildConfig.VERSION_NAME
        headers["device"] = applicationContext.resources.getString(R.string.device)
        headers["tokon"] = sharedPreferences.getString(Variables.api_token, "").orEmpty()
        headers["deviceid"] = sharedPreferences.getString(Variables.device_id, "").orEmpty()

        val stringRequest = mutableMapOf<String, String>()
        stringRequest["fb_id"] = sharedPreferences.getString(Variables.u_id, "").orEmpty()
        stringRequest["sound_id"] = Variables.Selected_sound_id.orEmpty()
        stringRequest["description"] = desc
        stringRequest["visible_for"] = sharedPreferences.getString(Variables.USER_PREF_VIDEO_VISIBILITY, "everyone").orEmpty().toLowerCase()

        val fileRequest = HashMap<String, File>()
        fileRequest["video"] = videoFile
        fileRequest["thum"] = thumbNail
        fileRequest["gif"] = gifFile
        val multipartRequest = MultipartRequest(Variables.UPLOAD_VIDEO, Response.ErrorListener { error ->
            //  Log.e(APP_NAME, "Video Upload Error : " + error.message)
            videoFile.deleteOnExit()
            gifFile.deleteOnExit()
            thumbNail.deleteOnExit()

            return@ErrorListener
        }, Response.Listener<String> { response ->
            //  Log.e(APP_NAME, "Video Upload Success : $response")
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
        val fileName = File(Variables.APP_FOLDER, "thumbnail" + Functions.getRandomString() + ".jpg")
        try {
            FileOutputStream(fileName).use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return fileName
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showProgress(progress: Int) {
        val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Uploading video to FunnyVO")
                .setProgress(100, progress, false)
                .build()
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel =
                    notificationManager?.getNotificationChannel(channelId)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(
                        channelId, TAG, NotificationManager.IMPORTANCE_LOW
                )
                notificationManager?.createNotificationChannel(notificationChannel)
            }
        }
    }
}