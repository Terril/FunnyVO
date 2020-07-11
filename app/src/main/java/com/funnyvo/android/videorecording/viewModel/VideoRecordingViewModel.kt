package com.funnyvo.android.videorecording.viewModel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.funnyvo.android.FunnyVOExceptions
import com.funnyvo.android.helper.Result
import com.funnyvo.android.simpleclasses.Variables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VideoRecordingViewModel @ViewModelInject constructor(
        @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val motionFilter = MutableLiveData<Boolean>();

    private var deleteCount = 0;
//
//    fun applyFastMoVideo() {
//        showProgressDialog()
//        val complexCommand = arrayOf("-y", "-i", Variables.outputfile2, "-filter_complex", "[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", Variables.OUTPUT_FILE_MOTION)
//        object : AsyncTask<Any?, Any?, Any>() {
//            override fun doInBackground(objects: Array<Any?>): Any {
//                return FFmpeg.execute(complexCommand)
//            }
//
//            override fun onPostExecute(o: Any) {
//                super.onPostExecute(o)
//                val rc = o as Int
//                if (rc == Config.RETURN_CODE_SUCCESS) {
//                    dismissProgressDialog()
//                    updateMediaSource(Variables.OUTPUT_FILE_MOTION)
//                    isMotionFilterSelected = true
//                } else if (rc == Config.RETURN_CODE_CANCEL) {
//                    dismissProgressDialog()
//                } else {
//                    Config.printLastCommandOutput(Log.INFO)
//                    dismissProgressDialog()
//                }
//            }
//        }.execute()
    //   }

    fun applyFastMoSlowMoVideo(srcFile: String, destFile: String) {
        viewModelScope.launch {
            when (applyFameMotionFilter(srcFile, destFile)) {
                is Result.Success -> motionFilter.value = true
                else ->  motionFilter.value = false
            }
        }
    }

    private suspend fun applyFameMotionFilter(srcFile: String, destFile: String): Result<Int> {
        val complexCommand = arrayOf("-y", "-i", srcFile, "-filter_complex", "[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", destFile)
        return withContext(Dispatchers.IO) {
            when (FFmpeg.execute(complexCommand)) {
                RETURN_CODE_SUCCESS -> Result.Success(RETURN_CODE_SUCCESS)
                else -> Result.Error(FunnyVOExceptions())
            }
        }
    }

    fun applySlowMoVideo() {
        val complexCommand = arrayOf("-y", "-i", Variables.outputfile2, "-filter_complex", "[0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", Variables.OUTPUT_FILE_MOTION)
        viewModelScope.launch(Dispatchers.IO) {
            FFmpeg.execute(complexCommand)
        }
    }

    fun deleteFile() {
        deleteCount++
        val output = File(Variables.outputfile)
        val output2 = File(Variables.outputfile2)
        val outputFilterFile = File(Variables.OUTPUT_FILTER_FILE)
        val outputFilterMotionFile = File(Variables.OUTPUT_FILE_MOTION)
        val outputFilterTrimmedFile = File(Variables.OUTPUT_FILE_TRIMMED)
        val outputFilterMessageFile = File(Variables.OUTPUT_FILE_MESSAGE)
        if (output.exists()) {
            output.delete()
        }
        if (output2.exists()) {
            output2.delete()
        }
        if (outputFilterFile.exists()) {
            outputFilterFile.delete()
        }
        if (outputFilterMotionFile.exists()) {
            outputFilterMotionFile.delete()
        }
        if (outputFilterTrimmedFile.exists()) {
            outputFilterTrimmedFile.delete()
        }
        if (outputFilterMessageFile.exists()) {
            outputFilterMessageFile.delete()
        }
        val file = File(Variables.app_folder + "myvideo" + deleteCount + ".mp4")
        if (file.exists()) {
            file.delete()
            deleteFile()
        }
    }
}