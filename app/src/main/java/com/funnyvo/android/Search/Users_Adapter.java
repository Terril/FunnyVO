package com.funnyvo.android.Search;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.funnyvo.android.R;
import com.funnyvo.android.SimpleClasses.Adapter_Click_Listener;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/19/2019.
 */


class Users_Adapter extends RecyclerView.Adapter<Users_Adapter.CustomViewHolder> {
    public Context context;

    ArrayList<Object> datalist;
    Adapter_Click_Listener adapter_click_listener;

    public Users_Adapter(Context context, ArrayList<Object> arrayList, Adapter_Click_Listener adapter_click_listener) {
        this.context = context;
        datalist = arrayList;
        this.adapter_click_listener = adapter_click_listener;
    }

    @Override
    public Users_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_users_list, viewGroup, false);
        Users_Adapter.CustomViewHolder viewHolder = new Users_Adapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final Users_Adapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);
        Users_Model item = (Users_Model) datalist.get(i);

        if (item.profile_pic != null && !item.profile_pic.equals("")) {
            Uri uri = Uri.parse(item.profile_pic);
            holder.image.setImageURI(uri);
        }

        holder.username_txt.setText(item.first_name + " " + item.last_name);
        holder.description_txt.setVisibility(View.GONE);
        holder.video_count.setText(item.videos + " (Videos)");
        holder.bind(i, item, adapter_click_listener);

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView image;
        TextView username_txt, description_txt, video_count;

        public CustomViewHolder(View view) {
            super(view);

            image = view.findViewById(R.id.image);
            username_txt = view.findViewById(R.id.username_txt);
            video_count = view.findViewById(R.id.video_count_txt);
            description_txt = view.findViewById(R.id.description_txt);


        }

        public void bind(final int pos, final Object item, final Adapter_Click_Listener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });


        }


    }


}

