package com.funnyvo.android.watchvideos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.funnyvo.android.R;
import com.funnyvo.android.home.datamodel.Home;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;


public class WatchVideosAdapter extends RecyclerView.Adapter<WatchVideosAdapter.CustomViewHolder> {

    public Context context;
    private WatchVideosAdapter.OnItemClickListener listener;
    private ArrayList<Home> dataList;


    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item
    public interface OnItemClickListener {
        void onItemClick(int positon, Home item, View view);
    }

    public WatchVideosAdapter(Context context, ArrayList<Home> dataList, WatchVideosAdapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public WatchVideosAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_watch_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        WatchVideosAdapter.CustomViewHolder viewHolder = new WatchVideosAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final WatchVideosAdapter.CustomViewHolder holder, final int i) {
        final Home item = dataList.get(i);

        try {
            holder.bind(i, item, listener);
            if (item.video_url.isEmpty()) {
                holder.mainLayoutWatchVideo.setVisibility(View.INVISIBLE);
            } else {
                holder.username.setText(item.username);
                if ((item.sound_name == null || item.sound_name.equals("") || item.sound_name.equals("null"))) {
                    holder.sound_name.setText("original sound - " + item.first_name + " " + item.last_name);
                } else {
                    holder.sound_name.setText(item.sound_name);
                }
                holder.sound_name.setSelected(true);

                holder.desc_txt.setText("" + item.video_description);

                Glide.with(context)
                        .load(item.profile_pic)
                        .centerCrop()
                        .apply(new RequestOptions().override(100, 100))
                        .placeholder(R.drawable.profile_image_placeholder)
                        .into(holder.user_pic);

                if ((item.sound_name == null || item.sound_name.equals(""))
                        || item.sound_name.equals("null")) {

                    item.sound_pic = item.profile_pic;

                } else if (item.sound_pic.equals(""))
                    item.sound_pic = "Null";

                Glide.with(context)
                        .load(item.sound_pic)
                        .apply(new RequestOptions().override(100, 100))
                        .placeholder(R.drawable.ic_round_music)
                        .into(holder.sound_image);

                if (item.liked.equals("1")) {
                    holder.like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like_fill));
                } else {
                    holder.like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart));
                }

                holder.like_txt.setText(item.like_count);
                holder.comment_txt.setText(item.video_comment_count);

                if (item.verified != null && item.verified.equalsIgnoreCase("1")) {
                    holder.varified_btn.setVisibility(View.VISIBLE);
                } else {
                    holder.varified_btn.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {

        }
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        PlayerView playerview;
        TextView sound_name;
        ImageView user_pic, sound_image, varified_btn;
        Chip username;
        LinearLayout like_layout, comment_layout, sound_image_layout;
        ImageView like_image, comment_image, imvWatchVideoSnap;
        TextView like_txt, desc_txt, comment_txt;

        ImageButton btnShare;

        RelativeLayout mainLayoutWatchVideo;

        public CustomViewHolder(View view) {
            super(view);

            playerview = view.findViewById(R.id.playerViewWatchVideo);

            username = view.findViewById(R.id.chipUsernameWatchVideo);
            user_pic = view.findViewById(R.id.user_pic);
            sound_name = view.findViewById(R.id.sound_name);
            sound_image = view.findViewById(R.id.sound_image);
            varified_btn = view.findViewById(R.id.varified_btn);

            like_layout = view.findViewById(R.id.like_layout);
            like_image = view.findViewById(R.id.like_image);
            like_txt = view.findViewById(R.id.like_txt);


            comment_layout = view.findViewById(R.id.comment_layout);
            comment_image = view.findViewById(R.id.comment_image);
            comment_txt = view.findViewById(R.id.comment_txt);

            desc_txt = view.findViewById(R.id.desc_txt);

            sound_image_layout = view.findViewById(R.id.sound_image_layout);
            btnShare = view.findViewById(R.id.btnShareWatchVideo);

            mainLayoutWatchVideo = view.findViewById(R.id.mainLayoutWatchVideo);
            imvWatchVideoSnap = view.findViewById(R.id.imvWatchVideoSnap);
        }

        public void bind(final int postion, final Home item, final WatchVideosAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion, item, v);
                }
            });


            user_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion, item, v);
                }
            });

            like_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion, item, v);
                }
            });


            comment_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            sound_image_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion, item, v);
                }
            });


        }


    }


}