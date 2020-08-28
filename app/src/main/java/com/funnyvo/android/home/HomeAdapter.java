package com.funnyvo.android.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.funnyvo.android.R;
import com.funnyvo.android.customview.FunnyVOTextView;
import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.profile.ProfileFragment;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.CustomViewHolder> {

    public Context context;
    private HomeAdapter.OnItemClickListener listener;
    private ArrayList<Home> dataList;


    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item
    public interface OnItemClickListener {
        void onItemClick(int position, Home item, View view);
    }

    public HomeAdapter(Context context, ArrayList<Home> dataList, HomeAdapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public HomeAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_home_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        HomeAdapter.CustomViewHolder viewHolder = new HomeAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final HomeAdapter.CustomViewHolder holder, final int i) {
        final Home item = dataList.get(i);
        //  holder.setIsRecyclable(false);
        try {
            holder.bind(i, item, listener);
            if (item.video_url.isEmpty()) {
                holder.mainlayout.setVisibility(View.INVISIBLE);
            } else {
                holder.mainlayout.setVisibility(View.VISIBLE);
                holder.username.setText(item.username);

                if ((item.sound_name == null || item.sound_name.equals("") || item.sound_name.equals("null"))) {
                    holder.sound_name.setText(context.getString(R.string.original_sound) + " " + item.first_name + " " + item.last_name);
                } else {
                    holder.sound_name.setText(item.sound_name);
                }
                holder.sound_name.setSelected(true);
                holder.desc_txt.setText(item.video_description);

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
                        .centerCrop()
                        .apply(new RequestOptions().override(100, 100))
                        .placeholder(R.drawable.ic_round_music)
                        .into(holder.sound_image);

                if (item.liked.equals("1")) {
                    holder.like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like_fill));
                } else {
                    holder.like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart));
                }

                if (item.isMute) {
                    holder.btnMuteUnMuteAudio.setImageDrawable(context.getDrawable(R.drawable.ic_music_off));
                } else {
                    holder.btnMuteUnMuteAudio.setImageDrawable(context.getDrawable(R.drawable.ic_music_on));
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

        FunnyVOTextView  desc_txt, sound_name;
        Chip username;
        ImageView user_pic, sound_image, varified_btn;

        LinearLayout like_layout, comment_layout, sound_image_layout, soundImageLayour;
        ImageView like_image, imvHomeVideoSnap;
        FunnyVOTextView like_txt, comment_txt;

        ImageButton btnShare, btnMuteUnMuteAudio;
        PlayerView playerView;

        RelativeLayout mainlayout;

        public CustomViewHolder(View view) {
            super(view);

            username = view.findViewById(R.id.chipUernameHome);
            user_pic = view.findViewById(R.id.user_pic);
            sound_name = view.findViewById(R.id.txtSoundNameHome);
            sound_image = view.findViewById(R.id.sound_image);
            varified_btn = view.findViewById(R.id.varified_btn);

            like_layout = view.findViewById(R.id.like_layout);
            like_image = view.findViewById(R.id.like_image);
            like_txt = view.findViewById(R.id.like_txt);

            desc_txt = view.findViewById(R.id.desc_txt);

            comment_layout = view.findViewById(R.id.comment_layout);
            comment_txt = view.findViewById(R.id.comment_txt);

            btnShare = view.findViewById(R.id.btnShare);
            btnMuteUnMuteAudio = view.findViewById(R.id.btnMuteUnMuteAudio);

            sound_image_layout = view.findViewById(R.id.sound_image_layout);
            playerView = view.findViewById(R.id.playerViewHome);
            soundImageLayour = view.findViewById(R.id.sound_image_layout);

            mainlayout = view.findViewById(R.id.mainLayoutHome);
            imvHomeVideoSnap = view.findViewById(R.id.imvHomeVideoSnap);
        }

        public void bind(final int postion, final Home item, final HomeAdapter.OnItemClickListener listener) {

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

            btnMuteUnMuteAudio.setOnClickListener(new View.OnClickListener() {
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