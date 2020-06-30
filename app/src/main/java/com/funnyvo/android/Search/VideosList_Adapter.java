package com.funnyvo.android.Search;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.funnyvo.android.Home.Home_Get_Set;
import com.funnyvo.android.R;
import com.funnyvo.android.SimpleClasses.Adapter_Click_Listener;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class VideosList_Adapter extends RecyclerView.Adapter<VideosList_Adapter.CustomViewHolder> {
    public Context context;

    ArrayList<Object> datalist;
    Adapter_Click_Listener adapter_click_listener;

    public VideosList_Adapter(Context context, ArrayList<Object> arrayList, Adapter_Click_Listener adapter_click_listener) {
        this.context = context;
        datalist = arrayList;
        this.adapter_click_listener = adapter_click_listener;
    }

    @Override
    public VideosList_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        VideosList_Adapter.CustomViewHolder viewHolder = new VideosList_Adapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView user_image;
        TextView username, message, watch_btn;

        public CustomViewHolder(View view) {
            super(view);
            user_image = view.findViewById(R.id.user_image);
            username = view.findViewById(R.id.username);
            message = view.findViewById(R.id.message);
            watch_btn = view.findViewById(R.id.watch_btn);
        }

        public void bind(final int pos, final Home_Get_Set item, final Adapter_Click_Listener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

            watch_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(final VideosList_Adapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        final Home_Get_Set item = (Home_Get_Set) datalist.get(i);

        holder.username.setText(item.first_name + " " + item.last_name);
        holder.message.setText(item.video_description);

        if (item.thum != null && !item.thum.equals("")) {
            Uri uri = Uri.parse(item.thum);
            holder.user_image.setImageURI(uri);
        }
        holder.bind(i, item, adapter_click_listener);
    }

}