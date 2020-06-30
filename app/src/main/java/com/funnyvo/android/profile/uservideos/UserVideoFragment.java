package com.funnyvo.android.profile.uservideos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.profile.MyVideosAdapter;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.watchvideos.WatchVideosFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserVideoFragment extends Fragment {

    public RecyclerView recyclerView;
    ArrayList<Home> data_list;
    MyVideosAdapter adapter;
    View view;
    Context context;
    String user_id;

    RelativeLayout no_data_layout;
    public static int myvideo_count = 0;

    public UserVideoFragment() {

    }

    @SuppressLint("ValidFragment")
    public UserVideoFragment(String user_id) {

        this.user_id = user_id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_video, container, false);

        context = getContext();

        recyclerView = view.findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        data_list = new ArrayList<>();
        adapter = new MyVideosAdapter(context, data_list, new MyVideosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, Home item, View view) {

                OpenWatchVideo(postion);

            }
        });

        recyclerView.setAdapter(adapter);

        no_data_layout = view.findViewById(R.id.no_data_layout);

        Call_Api_For_get_Allvideos();

        return view;
    }

    Boolean isVisibleToUser = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        if (view != null && isVisibleToUser) {
            Call_Api_For_get_Allvideos();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if ((view != null && isVisibleToUser) && (!data_list.isEmpty() && !is_api_run)) {
            Call_Api_For_get_Allvideos();
        }
    }

    Boolean is_api_run = false;

    //this will get the all videos data of user and then parse the data
    private void Call_Api_For_get_Allvideos() {
        is_api_run = true;
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("my_fb_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
            parameters.put("fb_id", user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(context, Variables.showMyAllVideos, parameters, new Callback() {
            @Override
            public void Response(String resp) {
                is_api_run = false;
                Parse_data(resp);
            }
        });
    }

    public void Parse_data(String responce) {
        data_list.clear();
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                JSONObject data = msgArray.getJSONObject(0);
                JSONObject user_info = data.optJSONObject("user_info");


                JSONArray user_videos = data.getJSONArray("user_videos");
                if (!user_videos.toString().equals("[" + "0" + "]")) {

                    no_data_layout.setVisibility(View.GONE);

                    for (int i = 0; i < user_videos.length(); i++) {
                        JSONObject itemdata = user_videos.optJSONObject(i);

                        Home item = new Home();
                        item.fb_id = user_id;

                        item.first_name = user_info.optString("first_name");
                        item.last_name = user_info.optString("last_name");
                        item.profile_pic = user_info.optString("profile_pic");
                        item.verified = user_info.optString("verified");

                        Log.d("resp", item.fb_id + " " + item.first_name);

                        JSONObject count = itemdata.optJSONObject("count");
                        item.like_count = count.optString("like_count");
                        item.video_comment_count = count.optString("video_comment_count");
                        item.views = count.optString("view");

                        JSONObject sound_data = itemdata.optJSONObject("sound");
                        item.sound_id = sound_data.optString("id");
                        item.sound_name = sound_data.optString("sound_name");
                        item.sound_pic = sound_data.optString("thum");


                        item.video_id = itemdata.optString("id");
                        item.liked = itemdata.optString("liked");
                        item.gif = itemdata.optString("gif");
                        item.video_url = itemdata.optString("video");
                        item.thum = itemdata.optString("thum");
                        item.created_date = itemdata.optString("created");

                        item.video_description = itemdata.optString("description");

                        data_list.add(item);
                    }

                    myvideo_count = data_list.size();

                } else {
                    no_data_layout.setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void OpenWatchVideo(int postion) {
        Intent intent = new Intent(getActivity(), WatchVideosFragment.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position", postion);
        startActivity(intent);
    }
}
