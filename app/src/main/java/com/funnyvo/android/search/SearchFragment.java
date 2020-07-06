package com.funnyvo.android.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.main_menu.MainMenuFragment;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.profile.ProfileFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.AdapterClickListener;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.FragmentCallback;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.watchvideos.WatchVideosFragment;
import com.funnyvo.android.search.datamodel.Users;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.funnyvo.android.search.SearchMainFragment.search_edit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends RootFragment {
    View view;
    Context context;
    String type, key;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;

    public SearchFragment(String type) {
        this.type = type;
        this.key = key;
    }

    public SearchFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();

        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        recyclerView = view.findViewById(R.id.recylerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        Call_Api();

        return view;
    }


    public void Call_Api() {

        JSONObject params = new JSONObject();
        try {
            params.put("type", type);
            params.put("keyword", search_edit.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, Variables.search, params, new Callback() {
            @Override
            public void response(String resp) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                if (type.equalsIgnoreCase("users"))
                    Parse_users(resp);

                if (type.equals("video"))
                    Parse_video(resp);


            }
        });

    }


    ArrayList<Object> data_list;

    public void Parse_users(String responce) {

        data_list = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equalsIgnoreCase("200")) {

                JSONArray msg = jsonObject.optJSONArray("msg");
                for (int i = 0; i < msg.length(); i++) {
                    JSONObject data = msg.optJSONObject(i);

                    Users user = new Users();
                    user.fb_id = data.optString("fb_id");
                    user.username = data.optString("username");
                    user.first_name = data.optString("first_name");
                    user.last_name = data.optString("last_name");
                    user.gender = data.optString("gender");
                    user.profile_pic = data.optString("profile_pic");
                    user.signup_type = data.optString("signup_type");
                    user.videos = data.optString("videos");

                    data_list.add(user);
                }

                if (data_list.isEmpty()) {
                    view.findViewById(R.id.no_data_image).setVisibility(View.VISIBLE);
                } else
                    view.findViewById(R.id.no_data_image).setVisibility(View.GONE);

                UsersAdapter adapter = new UsersAdapter(context, data_list, new AdapterClickListener() {
                    @Override
                    public void onItemClick(View view, int pos, Object object) {

                        Users item = (Users) object;
                        openProfile(item.fb_id, item.first_name, item.last_name, item.profile_pic);
                    }
                });
                recyclerView.setAdapter(adapter);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void Parse_video(String responce) {

        data_list = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    Home item = new Home();
                    item.fb_id = itemdata.optString("fb_id");

                    JSONObject user_info = itemdata.optJSONObject("user_info");

                    item.first_name = user_info.optString("first_name", context.getResources().getString(R.string.app_name));
                    item.last_name = user_info.optString("last_name", "User");
                    item.profile_pic = user_info.optString("profile_pic", "null");
                    item.verified = user_info.optString("verified");

                    JSONObject sound_data = itemdata.optJSONObject("sound");
                    item.sound_id = sound_data.optString("id");
                    item.sound_name = sound_data.optString("sound_name");
                    item.sound_pic = sound_data.optString("thum");


                    JSONObject count = itemdata.optJSONObject("count");
                    item.like_count = count.optString("like_count");
                    item.video_comment_count = count.optString("video_comment_count");


                    item.video_id = itemdata.optString("id");
                    item.liked = itemdata.optString("liked");
                    item.video_url = itemdata.optString("video");
                    item.video_description = itemdata.optString("description");

                    item.thum = itemdata.optString("thum");
                    item.created_date = itemdata.optString("created");

                    data_list.add(item);
                }

                if (data_list.isEmpty()) {
                    view.findViewById(R.id.no_data_image).setVisibility(View.VISIBLE);
                } else
                    view.findViewById(R.id.no_data_image).setVisibility(View.GONE);


                VideosListAdapter adapter = new VideosListAdapter(context, data_list, new AdapterClickListener() {
                    @Override
                    public void onItemClick(View view, int pos, Object object) {

                        Home item = (Home) object;
                        if (view.getId() == R.id.btnWatch) {
                            openWatchVideo(item.video_id);
                        } else {
                            openProfile(item.fb_id, item.first_name, item.last_name, item.profile_pic);
                        }
                    }
                });
                recyclerView.setAdapter(adapter);


            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void openWatchVideo(String video_id) {
        Intent intent = new Intent(getActivity(), WatchVideosFragment.class);
        intent.putExtra("video_id", video_id);
        startActivity(intent);
    }

    public void openProfile(String fb_id, String first_name, String last_name, String profile_pic) {
        if (Variables.sharedPreferences.getString(Variables.u_id, "0").equals(fb_id)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {

            ProfileFragment profile_fragment = new ProfileFragment(new FragmentCallback() {
                @Override
                public void responseCallBackFromFragment(Bundle bundle) {

                }
            });
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            Bundle args = new Bundle();
            args.putString("user_id", fb_id);
            args.putString("user_name", first_name + " " + last_name);
            args.putString("user_pic", profile_pic);
            profile_fragment.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.Search_Main_F, profile_fragment).commit();

        }

    }

}
