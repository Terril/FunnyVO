package com.funnyvo.android.inbox;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.funnyvo.android.chat.ChatActivity;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.inbox.datamodel.Inbox;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class InboxFragment extends RootFragment {

    View view;
    Context context;

    RecyclerView inbox_list;

    ArrayList<Inbox> inbox_arraylist;
    DatabaseReference root_ref;

    InboxAdapter inbox_adapter;

    ProgressBar pbar;

    boolean isview_created = false;

    public InboxFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_inbox, container, false);
        context = getContext();

        root_ref = FirebaseDatabase.getInstance().getReference();


        pbar = view.findViewById(R.id.pbar);
        inbox_list = view.findViewById(R.id.inboxlist);

        // intialize the arraylist and and inboxlist
        inbox_arraylist = new ArrayList<>();

        inbox_list = (RecyclerView) view.findViewById(R.id.inboxlist);
        LinearLayoutManager layout = new LinearLayoutManager(context);
        inbox_list.setLayoutManager(layout);
        inbox_list.setHasFixedSize(false);
        inbox_adapter = new InboxAdapter(context, inbox_arraylist, new InboxAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Inbox item) {

                // if user allow the stroage permission then we open the chat view
                if (check_ReadStoragepermission())
                    chatFragment(item.getId(), item.getName(), item.getPic());


            }
        }, new InboxAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(Inbox item) {

            }
        });

        inbox_list.setAdapter(inbox_adapter);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.hideSoftKeyboard(getActivity());

            }
        });

        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });


        isview_created = true;
        getData();
        return view;
    }


    AdView adView;

    @Override
    public void onStart() {
        super.onStart();
        adView = view.findViewById(R.id.bannerad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }


    // on start we will get the Inbox Message of user  which is show in bottom list of third tab
    ValueEventListener eventListener2;
    Query inbox_query;

    public void getData() {

        pbar.setVisibility(View.VISIBLE);

        inbox_query = root_ref.child("Inbox").child(Variables.user_id).orderByChild("date");
        eventListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inbox_arraylist.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Inbox model = ds.getValue(Inbox.class);
                    model.setId(ds.getKey());

                    inbox_arraylist.add(model);
                }

                pbar.setVisibility(View.GONE);

                if (inbox_arraylist.isEmpty())
                    view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
                else {
                    view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
                    Collections.reverse(inbox_arraylist);
                    inbox_adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        inbox_query.addValueEventListener(eventListener2);


    }


    // on stop we will remove the listener
    @Override
    public void onStop() {
        super.onStop();
        if (inbox_query != null)
            inbox_query.removeEventListener(eventListener2);
    }

    //open the chat fragment and on item click and pass your id and the other person id in which
    //you want to chat with them and this parameter is that is we move from match list or inbox list
    public void chatFragment(String receiverid, String name, String picture) {
        ChatActivity chat_activity = new ChatActivity();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);

        Bundle args = new Bundle();
        args.putString("user_id", receiverid);
        args.putString("user_name", name);
        args.putString("user_pic", picture);

        chat_activity.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Inbox_F, chat_activity).commit();
    }


    //this method will check there is a storage permission given or not
    private boolean check_ReadStoragepermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            try {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Variables.permission_Read_data);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return false;
    }


}
