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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.profile.MyVideosAdapter;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.watchvideos.WatchVideosActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserVideoFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<Home> data_list;
    private MyVideosAdapter adapter;
    private View view;
    private Context context;
    private String user_id;

    private RelativeLayout no_data_layout;
    public static int myvideo_count = 0;

    private Boolean is_api_run = false;

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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        recyclerView = view.findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        data_list = new ArrayList<>();
        adapter = new MyVideosAdapter(context, data_list, new MyVideosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, Home item, View view) {
                openWatchVideo(postion);

            }
        });

        recyclerView.setAdapter(adapter);
        no_data_layout = view.findViewById(R.id.no_data_layout);
        callApiForGetAllVideos();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (view != null && (!data_list.isEmpty() && !is_api_run)) {
            callApiForGetAllVideos();
        }
    }

    //this will get the all videos data of user and then parse the data
    private void callApiForGetAllVideos() {
        is_api_run = true;
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("my_fb_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
            parameters.put("fb_id", user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, Variables.showMyAllVideos, parameters, new Callback() {
            @Override
            public void response(String resp) {
                is_api_run = false;
                parseData(resp);
            }
        });
    }

    public void parseData(String responce) {
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

                        if (item.video_url.contains(Variables.base_url)) {
                            item.video_url = item.video_url.replace(Variables.base_url + "/", "");
                        }
                        if (item.sound_pic.contains(Variables.base_url)) {
                            item.sound_pic = item.sound_pic.replace(Variables.base_url + "/", "");
                        }
                        if (item.thum.contains(Variables.base_url)) {
                            item.thum = item.thum.replace(Variables.base_url + "/", "");
                        }

                        data_list.add(i, item);
                    }

                    myvideo_count = data_list.size();

                } else {
                    no_data_layout.setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();
                recyclerView.invalidate();

            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void openWatchVideo(int position) {
        Intent intent = new Intent(getActivity(), WatchVideosActivity.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position", position);
        startActivity(intent);
    }
}
