package com.funnyvo.android.watchvideos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.funnyvo.android.R
import com.funnyvo.android.base.BaseActivity
import com.funnyvo.android.home.datamodel.Home
import com.funnyvo.android.main_menu.MainMenuActivity
import com.funnyvo.android.simpleclasses.Variables

class WatchVideosActivity : BaseActivity() {
    private var dataList = ArrayList<Home>()
    private var position = 0
    private var videoId: String? = null
    private var link: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_video)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        if (Variables.sharedPreferences == null) {
            Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, Context.MODE_PRIVATE)
        }
        val intent = intent
        val bundle = Bundle()
        if (intent != null) {
            val appLinkData = intent.data
            videoId = intent.getStringExtra("video_id")
            if(videoId != null) {}
            else if (appLinkData == null) {
                dataList = intent.getSerializableExtra("arraylist") as ArrayList<Home>
                position = intent.getIntExtra("position", 0)
            } else {
                link = appLinkData.toString()
                val parts = link!!.split("=".toRegex()).toTypedArray()
                videoId = parts[1]
            }
            bundle.putString("video_id", videoId)
            bundle.putString("link", link)
            bundle.putInt("position", position)
            bundle.putSerializable("arraylist", dataList)
        }
        val ft = supportFragmentManager.beginTransaction()
        val fragment = WatchVideosFragment()
        fragment.arguments = bundle
        ft.replace(R.id.frameLayoutWatchVideo, fragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun onBackPressed() {
        if (videoId != null && link != null) {
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        } else {
            val fragments = supportFragmentManager.backStackEntryCount
            if (fragments == 1) {
                finish()
            } else {
                if (supportFragmentManager.backStackEntryCount > 1) {
                    supportFragmentManager.popBackStack()
                } else {
                    super.onBackPressed()
                }
            }
        }
    }
}