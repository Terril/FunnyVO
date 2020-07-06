package com.funnyvo.android.following;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.profile.ProfileFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.ApiCallBack;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.FragmentCallback;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.following.datamodel.Following;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FollowingFragment extends RootFragment {

    View view;
    Context context;
    String user_id;

    FollowingAdapter adapter;
    RecyclerView recyclerView;

    ArrayList<Following> datalist;
    RelativeLayout no_data_layout;
    String following_or_fan = "Followers";

    TextView title_txt;

    public FollowingFragment() {
        // Required empty public constructor
    }

    FragmentCallback fragment_callback;

    @SuppressLint("ValidFragment")
    public FollowingFragment(FragmentCallback fragment_callback) {
        this.fragment_callback = fragment_callback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_following, container, false);
        context = getContext();

        Bundle bundle = getArguments();
        if (bundle != null) {
            user_id = bundle.getString("id");
            following_or_fan = bundle.getString("from_where");
        }

        title_txt = view.findViewById(R.id.title_txt);

        datalist = new ArrayList<>();

        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        adapter = new FollowingAdapter(context, following_or_fan, datalist, new FollowingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Following item) {

                switch (view.getId()) {
                    case R.id.action_txt:
                        if (user_id.equals(Variables.sharedPreferences.getString(Variables.u_id, "")))
                            followUnFollowUser(item, postion);
                        break;

                    case R.id.mainlayout:
                        openProfile(item);
                        break;
                }
            }
        }
        );

        recyclerView.setAdapter(adapter);


        no_data_layout = view.findViewById(R.id.no_data_layout);

        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });


        if (following_or_fan.equals("following")) {
            callApiForGetAllfollowing();
            title_txt.setText("Following");
        } else {
            callApiForGetAllfan();
            title_txt.setText("Followers");
        }

        return view;
    }


    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void callApiForGetAllfollowing() {
        showProgressDialog();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.callApi(context, Variables.get_followings, parameters, new Callback() {
            @Override
            public void response(String resp) {
                parseFollowingData(resp);
            }
        });
    }

    public void parseFollowingData(String responce) {

        datalist.clear();

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject profile_data = msgArray.optJSONObject(i);

                    JSONObject follow_Status = profile_data.optJSONObject("follow_Status");

                    Following item = new Following();
                    item.fb_id = profile_data.optString("fb_id");
                    item.first_name = profile_data.optString("first_name");
                    item.last_name = profile_data.optString("last_name");
                    item.bio = profile_data.optString("bio");
                    item.username = profile_data.optString("username");
                    item.profile_pic = profile_data.optString("profile_pic");


                    item.follow = follow_Status.optString("follow");
                    item.follow_status_button = follow_Status.optString("follow_status_button");

                    if (!user_id.equals(Variables.sharedPreferences.getString(Variables.u_id, "")))
                        item.is_show_follow_unfollow_btn = false;


                    datalist.add(item);
                    adapter.notifyItemInserted(i);
                }

                dismissProgressDialog();
                adapter.notifyDataSetChanged();


                if (datalist.isEmpty()) {
                    no_data_layout.setVisibility(View.VISIBLE);
                } else
                    no_data_layout.setVisibility(View.GONE);

            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void callApiForGetAllfan() {
        showProgressDialog();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.callApi(context, Variables.get_followers, parameters, new Callback() {
            @Override
            public void response(String resp) {
                parseFansData(resp);
            }
        });

    }

    public void parseFansData(String responce) {

        datalist.clear();

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject profile_data = msgArray.optJSONObject(i);

                    JSONObject follow_Status = profile_data.optJSONObject("follow_Status");

                    Following item = new Following();
                    item.fb_id = profile_data.optString("fb_id");
                    item.first_name = profile_data.optString("first_name");
                    item.last_name = profile_data.optString("last_name");
                    item.bio = profile_data.optString("bio");
                    item.username = profile_data.optString("username");
                    item.profile_pic = profile_data.optString("profile_pic");


                    item.follow = follow_Status.optString("follow");
                    item.follow_status_button = follow_Status.optString("follow_status_button");


                    datalist.add(item);
                    adapter.notifyItemInserted(i);
                }

                dismissProgressDialog();
                adapter.notifyDataSetChanged();

                if (datalist.isEmpty()) {
                    no_data_layout.setVisibility(View.VISIBLE);
                } else
                    no_data_layout.setVisibility(View.GONE);

            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void openProfile(final Following item) {
        ProfileFragment profile_fragment = new ProfileFragment(new FragmentCallback() {
            @Override
            public void responseCallBackFromFragment(Bundle bundle) {

            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle args = new Bundle();
        args.putString("user_id", item.fb_id);
        args.putString("user_name", item.first_name + " " + item.last_name);
        args.putString("user_pic", item.profile_pic);
        profile_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, profile_fragment).commit();
    }


    public void followUnFollowUser(final Following item, final int position) {

        final String send_status;
        if (item.follow.equals("0")) {
            send_status = "1";
        } else {
            send_status = "0";
        }

        Functions.callApiForFollowOrUnFollow(getActivity(),
                Variables.sharedPreferences.getString(Variables.u_id, ""),
                item.fb_id,
                send_status,
                new ApiCallBack() {
                    @Override
                    public void arrayData(ArrayList arrayList) {

                    }

                    @Override
                    public void onSuccess(String responce) {

                        if (send_status.equals("1")) {
                            item.follow = "1";
                            datalist.remove(position);
                            datalist.add(position, item);
                        } else if (send_status.equals("0")) {
                            item.follow = "0";
                            datalist.remove(position);
                            datalist.add(position, item);
                        }

                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFailure(String responce) {

                    }

                });
    }


    @Override
    public void onDetach() {
        if (fragment_callback != null)
            fragment_callback.responseCallBackFromFragment(new Bundle());

        super.onDetach();
    }
}
