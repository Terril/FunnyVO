package com.funnyvo.android.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.funnyvo.android.R
import com.funnyvo.android.inbox.InboxFragment
import com.funnyvo.android.main_menu.MainMenuFragment
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment
import com.funnyvo.android.notifications.datamodel.Notification
import com.funnyvo.android.profile.ProfileFragment
import com.funnyvo.android.simpleclasses.ApiRequest
import com.funnyvo.android.simpleclasses.FragmentCallback
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.watchvideos.WatchVideosActivity
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.fragment_notification.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class NotificationFragment : RootFragment(), View.OnClickListener {
    private var adapter: NotificationAdapter? = null
    private var datalist: ArrayList<Notification> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return  inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(context)
        recylerViewNotification.layoutManager = layoutManager
        recylerViewNotification.setHasFixedSize(true)
        adapter = NotificationAdapter(context, datalist, NotificationAdapter.OnItemClickListener { view, postion, item ->
            when (view.id) {
                R.id.btnWatch -> openWatchVideo(item)
                else -> openProfile(item)
            }
        }
        )
        recylerViewNotification.adapter = adapter
        btnInbox.setOnClickListener(this)
        swipeRefreshNotification?.setOnRefreshListener(OnRefreshListener { callApi() })
        callApi()
    }

    var adView: AdView? = null
    override fun onStart() {
        super.onStart()
        //        adView = view.findViewById(R.id.bannerad);
//        if (!Variables.is_remove_ads) {
//            AdRequest adRequest = new AdRequest.Builder().build();
//            adView.loadAd(adRequest);
//        } else {
//            adView.setVisibility(View.GONE);
//        }
    }

    fun callApi() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("fb_id", Variables.user_id)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        showProgressDialog()
        ApiRequest.callApi(context, Variables.GET_NOTIFICATIONS, jsonObject) { resp ->
            dismissProgressDialog()
            swipeRefreshNotification.isRefreshing = false
            parseData(resp)
        }
    }

    fun parseData(resp: String?) {
        try {
            val jsonObject = JSONObject(resp)
            val code = jsonObject.optString("code")
            if (code == Variables.API_SUCCESS_CODE) {
                val msg = jsonObject.getJSONArray("msg")
                val tempList = ArrayList<Notification>()
                for (i in 0 until msg.length()) {
                    val data = msg.getJSONObject(i)
                    val fbIdDetails = data.optJSONObject("fb_id_details")
                    val valueData = data.optJSONObject("value_data")
                    val item = Notification()
                    item.fb_id = data.optString("fb_id")
                    item.username = fbIdDetails.optString("username")
                    item.first_name = fbIdDetails.optString("first_name")
                    item.last_name = fbIdDetails.optString("last_name")
                    item.profile_pic = fbIdDetails.optString("profile_pic")
                    item.effected_fb_id = fbIdDetails.optString("effected_fb_id")
                    item.type = data.optString("type")
                    if (item.type.equals("comment_video", ignoreCase = true) || item.type.equals("video_like", ignoreCase = true)) {
                        item.id = valueData.optString("id")
                        item.video = valueData.optString("video")
                        item.thum = valueData.optString("thum")
                        item.gif = valueData.optString("gif")
                    }
                    item.created = fbIdDetails.optString("created")
                    tempList.add(item)
                }
                datalist!!.clear()
                datalist!!.addAll(tempList)
                if (datalist!!.size <= 0) {
                    noDataLayoutNotification.visibility = View.VISIBLE
                } else {
                    noDataLayoutNotification.visibility = View.GONE
                }
                adapter!!.notifyDataSetChanged()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnInbox -> openInbox()
        }
    }

    private fun openInbox() {
        val inboxFragment = InboxFragment()
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom)
        transaction?.addToBackStack(null)
        transaction?.replace(R.id.main_menu_container, inboxFragment)?.commit()
    }

    private fun openWatchVideo(item: Notification) {
        val intent = Intent(activity, WatchVideosActivity::class.java)
        intent.putExtra("video_id", item.id)
        startActivity(intent)
    }

    private fun openProfile(item: Notification) {
        if (Variables.sharedPreferences.getString(Variables.u_id, "0") == item.fb_id) {
            val profile = MainMenuFragment.tabLayout.getTabAt(4)
            profile!!.select()
        } else {
            val profileFragment = ProfileFragment(FragmentCallback { })
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right)
            val args = Bundle()
            args.putString("user_id", item.fb_id)
            args.putString("user_name", item.first_name + " " + item.last_name)
            args.putString("user_pic", item.profile_pic)
            profileFragment.arguments = args
            transaction?.addToBackStack(null)
            transaction?.replace(R.id.main_menu_container, profileFragment)?.commit()
        }
    }
}