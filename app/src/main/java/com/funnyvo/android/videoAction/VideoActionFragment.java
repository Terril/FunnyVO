package com.funnyvo.android.videoAction;


import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.funnyvo.android.R;
import com.funnyvo.android.customview.ActivityIndicator;
import com.funnyvo.android.simpleclasses.FragmentCallback;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoActionFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    View view;
    Context context;
    RecyclerView recyclerView;
    FragmentCallback fragment_callback;
    String video_id, user_id;

    ActivityIndicator indicator;

    public VideoActionFragment() {
    }

    @SuppressLint("ValidFragment")
    public VideoActionFragment(String id, FragmentCallback fragment_callback) {
        video_id = id;
        this.fragment_callback = fragment_callback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_video_action, container, false);
        context = getContext();

        indicator = new ActivityIndicator(context);
        Bundle bundle = getArguments();
        if (bundle != null) {
            video_id = bundle.getString("video_id");
            user_id = bundle.getString("user_id");
        }

        indicator.show();
        view.findViewById(R.id.save_video_layout).setOnClickListener(this);
        view.findViewById(R.id.copy_layout).setOnClickListener(this);
        view.findViewById(R.id.delete_layout).setOnClickListener(this);

        if (user_id != null && user_id.equals(Variables.sharedPreferences.getString(Variables.u_id, "")))
            view.findViewById(R.id.delete_layout).setVisibility(View.VISIBLE);
        else
            view.findViewById(R.id.delete_layout).setVisibility(View.GONE);


        if (Variables.is_secure_info) {
            view.findViewById(R.id.share_notice_txt).setVisibility(View.VISIBLE);
            indicator.hide();
            view.findViewById(R.id.copy_layout).setVisibility(View.GONE);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSharedApp();
                }
            }, 1000);
        }
        return view;
    }

    VideoSharingAppsAdapter adapter;

    public void getSharedApp() {
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(context, 5);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PackageManager pm = getActivity().getPackageManager();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "https://funnyvo.com");
                    List<ResolveInfo> launchables = pm.queryIntentActivities(intent, 0);
                    for (int i = 0; i < launchables.size(); i++) {
                        if (launchables.get(i).activityInfo.name.contains("SendTextToClipboardActivity")) {
                            launchables.remove(i);
                            break;
                        }

                    }

                    Collections.sort(launchables,
                            new ResolveInfo.DisplayNameComparator(pm));

                    adapter = new VideoSharingAppsAdapter(context, launchables, new VideoSharingAppsAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int positon, ResolveInfo item, View view) {
                            Toast.makeText(context, "" + item.activityInfo.name, Toast.LENGTH_SHORT).show();
                            openApp(item);
                        }
                    });

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setAdapter(adapter);
                            indicator.hide();
                        }
                    });


                } catch (Exception e) {

                }
            }
        }).start();


    }


    public void openApp(ResolveInfo resolveInfo) {
        try {

            ActivityInfo activity = resolveInfo.activityInfo;
            ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                    activity.name);
            Intent i = new Intent(Intent.ACTION_MAIN);

            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            i.setComponent(name);

            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, Variables.base_url + "view.php?id=" + video_id);
            intent.setComponent(name);
            startActivity(intent);
        } catch (Exception e) {

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_video_layout:
                if (Functions.checkstoragepermision(getActivity())) {
                    Bundle bundle = new Bundle();
                    bundle.putString("action", "save");
                    dismiss();
                    fragment_callback.responseCallBackFromFragment(bundle);
                }

                break;

            case R.id.copy_layout:
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", Variables.base_url + "view.php?id=" + video_id);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, "Link Copy in clipboard", Toast.LENGTH_SHORT).show();
                break;

            case R.id.delete_layout:
                if (Variables.is_secure_info) {
                    Toast.makeText(context, getString(R.string.delete_function_not_available_in_demo), Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("action", "delete");
                    dismiss();
                    fragment_callback.responseCallBackFromFragment(bundle);
                }
                break;

        }
    }


}
