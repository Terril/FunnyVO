package com.funnyvo.android.Taged;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.profile.MyVideosAdapter;
import com.funnyvo.android.R;
import com.funnyvo.android.SimpleClasses.ApiRequest;
import com.funnyvo.android.SimpleClasses.Callback;
import com.funnyvo.android.SimpleClasses.Functions;
import com.funnyvo.android.SimpleClasses.Variables;
import com.funnyvo.android.WatchVideos.WatchVideos_F;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Taged_Videos_F extends RootFragment {

    View view;
    Context context;

    NestedScrollView scrollView;
    RelativeLayout recylerview_main_layout;

    LinearLayout top_layout;

    RecyclerView recyclerView;
    ArrayList<Home> data_list;
    MyVideosAdapter adapter;

    String tag_txt;

    TextView tag_txt_view, tag_title_txt;

    ProgressBar progress_bar;

    public Taged_Videos_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_taged_videos, container, false);
        context = getContext();

        if (Variables.sharedPreferences == null) {
            Variables.sharedPreferences = getActivity().getSharedPreferences(Variables.pref_name, Context.MODE_PRIVATE);
        }


        Bundle bundle = getArguments();
        if (bundle != null) {
            tag_txt = bundle.getString("tag");
        }


        tag_txt_view = view.findViewById(R.id.tag_txt_view);
        tag_title_txt = view.findViewById(R.id.tag_title_txt);

        tag_txt_view.setText(tag_txt);
        tag_title_txt.setText(tag_txt);

        recyclerView = view.findViewById(R.id.recylerview);
        scrollView = view.findViewById(R.id.scrollview);


        top_layout = view.findViewById(R.id.top_layout);
        recylerview_main_layout = view.findViewById(R.id.recylerview_main_layout);


        ViewTreeObserver observer = top_layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                final int height = top_layout.getMeasuredHeight();

                top_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);

                ViewTreeObserver observer = recylerview_main_layout.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        // TODO Auto-generated method stub
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recylerview_main_layout.getLayoutParams();
                        params.height = (int) (recylerview_main_layout.getMeasuredHeight() + height);
                        recylerview_main_layout.setLayoutParams(params);
                        recylerview_main_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                                this);

                    }
                });

            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                    if (!scrollView.canScrollVertically(1)) {
                        recyclerView.setNestedScrollingEnabled(true);


                    } else {
                        recyclerView.setNestedScrollingEnabled(false);
                    }

                }
            });
        }


        recyclerView = view.findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setNestedScrollingEnabled(false);
        } else {
            recyclerView.setNestedScrollingEnabled(true);
        }

        data_list = new ArrayList<>();
        adapter = new MyVideosAdapter(context, data_list, new MyVideosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, Home item, View view) {

                OpenWatchVideo(postion);

            }
        });

        recyclerView.setAdapter(adapter);


        progress_bar = view.findViewById(R.id.progress_bar);

        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        Call_Api_For_get_Allvideos();


        return view;
    }


    //this will get the all videos data of user and then parse the data
    private void Call_Api_For_get_Allvideos() {
        progress_bar.setVisibility(View.VISIBLE);
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
            parameters.put("tag", tag_txt);
            parameters.put("token", MainMenuActivity.token);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(context, Variables.SearchByHashTag, parameters, new Callback() {
            @Override
            public void Response(String resp) {
                progress_bar.setVisibility(View.GONE);
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

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    JSONObject user_info = itemdata.optJSONObject("user_info");

                    Home item = new Home();
                    item.fb_id = itemdata.optString("fb_id");

                    Log.d("resp", item.fb_id);

                    item.first_name = user_info.optString("first_name");
                    item.last_name = user_info.optString("last_name");
                    item.profile_pic = user_info.optString("profile_pic");
                    item.verified = user_info.optString("verified");

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


                adapter.notifyDataSetChanged();
                progress_bar.setVisibility(View.GONE);

            } else {
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            progress_bar.setVisibility(View.GONE);
            e.printStackTrace();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Functions.deleteCache(context);
    }

    private void OpenWatchVideo(int postion) {
        Intent intent = new Intent(getActivity(), WatchVideos_F.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position", postion);
        startActivity(intent);
    }

}
