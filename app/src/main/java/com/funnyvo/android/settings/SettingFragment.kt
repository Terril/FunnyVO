package com.funnyvo.android.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.funnyvo.android.BuildConfig
import com.funnyvo.android.R
import com.funnyvo.android.accounts.RequestVerificationFragment
import com.funnyvo.android.main_menu.MainMenuActivity
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment
import com.funnyvo.android.simpleclasses.ApiRequest
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.simpleclasses.WebviewFragment
import com.funnyvo.android.splash.SplashActivity
import kotlinx.android.synthetic.main.fragment_setting.*
import org.json.JSONException
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class SettingFragment : RootFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnGoBackSettings.setOnClickListener(this)
        request_verification_txt.setOnClickListener(this)
        privacy_policy_txt.setOnClickListener(this)
        logout_txt.setOnClickListener(this)
        txtRateApp.setOnClickListener(this)
        val items = listOf("Everyone", "Followers", "Private")
        val adapter = ArrayAdapter(requireContext(), R.layout.view_selection_dropdown, items)
        txtVideoVisibleDropdown.setAdapter(adapter)
//        txtVideoVisibleDropdown.postDelayed(Runnable {
//            txtVideoVisibleDropdown.setText(Variables.sharedPreferences.getString(Variables.USER_PREF_VIDEO_VISIBILITY, "Everyone"))
//            txtVideoVisibleDropdown.showDropDown()
//        }, 10)
        txtVideoVisibleDropdown.hint = Variables.sharedPreferences.getString(Variables.USER_PREF_VIDEO_VISIBILITY, "Everyone")

        txtVideoVisibleDropdown.setOnItemClickListener { adapterView, view, position, l ->
            val selection: String = adapterView.getItemAtPosition(position) as String
            val editor = Variables.sharedPreferences.edit()
            editor.putString(Variables.USER_PREF_VIDEO_VISIBILITY, selection)
            editor.apply()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnGoBackSettings -> activity?.onBackPressed()
            R.id.request_verification_txt -> openRequestVerification()
            R.id.privacy_policy_txt -> openPrivacyUrl()
            R.id.logout_txt -> logout()
            R.id.txtRateApp -> openAppPlayStore()
        }
    }

    private fun openAppPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID))
        startActivity(intent)
    }

    private fun openRequestVerification() {
        val requestVerificationFragment = RequestVerificationFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right)
        transaction.addToBackStack(null)
        transaction.replace(R.id.settingsFrameLayout, requestVerificationFragment).commit()
    }

    private fun openPrivacyUrl() {
        val webviewFragment = WebviewFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right)
        val bundle = Bundle()
        bundle.putString("url", Variables.privacy_policy)
        bundle.putString("title", "Privacy Policy")
        webviewFragment.arguments = bundle
        transaction.addToBackStack(null)
        transaction.replace(R.id.settingsFrameLayout, webviewFragment).commit()
    }

    // this will erase all the user info store in locally and logout the user
    private fun logout() {
        val parameters = JSONObject()
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, ""))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        ApiRequest.callApi(activity, Variables.LOGOUT, parameters) { response ->
            try {
                if(response.isNotEmpty()) {
                    val jsonObject = JSONObject(response)
                    val code = jsonObject.optString("code")
                    if (code == Variables.API_SUCCESS_CODE) {
                        val editor = Variables.sharedPreferences.edit()
                        editor.putString(Variables.u_id, "").clear()
                        editor.putString(Variables.u_name, "").clear()
                        editor.putString(Variables.u_pic, "").clear()
                        editor.putBoolean(Variables.islogin, false).clear()
                        editor.apply()
                        val intent = Intent(activity, SplashActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}