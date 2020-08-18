package com.funnyvo.android.videorecording.phototemplate

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.funnyvo.android.R
import com.funnyvo.android.base.BaseActivity
import com.funnyvo.android.customview.ZoomOutTransformation
import kotlinx.android.synthetic.main.activity_photo_template.*
import java.util.*

class PhotoTemplateActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideNavigation()
        setContentView(R.layout.activity_photo_template)

        val pagerAdapter = TemplatePagerAdapter(supportFragmentManager)
        pagerAdapter.addFragments(PhotoTemplateFragment())
        pagerAdapter.addFragments(PhotoTemplateFragment())
        pagerAdapter.addFragments(PhotoTemplateFragment())
        pagerImageTemplate.adapter = pagerAdapter
        pagerImageTemplate.setPageTransformer(true, ZoomOutTransformation())
    }

    class TemplatePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        var fragmentList = ArrayList<Fragment>()
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
}