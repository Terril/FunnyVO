package com.funnyvo.android.home.viewmodel

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyvo.android.extensions.fromJson
import com.funnyvo.android.home.datamodel.VideoList
import com.funnyvo.android.main_menu.MainMenuActivity.token
import com.funnyvo.android.simpleclasses.ApiRequest
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.simpleclasses.Variables.sharedPreferences
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class HomeViewModel @ViewModelInject constructor(
        @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val videoResponseEvent : MutableLiveData<VideoList> = MutableLiveData()
    val likeVideoResponseEvent = MutableLiveData<String>()

    fun requestForVideos(context: Context, url: String, pageNumber: String) {
        viewModelScope.launch {
            val parameters = JSONObject()
            try {
                parameters.put("fb_id", sharedPreferences.getString(Variables.u_id, "0"))
                parameters.put("token", token)
                parameters.put("page_number", pageNumber)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            ApiRequest.callApi(context, url, parameters) { response ->
                run {
                    if (response.isNotEmpty()) {
                        val jsonObject = JSONObject(response)
                        val code = jsonObject.optString("code")
                        if (code == Variables.API_SUCCESS_CODE) {
                            videoResponseEvent.value = fromJson(jsonObject.toString())
                        }
                    }
                }
            }
        }
    }

    fun requestToLikeVideo(context: Context, videoId: String, action: String) {
        viewModelScope.launch {
            val parameters = JSONObject()
            try {
                parameters.put("fb_id", sharedPreferences.getString(Variables.u_id, "0"))
                parameters.put("video_id", videoId)
                parameters.put("action", action)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            ApiRequest.callApi(context, Variables.LIKE_DISLIKE_VIDEO, parameters) { response ->
                run {
                    if (response.isNotEmpty()) {
                        val jsonObject = JSONObject(response)
                        val code = jsonObject.optString("code")
                        if (code == Variables.API_SUCCESS_CODE) {
                            likeVideoResponseEvent.value = fromJson(jsonObject.toString())
                        }
                    }
                }
            }
        }
    }
}