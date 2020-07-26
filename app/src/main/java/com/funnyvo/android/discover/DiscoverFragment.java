package com.funnyvo.android.discover;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.funnyvo.android.R;
import com.funnyvo.android.discover.datamodel.Discover;
import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.search.SearchMainFragment;
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
public class DiscoverFragment extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    RecyclerView recyclerView;
    EditText search_edit;


    SwipeRefreshLayout swiperefresh;

    public DiscoverFragment() {
        // Required empty public constructor
    }

    ArrayList<Discover> datalist;

    DiscoverAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_discover, container, false);
        context = getContext();

        datalist = new ArrayList<>();

        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new DiscoverAdapter(context, datalist, new DiscoverAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ArrayList<Home> datalist, int postion) {
                openWatchVideo(postion, datalist);
            }
        });

        recyclerView.setAdapter(adapter);

        search_edit = view.findViewById(R.id.search_edit);
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String query = search_edit.getText().toString();
                if (adapter != null)
                    adapter.getFilter().filter(query);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                callApiForGetAllvideos();
            }
        });

        view.findViewById(R.id.search_layout).setOnClickListener(this);
        view.findViewById(R.id.search_edit).setOnClickListener(this);

        callApiForGetAllvideos();

        return view;
    }

    // Bottom two function will get the Discover videos
    // from api and parse the json data which is shown in Discover tab

    private void callApiForGetAllvideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiRequest.callApi(context, Variables.DISCOVER, parameters, new Callback() {
            @Override
            public void response(String resp) {
                parseData(resp);
                swiperefresh.setRefreshing(false);
            }
        });


    }


    private void parseData(String response) {
        datalist.clear();

        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int d = 0; d < msgArray.length(); d++) {

                    Discover discover = new Discover();
                    JSONObject discover_object = msgArray.optJSONObject(d);
                    discover.title = discover_object.optString("section_name");

                    JSONArray video_array = discover_object.optJSONArray("sections_videos");

                    ArrayList<Home> video_list = new ArrayList<>();
                    for (int i = 0; i < video_array.length(); i++) {
                        JSONObject itemdata = video_array.optJSONObject(i);
                        Home item = new Home();


                        JSONObject user_info = itemdata.optJSONObject("user_info");
                        item.fb_id = user_info.optString("fb_id");
                        item.username = user_info.optString("username");
                        item.first_name = user_info.optString("first_name");
                        item.last_name = user_info.optString("last_name");
                        item.profile_pic = user_info.optString("profile_pic");
                        item.verified = user_info.optString("verified");

                        JSONObject count = itemdata.optJSONObject("count");
                        item.like_count = count.optString("like_count");
                        item.video_comment_count = count.optString("video_comment_count");

                        JSONObject sound_data = itemdata.optJSONObject("sound");
                        item.sound_id = sound_data.optString("id");
                        item.sound_name = sound_data.optString("sound_name");
                        item.sound_pic = sound_data.optString("thum");


                        item.video_id = itemdata.optString("id");
                        item.liked = itemdata.optString("liked");

                        item.video_url = itemdata.optString("video");

                        item.thum = itemdata.optString("thum");
                        item.gif = itemdata.optString("gif");
                        item.created_date = itemdata.optString("created");
                        item.video_description = itemdata.optString("description");

                        video_list.add(item);
                    }

                    discover.arrayList = video_list;
                    datalist.add(discover);
                }

                adapter.notifyDataSetChanged();
            } else {
             //   Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // When you click on any Video a new activity is open which will play the Clicked video
    private void openWatchVideo(int postion, ArrayList<Home> data_list) {
        Intent intent = new Intent(getActivity(), WatchVideosActivity.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position", postion);
        startActivity(intent);
    }

    public void openSearch() {
        SearchMainFragment search_main_fragment = new SearchMainFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, search_main_fragment).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_layout:
            case R.id.search_edit:
                openSearch();
                break;

        }
    }

}
