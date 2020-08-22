package com.funnyvo.android.videorecording.phototemplate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.funnyvo.android.R
import com.funnyvo.android.base.BaseActivity
import com.funnyvo.android.customview.ZoomOutTransformation
import com.funnyvo.android.helper.FileUtils
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.simpleclasses.Variables.OUTPUT_PHOTO_FILE
import com.funnyvo.android.videorecording.PreviewVideoActivity
import com.sangcomz.fishbun.FishBun
import kotlinx.android.synthetic.main.activity_photo_template.*
import java.io.File
import java.util.*

class PhotoTemplateActivity : BaseActivity() {

    private lateinit var uriList: ArrayList<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideNavigation()
        setContentView(R.layout.activity_photo_template)

        pagerImageTemplate.clipToPadding = false

        val pagerAdapter = TemplatePagerAdapter(supportFragmentManager)
        pagerAdapter.addFragments(PhotoTemplateFragment(getString(R.string.single_pic_msg), 1))
        pagerAdapter.addFragments(PhotoTemplateFragment(getString(R.string.select_4_photo), 4))
        pagerAdapter.addFragments(PhotoTemplateFragment(getString(R.string.select_6_photo), 6))
        pagerImageTemplate.adapter = pagerAdapter
        pagerImageTemplate.setPageTransformer(true, ZoomOutTransformation())

        btnGoBackPost.setOnClickListener {
            onBackPressed()
        }
    }

    class TemplatePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private var fragmentList = ArrayList<Fragment>()
        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return 3
        }

        fun addFragments(fragment: Fragment) {
            fragmentList.add(fragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FishBun.FISHBUN_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                uriList = data?.getParcelableArrayListExtra<Uri>(FishBun.INTENT_PATH)!!

                applySlideTransitions()
//
//                val intent = Intent(this, PhotoTemplatePreviewActivity::class.java)
//                intent.putExtra("imageData", path)
//                startActivity(intent)
            }
        }
    }


    private fun applySlideTransitions() {
        showProgressDialog()

        var command: String = ""
        when (uriList.size) {
            1 -> {
                val path1 = uriList?.get(0)?.let { FileUtils(this).getPath(it) }.orEmpty()
                // Single image video
                command = "-loop 1 -i  \"$path1\" -t 20 -pix_fmt yuv420p $OUTPUT_PHOTO_FILE"
            }
            4 -> {
                val path1 = uriList?.get(0)?.let { FileUtils(this).getPath(it) }.orEmpty()
                val path2 = uriList?.get(1)?.let { FileUtils(this).getPath(it) }.orEmpty()
                val path3 = uriList?.get(2)?.let { FileUtils(this).getPath(it) }.orEmpty()
                val path4 = uriList?.get(3)?.let { FileUtils(this).getPath(it) }.orEmpty()
                //Zoomout and fade working
                command = "-t 5 -i \"$path1\" -t 5 -i \"$path2\" -t 5 -i \"$path3\" -t 5 -i \"$path4\" -filter_complex \"[0:v]zoompan=z='min(max(zoom,pzoom)+0.015,2)':s=720x1280:d=50:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',fade=t=out:st=4:d=1[v0]; " +
                        "[1:v]zoompan=z='min(max(zoom,pzoom)+0.015,2)':s=720x1280:d=50:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',fade=t=in:st=0:d=1,fade=t=out:st=4:d=2[v1]; " +
                        "[2:v]zoompan=z='min(max(zoom,pzoom)+0.015,2)':s=720x1280:d=50:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',fade=t=in:st=0:d=1,fade=t=out:st=4:d=2[v2]; " +
                        "[3:v]zoompan=z='min(max(zoom,pzoom)+0.015,2)':s=720x1280:d=50:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',fade=t=in:st=0:d=1,fade=t=out:st=4:d=1[v3]; " +
                        "[v0][v1][v2][v3]concat=n=4:v=1:a=0,format=yuv420p[v]\" -map [v] -t 40  $OUTPUT_PHOTO_FILE"
            }
            else -> {
                val path1 = uriList?.get(0)?.let { FileUtils(this).getPath(it) }.orEmpty()
                val path2 = uriList?.get(1)?.let { FileUtils(this).getPath(it) }.orEmpty()
                val path3 = uriList?.get(2)?.let { FileUtils(this).getPath(it) }.orEmpty()
                val path4 = uriList?.get(3)?.let { FileUtils(this).getPath(it) }.orEmpty()
                val path5 = uriList?.get(4)?.let { FileUtils(this).getPath(it) }.orEmpty()
                val path6 = uriList?.get(5)?.let { FileUtils(this).getPath(it) }.orEmpty()
                //ZoomIn and fade working
                command = "-t 5 -i \"$path1\" -t 5 -i \"$path2\" -t 5 -i \"$path3\" -t 5 -i \"$path4\" -t 5 -i \"$path5\" -t 5 -i \"$path6\"  -filter_complex \"[0:v]zoompan=z='if(lte(zoom,1.0),1.5,max(1.001,zoom-0.015))':s=720x1280:d=50:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',fade=t=out:st=4:d=1[v0]; " +
                        "[1:v]zoompan=z='if(lte(zoom,1.0),1.5,max(1.001,zoom-0.015))':s=720x1280:d=50:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',fade=t=in:st=0:d=1,fade=t=out:st=4:d=2[v1]; " +
                        "[2:v]zoompan=z='if(lte(zoom,1.0),1.5,max(1.001,zoom-0.015))':s=7280x1280:d=50:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',fade=t=in:st=0:d=1,fade=t=out:st=4:d=2[v2]; " +
                        "[3:v]zoompan=z='if(lte(zoom,1.0),1.5,max(1.001,zoom-0.015))':s=720x1280:d=50:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',fade=t=in:st=0:d=1,fade=t=out:st=4:d=1[v3]; " +
                        "[4:v]zoompan=z='if(lte(zoom,1.0),1.5,max(1.001,zoom-0.015))':s=720x1280:d=50:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',fade=t=in:st=0:d=1,fade=t=out:st=4:d=1[v4]; " +
                        "[5:v]zoompan=z='if(lte(zoom,1.0),1.5,max(1.001,zoom-0.015))':s=720x1280:d=50:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',fade=t=in:st=0:d=1,fade=t=out:st=4:d=1[v5]; " +
                        "[v0][v1][v2][v3][v4][v5]concat=n=6:v=1:a=0,format=yuv420p[v]\" -map [v] -t 40 $OUTPUT_PHOTO_FILE"
            }
        }

        FFmpeg.executeAsync(command) { l: Long, rc: Int ->
            if (rc == Config.RETURN_CODE_SUCCESS) {
                dismissProgressDialog()
                Log.i(Variables.APP_NAME, "Async command execution completed successfully.");
                val intent = Intent(this, PreviewVideoActivity::class.java)
                intent.putExtra("video_path", OUTPUT_PHOTO_FILE)
                intent.putExtra("isFromGallery", true)
                startActivity(intent)
            } else if (rc == Config.RETURN_CODE_CANCEL) {
                dismissProgressDialog()
                Log.i(Variables.APP_NAME, "Async command execution cancelled by user.");
            } else {
                dismissProgressDialog()
                Log.i(Variables.APP_NAME, String.format("Async command execution failed with rc=%d.", rc));
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val outputFile = File(OUTPUT_PHOTO_FILE)
        if (outputFile.exists()) {
            outputFile.delete()
        }
    }
}