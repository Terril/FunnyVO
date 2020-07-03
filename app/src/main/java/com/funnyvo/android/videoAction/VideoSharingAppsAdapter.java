package com.funnyvo.android.videoAction;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.funnyvo.android.R;

import java.util.List;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class VideoSharingAppsAdapter extends RecyclerView.Adapter<VideoSharingAppsAdapter.CustomViewHolder> {

    public Context context;
    private VideoSharingAppsAdapter.OnItemClickListener listener;
    private List<ResolveInfo> dataList;


    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item
    public interface OnItemClickListener {
        void onItemClick(int positon, ResolveInfo item, View view);
    }


    public VideoSharingAppsAdapter(Context context, List<ResolveInfo> dataList, VideoSharingAppsAdapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public VideoSharingAppsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_videosharingapps_layout, null);
        VideoSharingAppsAdapter.CustomViewHolder viewHolder = new VideoSharingAppsAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final VideoSharingAppsAdapter.CustomViewHolder holder, final int i) {
        final ResolveInfo item = dataList.get(i);
        holder.setIsRecyclable(false);

        try {

            holder.bind(i, item, listener);

            holder.image.setImageDrawable(item.loadIcon(context.getPackageManager()));


        } catch (Exception e) {

        }
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView image;


        public CustomViewHolder(View view) {
            super(view);


            image = view.findViewById(R.id.image);
        }

        public void bind(final int postion, final ResolveInfo item, final VideoSharingAppsAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion, item, v);
                }
            });


        }


    }


}