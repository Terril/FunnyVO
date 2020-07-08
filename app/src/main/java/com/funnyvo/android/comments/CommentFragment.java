package com.funnyvo.android.comments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.funnyvo.android.customview.FunnyVOEditTextView;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.ApiCallBack;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.FragmentDataSend;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.comments.datamodel.Comments;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentFragment extends RootFragment {

    private View view;
    private Context context;

    private RecyclerView recyclerView;

    private CommentAdapter adapter;

    private ArrayList<Comments> data_list;

    private String video_id;
    private String user_id;

    private FunnyVOEditTextView message_edit;
    private ImageButton send_btn;
    private ProgressBar send_progress;
    private TextView comment_count_txt;
    private FrameLayout comment_screen;

    public static int comment_count = 0;

    public CommentFragment() {

    }

    FragmentDataSend fragment_data_send;

    @SuppressLint("ValidFragment")
    public CommentFragment(int count, FragmentDataSend fragment_data_send) {
        comment_count = count;
        this.fragment_data_send = fragment_data_send;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_comment, container, false);
        context = getContext();


        comment_screen = view.findViewById(R.id.comment_screen);
        comment_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            video_id = bundle.getString("video_id");
            user_id = bundle.getString("user_id");
        }

        comment_count_txt = view.findViewById(R.id.comment_count);

        recyclerView = view.findViewById(R.id.recylerviewComments);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        data_list = new ArrayList<>();
        adapter = new CommentAdapter(context, data_list, new CommentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, Comments item, View view) {


            }
        });

        recyclerView.setAdapter(adapter);
        message_edit = view.findViewById(R.id.message_edit);
        send_progress = view.findViewById(R.id.send_progress);
        send_btn = view.findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = message_edit.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                        sendComments(video_id, message);
                        message_edit.setText(null);
                        send_progress.setVisibility(View.VISIBLE);
                        send_btn.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(context, "Please Login into the app", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


        getAllComments();


        return view;
    }


    @Override
    public void onDetach() {
        Functions.hideSoftKeyboard(getActivity());

        super.onDetach();
    }

    // this funtion will get all the comments against post
    public void getAllComments() {

        Functions.callApiForGetComment(getActivity(), video_id, new ApiCallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {
                ArrayList<Comments> arrayList1 = arrayList;
                for (Comments item : arrayList1) {
                    data_list.add(item);
                }
                comment_count_txt.setText(data_list.size() + " comments");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onSuccess(String responce) {

            }

            @Override
            public void onFailure(String responce) {

            }

        });

    }


    // this function will call an api to upload your comment
    public void sendComments(String video_id, final String comment) {

        Functions.callApiToSendComment(getActivity(), video_id, comment, new ApiCallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {
                send_progress.setVisibility(View.GONE);
                send_btn.setVisibility(View.VISIBLE);

                ArrayList<Comments> arrayList1 = arrayList;
                for (Comments item : arrayList1) {
                    data_list.add(0, item);
                    comment_count++;

                    sendPushNotification(getActivity(), user_id, comment);

                    comment_count_txt.setText(comment_count + " comments");

                    if (fragment_data_send != null)
                        fragment_data_send.onDataSent("" + comment_count);

                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onSuccess(String responce) {

            }

            @Override
            public void onFailure(String responce) {

            }
        });

    }


    public void sendPushNotification(Activity activity, String user_id, String comment) {

        JSONObject notimap = new JSONObject();
        try {
            notimap.put("title", Variables.sharedPreferences.getString(Variables.u_name, "") + " Comment on your video");
            notimap.put("message", comment);
            notimap.put("icon", Variables.sharedPreferences.getString(Variables.u_pic, ""));
            notimap.put("senderid", Variables.sharedPreferences.getString(Variables.u_id, ""));
            notimap.put("receiverid", user_id);
            notimap.put("action_type", "comment");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiRequest.callApi(context, Variables.sendPushNotification, notimap, null);

    }


}
