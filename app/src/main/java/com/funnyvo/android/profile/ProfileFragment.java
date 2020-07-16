package com.funnyvo.android.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.funnyvo.android.R;
import com.funnyvo.android.SeeFullImageFragment;
import com.funnyvo.android.chat.ChatActivity;
import com.funnyvo.android.following.FollowingFragment;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.profile.liked_videos.LikedVideoFragment;
import com.funnyvo.android.profile.uservideos.UserVideoFragment;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.FragmentCallback;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


// This is the profile screen which is show in 5 tab as well as it is also call
// when we see the profile of other users

public class ProfileFragment extends RootFragment implements View.OnClickListener {

    private MaterialButton follow_unfollow_btn;
    private TextView username, username2_txt, video_count_txt;
    private ImageView imageView;
    private TextView follow_count_txt, fans_count_txt, heart_count_txt;

    private ImageView back_btn, setting_btn;

    private String user_id, user_name, user_pic;
    private Bundle bundle;
    protected TabLayout tabLayout;
    protected ViewPager pager;
    private ViewPagerAdapter adapter;
    private boolean isdataload = false;
    private RelativeLayout tabs_main_layout;
    private LinearLayout top_layout;
    public static String pic_url;
    private boolean is_run_first_time = false;
    private String follow_status = "0";

    FragmentCallback fragment_callback;

    public ProfileFragment() {
    }

