package com.funnyvo.android.videorecording.viewModel

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.daasuu.gpuv.composer.GPUMp4Composer
import com.funnyvo.android.FunnyVOException
import com.funnyvo.android.R
import com.funnyvo.android.helper.Result
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.videorecording.data.VideoRecording
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AppendTrack
import com.lb.video_trimmer_library.interfaces.VideoTrimmingListener
import com.lb.video_trimmer_library.utils.TrimVideoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class VideoRecordingViewModel @ViewModelInject constructor(
        @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val motionFilter = MutableLiveData<Boolean>()
    val videoRecordingLiveEvent = MutableLiveData<VideoRecording>()
    val videoAppendEvent = MutableLiveData<Boolean>()

    private var deleteCount = 0;

    fun applyFastMoSlowMoVideo(srcFile: String, destFile: String, speedValue: String) {
        viewModelScope.launch {
            when (applyFrameMotionFilter(srcFile, destFile, speedValue)) {
                is Result.Success -> motionFilter.value = true
                else -> motionFilter.value = false
            }
        }
    }

    private suspend fun applyFrameMotionFilter(srcFile: String, destFile: String, speedValue: String): Result<Int> {
        val complexCommand = arrayOf("-y", "-i", srcFile, "-filter_complex", "[0:v]setpts=" + 1.div(speedValue.toFloat()) + "*PTS[v];[0:a]atempo=" + speedValue.toFloat() + "[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", destFile)
        return withContext(Dispatchers.IO) {
            when (FFmpeg.execute(complexCommand)) {
                RETURN_CODE_SUCCESS -> Result.Success(RETURN_CODE_SUCCESS)
                else -> Result.Error(FunnyVOException())
            }
        }
    }

    fun applySlowMoVideo() {
        val complexCommand = arrayOf("-y", "-i", Variables.outputfile2, "-filter_complex", "[0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", Variables.OUTPUT_FILE_MOTION)
        viewModelScope.launch(Dispatchers.IO) {
            FFmpeg.execute(complexCommand)
        }
    }

    fun changeVideoSize(srcPath: String?, destinationPath: String?) {
        var recording: VideoRecording
        GPUMp4Composer(srcPath, destinationPath)
                .size(720, 1280)
                .videoBitrate((0.25 * 16 * 720 * 1280).toInt())
                .listener(object : GPUMp4Composer.Listener {
                    override fun onProgress(progress: Double) {
                        recording = VideoRecording(isInProgress = true)
                        viewModelScope.launch(Dispatchers.Main) {
                            videoRecordingLiveEvent.value = recording
                        }
                    }

                    override fun onCompleted() {
                        recording = VideoRecording(hasCompleted = true)
                        viewModelScope.launch(Dispatchers.Main) {
                            videoRecordingLiveEvent.value = recording
                        }
                    }

                    override fun onCanceled() {
                        recording = VideoRecording(isCancelled = true)
                        viewModelScope.launch(Dispatchers.Main) {
                            videoRecordingLiveEvent.value = recording
                        }
                    }

                    override fun onFailed(exception: Exception) {
                        recording = VideoRecording(hasFailed = true)
                        viewModelScope.launch(Dispatchers.Main) {
                            videoRecordingLiveEvent.value = recording
                        }
                    }
                })
                .start()
    }

    fun trimVideo(context: Context, uri: Uri, trimmingListener: VideoTrimmingListener) {
        viewModelScope.launch(Dispatchers.IO) {
            TrimVideoUtils.startTrim(context, uri, File(Variables.gallery_trimed_video), 1000, 18000, 18000, trimmingListener)
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
        val selectedAudioFile = File(Variables.APP_FOLDER + Variables.SELECTED_AUDIO_AAC)
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
        if (selectedAudioFile.exists()) {
            selectedAudioFile.delete()
        }
        val file = File(Variables.APP_FOLDER + "myvideo" + deleteCount + ".mp4")
        if (file.exists()) {
            file.delete()
            deleteFile()
        }
    }

    fun appendTheContent(context: Context, videoPaths: ArrayList<String>, outputFilePath: String) {
        viewModelScope.launch {
            when (append(context, videoPaths, outputFilePath)) {
                is Result.Success -> videoAppendEvent.value = true
                else -> videoAppendEvent.value = false
            }
        }
    }

    private suspend fun append(context: Context, videoPaths: ArrayList<String>, outputFilePath: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            val videoList = ArrayList<String>()
            for (i in videoPaths.indices) {
                val file = File(videoPaths[i])
                if (file.exists()) {
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(context, Uri.fromFile(file))
                        val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
                        val isVideo = context.getString(R.string.yes) == hasVideo
                        if (isVideo && file.length() > 3000) {
                            //        Log.d("resp", videopaths.get(i));
                            videoList.add(videoPaths[i])
                        }
                    } catch (e: java.lang.Exception) {
                        Log.d(Variables.tag, e.toString())
                    }
                }
            }

            try {
                val inMovies = arrayOfNulls<Movie>(videoPaths.size)
                for (i in videoPaths.indices) {
                    inMovies[i] = MovieCreator.build(videoPaths[i])
                }
                val videoTracks: MutableList<Track> = LinkedList()
                val audioTracks: MutableList<Track> = LinkedList()
                for (m in inMovies) {
                    for (t in m!!.tracks) {
                        if (t.handler == "soun") {
                            audioTracks.add(t)
                        }
                        if (t.handler == "vide") {
                            videoTracks.add(t)
                        }
                    }
                }
                val result = Movie()
                if (audioTracks.size > 0) {
                    result.addTrack(AppendTrack(*audioTracks.toTypedArray()))
                }
                if (videoTracks.size > 0) {
                    result.addTrack(AppendTrack(*videoTracks.toTypedArray()))
                }
                val out = DefaultMp4Builder().build(result)

                val fos = FileOutputStream(File(outputFilePath))
                out.writeContainer(fos.channel)
                fos.close()
                Result.Success(true)

            } catch (e: java.lang.Exception) {
                Result.Error(e)
            }
        }
    }
}