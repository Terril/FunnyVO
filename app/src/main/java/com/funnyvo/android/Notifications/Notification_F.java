package com.funnyvo.android.Notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.funnyvo.android.Inbox.Inbox_F;
import com.funnyvo.android.Main_Menu.MainMenuFragment;
import com.funnyvo.android.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.funnyvo.android.Profile.Profile_F;
import com.funnyvo.android.R;
import com.funnyvo.android.SimpleClasses.ApiRequest;
import com.funnyvo.android.SimpleClasses.Callback;
import com.funnyvo.android.SimpleClasses.Fragment_Callback;
import com.funnyvo.android.SimpleClasses.Variables;
import com.funnyvo.android.WatchVideos.WatchVideos_F;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Notification_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    Notification_Adapter adapter;
    RecyclerView recyclerView;

    ArrayList<Notification_Get_Set> datalist;

    SwipeRefreshLayout swiperefresh;

    public Notification_F() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        context = getContext();

        datalist = new ArrayList<>();

        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new Notification_Adapter(context, datalist, new Notification_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Notification_Get_Set item) {

                switch (view.getId()) {
                    case R.id.watch_btn:
                        OpenWatchVideo(item);
                        break;
                    default:
                        Open_Profile(item);
                        break;
                }
            }
        }
        );

        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.inbox_btn).setOnClickListener(this);

        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Call_api();
            }
        });


        Call_api();
        return view;
    }


    AdView adView;

    @Override
    public void onStart() {
        super.onStart();
        adView = view.findViewById(R.id.bannerad);
        if (!Variables.is_remove_ads) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }
    }


    public void Call_api() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fb_id", Variables.user_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(context, Variables.getNotifications, jsonObject, new Callback() {
            @Override
            public void Response(String resp) {
                swiperefresh.setRefreshing(false);
                parse_data(resp);
            }
        });

    }

    public void parse_data(String resp) {
        try {
            JSONObject jsonObject = new JSONObject(resp);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msg = jsonObject.getJSONArray("msg");
                ArrayList<Notification_Get_Set> temp_list = new ArrayList<>();
                for (int i = 0; i < msg.length(); i++) {
                    JSONObject data = msg.getJSONObject(i);
                    JSONObject fb_id_details = data.optJSONObject("fb_id_details");
                    JSONObject value_data = data.optJSONObject("value_data");

                    Notification_Get_Set item = new Notification_Get_Set();

                    item.fb_id = data.optString("fb_id");

                    item.username = fb_id_details.optString("username");
                    item.first_name = fb_id_details.optString("first_name");
                    item.last_name = fb_id_details.optString("last_name");
                    item.profile_pic = fb_id_details.optString("profile_pic");

                    item.effected_fb_id = fb_id_details.optString("effected_fb_id");

                    item.type = data.optString("type");

                    if (item.type.equalsIgnoreCase("comment_video") || item.type.equalsIgnoreCase("video_like")) {

                        item.id = value_data.optString("id");
                        item.video = value_data.optString("video");
                        item.thum = value_data.optString("thum");
                        item.gif = value_data.optString("gif");

                    }

                    item.created = fb_id_details.optString("created");

                    temp_list.add(item);


                }

                datalist.clear();
                datalist.addAll(temp_list);

                if (datalist.size() <= 0) {
                    view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inbox_btn:
                Open_inbox_F();
                break;
        }
    }

    private void Open_inbox_F() {
        Inbox_F inbox_f = new Inbox_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, inbox_f).commit();

    }

    private void OpenWatchVideo(Notification_Get_Set item) {
        Intent intent = new Intent(getActivity(), WatchVideos_F.class);
        intent.putExtra("video_id", item.id);
        startActivity(intent);
    }


    public void Open_Profile(Notification_Get_Set item) {
        if (Variables.sharedPreferences.getString(Variables.u_id, "0").equals(item.fb_id)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {

            Profile_F profile_f = new Profile_F(new Fragment_Callback() {
                @Override
                public void Response(Bundle bundle) {

                }
            });
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            Bundle args = new Bundle();
            args.putString("user_id", item.fb_id);
            args.putString("user_name", item.first_name + " " + item.last_name);
            args.putString("user_pic", item.profile_pic);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.MainMenuFragment, profile_f).commit();

        }

    }
}
