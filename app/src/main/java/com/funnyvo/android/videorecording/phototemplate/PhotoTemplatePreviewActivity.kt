package com.funnyvo.android.videorecording.phototemplate

import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.funnyvo.android.base.BaseActivity
import com.funnyvo.android.helper.FileUtils
import com.funnyvo.android.simpleclasses.Variables.APP_NAME
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PhotoTemplatePreviewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent.extras
        val uriList = intent?.getParcelableArrayList<Uri>("imageData")
        //   val image1 = uriList?.get(0)?.encodedPath
        val path1 = uriList?.get(0)?.let { FileUtils(this).getPath(it) }
        val path2 = uriList?.get(1)?.let { FileUtils(this).getPath(it) }
        val path3 = uriList?.get(2)?.let { FileUtils(this).getPath(it) }
        val path4 = uriList?.get(3)?.let { FileUtils(this).getPath(it) }
//        val videoFile1 = path1?.let { createFileStream(File(path1), it) } //path1?.substringAfterLast("/")?.let {  }
//        val videoFile2 = path2?.let { createFileStream(File(path2), it) }
//        val videoFile3 =  path3?.let { createFileStream(File(path3), it) }
//        val videoFile4 =  path4?.let { createFileStream(File(path4), it) }
        Log.d(APP_NAME, path1)
//            Log.d(APP_NAME, videoFile1?.path)


        // This is working
       //       val command = "-t 5 -i \"$path1\" -t 5 -i \"$path2\" -t 5 -i \"$path3\" -t 5 -i \"$path4\" -filter_complex \"[0:v]scale=640:-1[1];[1]zoompan=z='if(lte(zoom,1.0),1.5,max(1.001,zoom-0.0015))':d=125,fade=t=out:st=4:d=1[v0]; [1:v]scale=640:-1[2];[2]zoompan=z='if(lte(zoom,1.0),1.5,max(1.001,zoom-0.0015))':d=125,fade=t=in:st=0:d=1,fade=t=out:st=4:d=1[v1];[2:v]scale=640:-1[3];[3]zoompan=z='if(lte(zoom,1.0),1.5,max(1.001,zoom-0.0015))':d=125,fade=t=in:st=0:d=1,fade=t=out:st=4:d=1[v2];[3:v]scale=640:-1[4];[4]zoompan=z='if(lte(zoom,1.0),1.5,max(1.001,zoom-0.0015))':d=125,fade=t=in:st=0:d=1,fade=t=out:st=4:d=1[v3];[v0][v1][v2][v3]concat=n=4:v=1:a=0,format=yuv420p[v] \" -map [v] -preset ultrafast -t 40 $OUTPUT_PHOTO_FILE"


//        //Blend Effect working but not working with framerate
//        val command = "-loop 1 -t 0.5 -i \"$path1\" -loop 1 -t 0.5 -i \"$path2\" -loop 1 -t 0.5 -i \"$path3\" -loop 1 -t 0.5 -i \"$path4\""+
//                " -filter_complex \"[0:v]scale=720:1280:force_original_aspect_ratio=1[v0]; [1:v]scale=720:1280:force_original_aspect_ratio=1[v1]; [v1][v0]blend=all_expr='A*(if(gte(T,0.5),1,T/0.5))+B*(1-(if(gte(T,0.5),1,T/0.5)))'[b1v]; " +
//                "[2:v]scale=720:1280:force_original_aspect_ratio=1[v2]; [v2][v1]blend=all_expr='A*(if(gte(T,0.5),1,T/0.5))+B*(1-(if(gte(T,0.5),1,T/0.5)))'[b2v]; " +
//                "[3:v]scale=720:1280:force_original_aspect_ratio=1[v3]; [v3][v2]blend=all_expr='A*(if(gte(T,0.5),1,T/0.5))+B*(1-(if(gte(T,0.5),1,T/0.5)))'[b3v]; " +
//                "[v0][b1v][v1][b2v][v2][b3v][v3] concat=n=7:v=1:a=0 ,format=yuv420p[v]\" -map [v] $OUTPUT_PHOTO_FILE"

//        val command = "-loop 1 -t 1 -i  \"$path1\" -loop 1 -t 1 -i \"$path2\" -filter_complex \"[0:v]scale=-2:480,split[v0][cv0]; [1:v]scale=-2:480,split[v1][cv1]; " +
//                " [v1][v0]blend=all_expr='A*(if(gte(T,0.5),1,T/0.5))+B*(1-(if(gte(T,0.5),1,T/0.5)))'[b1v];" +
//                " [cv0][b1v][cv1]concat=n=3:v=1:a=0 [v]\" -map [v] -threads 0 -preset ultrafast -y $OUTPUT_PHOTO_FILE"


