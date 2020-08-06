package com.funnyvo.android.following;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.funnyvo.android.R;
import com.funnyvo.android.following.datamodel.Following;
import com.funnyvo.android.profile.ProfileFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.CustomViewHolder> {
    public Context context;
    String following_or_fans;
    ArrayList<Following> datalist;

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Following item);
    }

    public FollowingAdapter.OnItemClickListener listener;

    public FollowingAdapter(Context context, String following_or_fans, ArrayList<Following> arrayList, FollowingAdapter.OnItemClickListener listener) {
        this.context = context;
        this.following_or_fans = following_or_fans;
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public FollowingAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_following, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        FollowingAdapter.CustomViewHolder viewHolder = new FollowingAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView user_image;
        TextView user_name;
        TextView user_id;
        MaterialButton buttonUserAction;
        RelativeLayout mainlayout;

        public CustomViewHolder(View view) {
            super(view);

            mainlayout = view.findViewById(R.id.mainlayout);

            user_image = view.findViewById(R.id.user_image);
            user_name = view.findViewById(R.id.user_name);
            user_id = view.findViewById(R.id.user_id);

            buttonUserAction = view.findViewById(R.id.buttonUserAction);
        }

        public void bind(final int pos, final Following item, final FollowingAdapter.OnItemClickListener listener) {


            mainlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

            buttonUserAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(final FollowingAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);
        Following item = datalist.get(i);
        holder.user_name.setText(item.first_name + " " + item.last_name);

        Glide.with(context)
                .load(item.profile_pic)
                .placeholder(R.drawable.profile_image_placeholder)
                .into(holder.user_image);

        holder.user_id.setText(item.username);

        if (item.is_show_follow_unfollow_btn) {
            holder.buttonUserAction.setVisibility(View.VISIBLE);

            if (following_or_fans.equals("following")) {

                if (item.follow.equals("0")) {
                    holder.buttonUserAction.setText("Follow");
                    holder.buttonUserAction.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.palette_cheddar_dark)));
                } else {
                    holder.buttonUserAction.setText("UnFollow");
                }


            } else {

                if (item.follow.equals("0")) {
                    holder.buttonUserAction.setText("Follow");
                    holder.buttonUserAction.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.palette_cheddar_dark)));
                } else {
                    holder.buttonUserAction.setText("Friends");
                }
            }

        } else {
            holder.buttonUserAction.setVisibility(View.GONE);
        }

        holder.bind(i, datalist.get(i), listener);

    }

}