    @SuppressLint("ValidFragment")
    public ProfileFragment(FragmentCallback fragment_callback) {
        this.fragment_callback = fragment_callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        if (bundle != null) {
            user_id = bundle.getString("user_id");
            user_name = bundle.getString("user_name");
            user_pic = bundle.getString("user_pic");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        return init(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_image:
                openfullsizeImage(pic_url);
                break;

            case R.id.follow_unfollow_btn:

                if (Variables.sharedPreferences.getBoolean(Variables.islogin, false))
                    followUnFollowUser();
                else
                    Toast.makeText(getActivity(), "Please login in to app", Toast.LENGTH_SHORT).show();

                break;

            case R.id.setting_btn:
                openChat();
                break;

            case R.id.following_layout:
                openFollowing();
                break;

            case R.id.fans_layout:
                openFollowers();
                break;
            case R.id.back_btn:
                getActivity().onBackPressed();
                break;
        }
    }


    private View init(View view) {
        username = view.findViewById(R.id.username);
        username2_txt = view.findViewById(R.id.username2_txt);
        imageView = view.findViewById(R.id.user_image);
        imageView.setOnClickListener(this);

        video_count_txt = view.findViewById(R.id.video_count_txt);

        follow_count_txt = view.findViewById(R.id.follow_count_txt);
        fans_count_txt = view.findViewById(R.id.fan_count_txt);
        heart_count_txt = view.findViewById(R.id.heart_count_txt);


        setting_btn = view.findViewById(R.id.setting_btn);
        setting_btn.setOnClickListener(this);

        back_btn = view.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(this);

        follow_unfollow_btn = view.findViewById(R.id.follow_unfollow_btn);
        follow_unfollow_btn.setOnClickListener(this);


        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(2);

        adapter = new ViewPagerAdapter(getResources(), getChildFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);

        setupTabIcons();


        tabs_main_layout = view.findViewById(R.id.tabs_main_layout);
        top_layout = view.findViewById(R.id.top_layout);


        ViewTreeObserver observer = top_layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                final int height = top_layout.getMeasuredHeight();

                top_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);

                ViewTreeObserver observer = tabs_main_layout.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tabs_main_layout.getLayoutParams();
                        params.height = (int) (tabs_main_layout.getMeasuredHeight() + height);
                        tabs_main_layout.setLayoutParams(params);
                        tabs_main_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                                this);

                    }
                });

            }
        });


        view.findViewById(R.id.following_layout).setOnClickListener(this);
        view.findViewById(R.id.fans_layout).setOnClickListener(this);

        isdataload = true;
        callApiForGetAllvideos();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (is_run_first_time) {
            callApiForGetAllvideos();
        }

    }

    private void setupTabIcons() {

        View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView2 = view2.findViewById(R.id.image);
        imageView2.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
        tabLayout.getTabAt(1).setCustomView(view2);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:

                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
                        break;

                    case 1:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_color));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_gray));
                        break;
                    case 1:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
                        break;
                }

                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final Resources resources;
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public ViewPagerAdapter(final Resources resources, FragmentManager fm) {
            super(fm);
            this.resources = resources;
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new UserVideoFragment(user_id);
                    break;
                case 1:
                    result = new LikedVideoFragment(user_id);
                    break;

                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 2;
        }


        @Override
        public CharSequence getPageTitle(final int position) {
            return null;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }


        /**
         * Get the Fragment by position
         *
         * @param position tab position of the fragment
         * @return
         */
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }


    }

    private void callApiForGetAllvideos() {

        if (bundle == null) {
            user_id = Variables.sharedPreferences.getString(Variables.u_id, "0");
        }

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("my_fb_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
            parameters.put("fb_id", user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.callApi(getActivity(), Variables.showMyAllVideos, parameters, new Callback() {
            @Override
            public void response(String resp) {
                is_run_first_time = true;
                parseData(resp);
            }
        });


    }

    private void parseData(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                JSONObject data = msgArray.getJSONObject(0);
                JSONObject user_info = data.optJSONObject("user_info");
                username.setText(user_info.optString("first_name") + " " + user_info.optString("last_name"));
                username2_txt.setText(user_info.optString("username"));

                ProfileFragment.pic_url = user_info.optString("profile_pic");

                Glide.with(this)
                        .load(ProfileFragment.pic_url)
                        .centerCrop()
                        .apply(new RequestOptions().override(200, 200))
                        .placeholder(R.drawable.profile_image_placeholder)
                        .into(imageView);

                follow_count_txt.setText(data.optString("total_following"));
                fans_count_txt.setText(data.optString("total_fans"));
                heart_count_txt.setText(data.optString("total_heart"));


                if (!data.optString("fb_id").
                        equals(Variables.sharedPreferences.getString(Variables.u_id, ""))) {

                    follow_unfollow_btn.setVisibility(View.VISIBLE);
                    JSONObject follow_Status = data.optJSONObject("follow_Status");
                    follow_unfollow_btn.setText(follow_Status.optString("follow_status_button"));
                    follow_status = follow_Status.optString("follow");
                }


                JSONArray user_videos = data.getJSONArray("user_videos");
                if (!user_videos.toString().equals("[" + "0" + "]")) {
                    video_count_txt.setText(user_videos.length() + " Videos");

                }

                String verified = user_info.optString("verified");
                if (verified != null && verified.equalsIgnoreCase("1")) {
                    getView().findViewById(R.id.varified_btn).setVisibility(View.VISIBLE);
                }


            } else {
                Toast.makeText(getActivity(), "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void followUnFollowUser() {
        final String status;
        if (follow_status.equals("0")) {
            status = "1";
        } else {
            status = "0";
        }

        callApiForFollowUnFollow(status);
    }

    private void callApiForFollowUnFollow(final String status) {
        showProgressDialog();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
            parameters.put("followed_fb_id", user_id);
            parameters.put("status", status);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(getActivity(), Variables.follow_users, parameters, new Callback() {
            @Override
            public void response(String resp) {
                dismissProgressDialog();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {
                        if (status.equals("1")) {
                            follow_unfollow_btn.setText("UnFollow");
                            follow_status = "1";

                        } else if (status.equals("0")) {
                            follow_unfollow_btn.setText("Follow");
                            follow_status = "0";
                        }

                        callApiForGetAllvideos();

                    } else {
                        Toast.makeText(getActivity(), "" + response.optString("msg"), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    //this method will get the big size of profile image.
    private void openfullsizeImage(String url) {
        SeeFullImageFragment see_image_f = new SeeFullImageFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        Bundle args = new Bundle();
        args.putSerializable("image_url", url);
        see_image_f.setArguments(args);
        transaction.addToBackStack(null);

        View view = getActivity().findViewById(R.id.MainMenuFragment);
        if (view != null)
            transaction.replace(R.id.MainMenuFragment, see_image_f).commit();
        else
            transaction.replace(R.id.Profile_F, see_image_f).commit();


    }

    private void openChat() {
        ChatActivity chat_activity = new ChatActivity();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("user_id", user_id);
        args.putString("user_name", user_name);
        args.putString("user_pic", user_pic);
        chat_activity.setArguments(args);
        transaction.addToBackStack(null);

        View view = getActivity().findViewById(R.id.MainMenuFragment);
        if (view != null)
            transaction.replace(R.id.MainMenuFragment, chat_activity).commit();
        else
            transaction.replace(R.id.Profile_F, chat_activity).commit();


    }

    private void openFollowing() {

        FollowingFragment following_fragment = new FollowingFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", user_id);
        args.putString("from_where", "following");
        following_fragment.setArguments(args);
        transaction.addToBackStack(null);


        View view = getActivity().findViewById(R.id.MainMenuFragment);

        if (view != null)
            transaction.replace(R.id.MainMenuFragment, following_fragment).commit();
        else
            transaction.replace(R.id.Profile_F, following_fragment).commit();


    }

    private void openFollowers() {
        FollowingFragment following_fragment = new FollowingFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", user_id);
        args.putString("from_where", "fan");
        following_fragment.setArguments(args);
        transaction.addToBackStack(null);


        View view = getActivity().findViewById(R.id.MainMenuFragment);

        if (view != null)
            transaction.replace(R.id.MainMenuFragment, following_fragment).commit();
        else
            transaction.replace(R.id.Profile_F, following_fragment).commit();


    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (fragment_callback != null)
            fragment_callback.responseCallBackFromFragment(new Bundle());

        Functions.deleteCache(getActivity());

    }


}
