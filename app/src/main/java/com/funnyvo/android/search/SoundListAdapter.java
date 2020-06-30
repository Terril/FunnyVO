package com.funnyvo.android.search;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.AdapterClickListener;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.soundlists.Sounds;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/19/2019.
 */

class SoundListAdapter extends RecyclerView.Adapter<SoundListAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<Object> datalist;
    AdapterClickListener adapter_click_listener;

    public SoundListAdapter(Context context, ArrayList<Object> arrayList, AdapterClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.adapter_click_listener = listener;
    }

    @Override
    public SoundListAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sound_layout, viewGroup, false);
        SoundListAdapter.CustomViewHolder viewHolder = new SoundListAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final SoundListAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        Sounds item = (Sounds) datalist.get(i);

        try {
            holder.sound_name.setText(item.sound_name);
            holder.description_txt.setText(item.description);

            if (item.thum != null && !item.thum.equals("")) {
                Log.d(Variables.tag, item.thum);
                Uri uri = Uri.parse(item.thum);
                holder.sound_image.setImageURI(uri);
            }


            if (item.fav.equals("1"))
                holder.fav_btn.setImageDrawable(context.getDrawable(R.drawable.ic_my_favourite));
            else
                holder.fav_btn.setImageDrawable(context.getDrawable(R.drawable.ic_my_un_favourite));

            holder.bind(i, item, adapter_click_listener);


        } catch (Exception e) {

        }

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageButton done, fav_btn;
        TextView sound_name, description_txt;
        SimpleDraweeView sound_image;

        public CustomViewHolder(View view) {
            super(view);
            done = view.findViewById(R.id.done);
            fav_btn = view.findViewById(R.id.fav_btn);


            sound_name = view.findViewById(R.id.sound_name);
            description_txt = view.findViewById(R.id.description_txt);
            sound_image = view.findViewById(R.id.sound_image);

        }

        public void bind(final int pos, final Sounds item, final AdapterClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

            fav_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

        }


    }


}

