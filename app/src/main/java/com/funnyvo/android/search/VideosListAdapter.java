package com.funnyvo.android.search;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.AdapterClickListener;

import java.util.ArrayList;

public class VideosListAdapter extends RecyclerView.Adapter<VideosListAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<Object> datalist;
    AdapterClickListener adapter_click_listener;

    public VideosListAdapter(Context context, ArrayList<Object> arrayList, AdapterClickListener adapter_click_listener) {
        this.context = context;
        datalist = arrayList;
        this.adapter_click_listener = adapter_click_listener;
    }

    @Override
    public VideosListAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        VideosListAdapter.CustomViewHolder viewHolder = new VideosListAdapter.CustomViewHolder(view);
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

        public void bind(final int pos, final Home item, final AdapterClickListener listener) {

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
    public void onBindViewHolder(final VideosListAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        final Home item = (Home) datalist.get(i);

        holder.username.setText(item.first_name + " " + item.last_name);
        holder.message.setText(item.video_description);

        if (item.thum != null && !item.thum.equals("")) {
            Uri uri = Uri.parse(item.thum);
            holder.user_image.setImageURI(uri);
        }
        holder.bind(i, item, adapter_click_listener);
    }

}