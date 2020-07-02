package com.funnyvo.android.videorecording.galleryvideos;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.funnyvo.android.R;
import com.funnyvo.android.videorecording.galleryvideos.datamodel.GalleryVideo;

import java.io.File;
import java.util.ArrayList;


public class GalleryVideosAdapter extends RecyclerView.Adapter<GalleryVideosAdapter.CustomViewHolder> {

    public Context context;
    private GalleryVideosAdapter.OnItemClickListener listener;
    private ArrayList<GalleryVideo> dataList;


    public interface OnItemClickListener {
        void onItemClick(int postion, GalleryVideo item, View view);
    }

    public GalleryVideosAdapter(Context context, ArrayList<GalleryVideo> dataList, GalleryVideosAdapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public GalleryVideosAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_galleryvideo_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        GalleryVideosAdapter.CustomViewHolder viewHolder = new GalleryVideosAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView thumb_image;
        TextView view_txt;

        public CustomViewHolder(View view) {
            super(view);
            thumb_image = view.findViewById(R.id.thumb_image);
            view_txt = view.findViewById(R.id.view_txt);
        }

        public void bind(final int position, final GalleryVideo item, final GalleryVideosAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(position, item, v);
                }
            });

        }

    }


    @Override
    public void onBindViewHolder(final GalleryVideosAdapter.CustomViewHolder holder, final int i) {
        final GalleryVideo item = dataList.get(i);

        holder.view_txt.setText(item.video_time);

        Glide.with(context)
                .load(Uri.fromFile(new File(item.video_path)))
                .into(holder.thumb_image);

        holder.bind(i, item, listener);

    }

}