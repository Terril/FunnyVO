package com.funnyvo.android.soundlists.favouritesounds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.soundlists.Sounds;

import java.util.ArrayList;

class DeviceSoundAdapter extends RecyclerView.Adapter<DeviceSoundAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<Sounds> datalist;

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Sounds item);
    }

    public void updateList(ArrayList<Sounds> searchedSounds) {
        datalist = searchedSounds;
        notifyDataSetChanged();
    }

    public DeviceSoundAdapter.OnItemClickListener listener;

    public DeviceSoundAdapter(Context context, ArrayList<Sounds> arrayList, DeviceSoundAdapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public DeviceSoundAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sound_layout, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(Variables.screen_width - 50, RecyclerView.LayoutParams.WRAP_CONTENT));
        DeviceSoundAdapter.CustomViewHolder viewHolder = new DeviceSoundAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final DeviceSoundAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        Sounds item = datalist.get(i);
        try {

            holder.sound_name.setText(item.sound_name);
            holder.description_txt.setText(item.description);

            if (item.thum != null && !item.thum.equals("")) {
                Glide.with(context)
                        .load(item.thum)
                        .centerCrop()
                        .placeholder(R.color.colorAccent)
                        .into(holder.sound_image);
            }
            holder.bind(i, datalist.get(i), listener);
        } catch (Exception e) {

        }
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageButton btnSoundSelected;
        TextView sound_name, description_txt;
        ImageView sound_image;

        public CustomViewHolder(View view) {
            super(view);
            btnSoundSelected = view.findViewById(R.id.btnSoundSelected);
            sound_name = view.findViewById(R.id.sound_name);
            description_txt = view.findViewById(R.id.description_txt);
            sound_image = view.findViewById(R.id.sound_image);

        }

        public void bind(final int pos, final Sounds item, final DeviceSoundAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

            btnSoundSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });
        }
    }
}

