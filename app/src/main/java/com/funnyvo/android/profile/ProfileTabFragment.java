package com.funnyvo.android.profile;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.funnyvo.android.following.FollowingFragment;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.profile.liked_videos.LikedVideoFragment;
import com.funnyvo.android.profile.uservideos.UserVideoFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.SeeFullImageFragment;
import com.funnyvo.android.settings.SettingFragment;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.FragmentCallback;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.videorecording.galleryvideos.GalleryVideosActivity;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileTabFragment extends RootFragment implements View.OnClickListener {
    private View view;
    private Context context;

    private TextView username, username2_txt, video_count_txt;
    private ImageView imageView;
    private TextView follow_count_txt, fans_count_txt, heart_count_txt, draft_count_txt;

    private ImageView setting_btn;

    protected TabLayout tabLayout;
    protected ViewPager pager;
    private ViewPagerAdapter adapter;
    private boolean isdataload = false;
    private RelativeLayout tabs_main_layout;

    private LinearLayout top_layout;
    private static String pic_url;
    private LinearLayout create_popup_layout;

    public ProfileTabFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile_tab, container, false);
        context = getContext();

        return init();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_image:
                openfullsizeImage(pic_url);
                break;

            case R.id.setting_btn:
                openMenuTab(setting_btn);
                break;

            case R.id.following_layout:
                openFollowing();
                break;

            case R.id.fans_layout:
                openFollowers();
                break;

            case R.id.draft_btn:
                Intent upload_intent = new Intent(getActivity(), GalleryVideosActivity.class);
                startActivity(upload_intent);
                getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                break;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showDraftCount();
        if (view != null && !isdataload) {
            if (Variables.sharedPreferences.getBoolean(Variables.islogin, false))
                init();
        }
        if (view != null && isdataload) {
            callApiForGetAllVideos();
        }
    }

    public View init() {
        username = view.findViewById(R.id.username);
        username2_txt = view.findViewById(R.id.username2_txt);
        imageView = view.findViewById(R.id.user_image);
        imageView.setOnClickListener(this);

        video_count_txt = view.findViewById(R.id.video_count_txt);

        follow_count_txt = view.findViewById(R.id.follow_count_txt);
        fans_count_txt = view.findViewById(R.id.fan_count_txt);
        heart_count_txt = view.findViewById(R.id.heart_count_txt);
        draft_count_txt = view.findViewById(R.id.draft_count_txt);

        showDraftCount();

        setting_btn = view.findViewById(R.id.setting_btn);
        setting_btn.setOnClickListener(this);


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


        create_popup_layout = view.findViewById(R.id.create_popup_layout);

        view.findViewById(R.id.following_layout).setOnClickListener(this);
        view.findViewById(R.id.fans_layout).setOnClickListener(this);

        isdataload = true;

        updateProfile();

        callApiForGetAllVideos();

        return view;
    }

    public void showDraftCount() {
        try {

            String path = Variables.draft_app_folder;
            File directory = new File(path);
            File[] files = directory.listFiles();
            draft_count_txt.setText("" + files.length);
            if (files.length <= 0) {
                view.findViewById(R.id.draft_btn).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.draft_btn).setVisibility(View.VISIBLE);
                view.findViewById(R.id.draft_btn).setOnClickListener(this);
            }
        } catch (Exception e) {
            view.findViewById(R.id.draft_btn).setVisibility(View.GONE);
        }
    }

    public void updateProfile() {
        username2_txt.setText(Variables.sharedPreferences.getString(Variables.u_name, ""));
        username.setText(Variables.sharedPreferences.getString(Variables.f_name, "") + " " + Variables.sharedPreferences.getString(Variables.l_name, ""));
        ProfileTabFragment.pic_url = Variables.sharedPreferences.getString(Variables.u_pic, "null");
        userProfilePicture(ProfileTabFragment.pic_url);
    }

    private void userProfilePicture(String picUrl) {
        ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        try {
            Picasso.with(context).load(picUrl)
                    .resize(200, 200)
                    .placeholder(R.drawable.profile_image_placeholder)
                    .centerCrop()
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            final File myImageFile = new File(directory, getString(R.string.user_profile)); // Create image file
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(myImageFile);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            imageView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });

        } catch (Exception e) {

        }
    }

    private void setupTabIcons() {
        View view1 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
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
                        if (UserVideoFragment.myvideo_count > 0) {
                            create_popup_layout.setVisibility(View.GONE);
                        } else {
                            create_popup_layout.setVisibility(View.VISIBLE);
                            Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation);
                            create_popup_layout.startAnimation(aniRotate);
                        }

                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
                        break;

                    case 1:
                        create_popup_layout.clearAnimation();
                        create_popup_layout.setVisibility(View.GONE);
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
                    result = new UserVideoFragment(Variables.sharedPreferences.getString(Variables.u_id, ""));
                    break;
                case 1:
                    result = new LikedVideoFragment(Variables.sharedPreferences.getString(Variables.u_id, ""));
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


    //this will get the all videos data of user and then parse the data
    private void callApiForGetAllVideos() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("my_fb_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, Variables.showMyAllVideos, parameters, new Callback() {
            @Override
            public void response(String resp) {
                parseData(resp);
            }
        });


    }

    public void parseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                JSONObject data = msgArray.getJSONObject(0);
                JSONObject user_info = data.optJSONObject("user_info");
                username2_txt.setText(user_info.optString("username"));
                username.setText(user_info.optString("first_name") + " " + user_info.optString("last_name"));

                ProfileFragment.pic_url = user_info.optString("profile_pic");
                userProfilePicture(ProfileFragment.pic_url);

                follow_count_txt.setText(data.optString("total_following"));
                fans_count_txt.setText(data.optString("total_fans"));
                heart_count_txt.setText(data.optString("total_heart"));


                JSONArray user_videos = data.getJSONArray("user_videos");
                if (!user_videos.toString().equals("[" + "0" + "]")) {
                    video_count_txt.setText(user_videos.length() + " Videos");
                    create_popup_layout.setVisibility(View.GONE);

                } else {
                    create_popup_layout.setVisibility(View.VISIBLE);
                    Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation);
                    create_popup_layout.startAnimation(aniRotate);

                }

                String verified = user_info.optString("verified");
                if (verified != null && verified.equalsIgnoreCase("1")) {
                    view.findViewById(R.id.varified_btn).setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void openEditProfile() {
        EditProfileFragment edit_profile_fragment = new EditProfileFragment(new FragmentCallback() {
            @Override
            public void responseCallBackFromFragment(Bundle bundle) {
                updateProfile();
            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, edit_profile_fragment).commit();
    }


    public void openSetting() {
        SettingFragment setting_fragment = new SettingFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, setting_fragment).commit();
    }


    //this method will get the big size of profile image.
    public void openfullsizeImage(String url) {
        SeeFullImageFragment see_image_f = new SeeFullImageFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        Bundle args = new Bundle();
        args.putSerializable("image_url", url);
        see_image_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, see_image_f).commit();
    }


    public void openMenuTab(View anchor_view) {
        Context wrapper = new ContextThemeWrapper(context, R.style.AlertDialogCustom);
        PopupMenu popup = new PopupMenu(wrapper, anchor_view);
        popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.setGravity(Gravity.TOP | Gravity.RIGHT);
        }
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit_Profile_id:
                        openEditProfile();
                        break;

                    case R.id.setting_id:
                        openSetting();
                        break;

                    case R.id.logout_id:
                        logout();
                        break;

                }
                return true;
            }
        });

    }

    public void openFollowing() {
        FollowingFragment following_fragment = new FollowingFragment(new FragmentCallback() {
            @Override
            public void responseCallBackFromFragment(Bundle bundle) {
                callApiForGetAllVideos();
            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", Variables.sharedPreferences.getString(Variables.u_id, ""));
        args.putString("from_where", "following");
        following_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, following_fragment).commit();

    }

    public void openFollowers() {
        FollowingFragment following_fragment = new FollowingFragment(new FragmentCallback() {
            @Override
            public void responseCallBackFromFragment(Bundle bundle) {
                callApiForGetAllVideos();
            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", Variables.sharedPreferences.getString(Variables.u_id, ""));
        args.putString("from_where", "fan");
        following_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, following_fragment).commit();

    }

    // this will erase all the user info store in locally and logout the user
    public void logout() {
        SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
        editor.putString(Variables.u_id, "");
        editor.putString(Variables.u_name, "");
        editor.putString(Variables.u_pic, "");
        editor.putBoolean(Variables.islogin, false);
        editor.commit();
        getActivity().finish();
        startActivity(new Intent(getActivity(), MainMenuActivity.class));
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Functions.deleteCache(context);
    }

}