//        //zoom and Blend
//        val command = " -loop 1 -i \"$path1\" -loop 1 -i \"$path2\" -loop 1 -i \"$path3\"  -filter_complex \"[0:v]zoompan=z='min(zoom+0.0015,1.5)':d=125,trim=duration=5,blend=all_expr='A*(if(gte(T,0.5),1,T/0.5))+B*(1-(if(gte(T,0.5),1,T/0.5)))',setpts=PTS-STARTPTS[v0]; " +
//                "[1:v]zoompan=z='min(zoom+0.0015,1.5)':d=125,trim=duration=5,blend=all_expr='A*(if(gte(T,0.5),1,T/0.5))+B*(1-(if(gte(T,0.5),1,T/0.5)))',setpts=PTS-STARTPTS[v1]; " +
//                "[2:v]zoompan=z='min(zoom+0.0015,1.5)':d=125,trim=duration=5,blend=all_expr='A*(if(gte(T,0.5),1,T/0.5))+B*(1-(if(gte(T,0.5),1,T/0.5)))',setpts=PTS-STARTPTS[v2]; " +
//                "[v0][v1][v2] concat=n=3:v=1:a=0, format=yuv420p[v]\" -map '[v]'  -pix_fmt yuvj420p -q:v 1 $OUTPUT_PHOTO_FILE"



//        val command = arrayOf("-i",
//               "$path1",
//                 "-loglevel",
//                 "trace",
//                 "-pix_fmt",
//                 "yuv420p",
//                "-vf",
//                "transpose=cclock,transpose=cclock,transpose=cclock,scale=1280:1706,crop=1280:720:0:493,zoompan=z=1.25:y=y+0.96:x=128:d=150:s=1280x720:fps=30",
//                 "-c:v",
//                "libx264",
//               "-preset",
//                "ultrafast",
//               "-b:v",
//                "8388608",
//                "-an",
//                OUTPUT_PHOTO_FILE)

     //   This is working
    //    val command = "-loop 1 -i $path1 -t 30 -pix_fmt yuv420p -map [v] -preset ultrafast $OUTPUT_PHOTO_FILE"


       // val command = "-loop 1 -t 3 -i " + videoFile1?.path + " -loop 1 -t 3 -i " + videoFile2?.path + " -loop 1 -t 3 -i " + videoFile3?.path + " -loop 1 -t 3 -i " + videoFile4?.path + " -filter_complex [0:v]trim=duration=3,fade=t=out:st=2.5:d=0.5[v0];[1:v]trim=duration=3,fade=t=in:st=0:d=0.5,fade=t=out:st=2.5:d=0.5[v1];[2:v]trim=duration=3,fade=t=in:st=0:d=0.5,fade=t=out:st=2.5:d=0.5[v2];[3:v]trim=duration=3,fade=t=in:st=0:d=0.5,fade=t=out:st=2.5:d=0.5[v3];[v0][v1][v2][v3]concat=n=4:v=1:a=0,format=yuv420p[v] -map [v] -preset ultrafast " + OUTPUT_PHOTO_FILE

//        observeLiveEvents()
//        if (path1 != null) {
//            viewModel.makeImageToVideo(path1)
//        }
//
//        FFmpeg.executeAsync(command) { l: Long, rc: Int ->
//            if (rc == Config.RETURN_CODE_SUCCESS) {
//                Log.i(Variables.APP_NAME, "Async command execution completed successfully.");
//            } else if (rc == Config.RETURN_CODE_CANCEL) {
//                Log.i(Variables.APP_NAME, "Async command execution cancelled by user.");
//            } else {
//                Log.i(Variables.APP_NAME, String.format("Async command execution failed with rc=%d.", rc));
//            }
//        }
    }


    private fun createFileStream(imageFile: File, path: String): File {
        val fileArray = path.split("/")
        val fileName = fileArray.last()

        Log.i(APP_NAME, fileName);
        if (!imageFile.exists()) try {

            val isFile: InputStream = assets.open(fileName)
            val size = isFile.available()
            val buffer = ByteArray(size)
            isFile.read(buffer)
            isFile.close()


            val fos = FileOutputStream(imageFile)
            fos.write(buffer);
            fos.close();
        } catch (e: Exception) {

        }

        return imageFile
    }
}