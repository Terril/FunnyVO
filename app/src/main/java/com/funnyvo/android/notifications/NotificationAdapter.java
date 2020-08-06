package com.funnyvo.android.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.funnyvo.android.R;
import com.funnyvo.android.notifications.datamodel.Notification;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<Notification> datalist;

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Notification item);
    }

    public NotificationAdapter.OnItemClickListener listener;

    public NotificationAdapter(Context context, ArrayList<Notification> arrayList, NotificationAdapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public NotificationAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        NotificationAdapter.CustomViewHolder viewHolder = new NotificationAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        CircleImageView user_image;
        TextView username, message;
        ImageButton btnWatch;

        public CustomViewHolder(View view) {
            super(view);
            user_image = view.findViewById(R.id.user_image);
            username = view.findViewById(R.id.username);
            message = view.findViewById(R.id.message);
            btnWatch = view.findViewById(R.id.btnWatch);

        }

        public void bind(final int pos, final Notification item, final NotificationAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

            btnWatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(final NotificationAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        final Notification item = datalist.get(i);
        holder.username.setText(item.username);

        if (item.profile_pic != null && !item.profile_pic.equals("")) {
            Glide.with(context)
                    .load(item.profile_pic)
                    .centerCrop()
                    .placeholder(R.drawable.profile_image_placeholder)
                    .into(holder.user_image);
        }

        if (item.type.equalsIgnoreCase("comment_video")) {
            holder.message.setText(item.first_name + " have comment on your video");
            holder.btnWatch.setVisibility(View.VISIBLE);
        } else if (item.type.equalsIgnoreCase("video_like")) {
            holder.message.setText(item.first_name + " liked your video");
            holder.btnWatch.setVisibility(View.VISIBLE);
        } else if (item.type.equalsIgnoreCase("following_you")) {
            holder.message.setText(item.first_name + " following you");
            holder.btnWatch.setVisibility(View.GONE);
        }


        holder.bind(i, datalist.get(i), listener);

    }

}