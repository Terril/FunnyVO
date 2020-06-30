package com.funnyvo.android.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.R;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class MyVideosAdapter extends RecyclerView.Adapter<MyVideosAdapter.CustomViewHolder> {

    public Context context;
    private MyVideosAdapter.OnItemClickListener listener;
    private ArrayList<Home> dataList;


    public interface OnItemClickListener {
        void onItemClick(int postion, Home item, View view);
    }

    public MyVideosAdapter(Context context, ArrayList<Home> dataList, MyVideosAdapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public MyVideosAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_myvideo_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        MyVideosAdapter.CustomViewHolder viewHolder = new MyVideosAdapter.CustomViewHolder(view);
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

        public void bind(final int position, final Home item, final MyVideosAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(position, item, v);
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(final MyVideosAdapter.CustomViewHolder holder, final int i) {
        final Home item = dataList.get(i);
        holder.setIsRecyclable(false);


        try {
            Glide.with(context)
                    .asGif()
                    .load(item.gif)
                    .skipMemoryCache(true)
                    .thumbnail(new RequestBuilder[]{Glide
                            .with(context)
                            .load(item.thum)})
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)
                            .placeholder(context.getResources().getDrawable(R.drawable.image_placeholder)).centerCrop())

                    .into(holder.thumb_image);

        } catch (Exception e) {

        }


        holder.view_txt.setText(item.views);

        holder.bind(i, item, listener);

    }

}