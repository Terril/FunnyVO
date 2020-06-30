package com.funnyvo.android.SoundLists.FavouriteSounds;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.funnyvo.android.R;
import com.funnyvo.android.SimpleClasses.Variables;
import com.funnyvo.android.SoundLists.Sounds_GetSet;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/19/2019.
 */

class Favourite_Sound_Adapter extends RecyclerView.Adapter<Favourite_Sound_Adapter.CustomViewHolder > {
    public Context context;

    ArrayList<Sounds_GetSet> datalist;
    public interface OnItemClickListener {
        void onItemClick(View view,int postion, Sounds_GetSet item);
    }

    public Favourite_Sound_Adapter.OnItemClickListener listener;


    public Favourite_Sound_Adapter(Context context, ArrayList<Sounds_GetSet> arrayList, Favourite_Sound_Adapter.OnItemClickListener listener) {
        this.context = context;
        datalist= arrayList;
        this.listener=listener;
    }

    @Override
    public Favourite_Sound_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sound_layout,viewGroup,false);
        view.setLayoutParams(new RecyclerView.LayoutParams(Variables.screen_width-50, RecyclerView.LayoutParams.WRAP_CONTENT));
        Favourite_Sound_Adapter.CustomViewHolder viewHolder = new Favourite_Sound_Adapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }




    @Override
    public void onBindViewHolder(final Favourite_Sound_Adapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        Sounds_GetSet item=datalist.get(i);
        try {

            holder.sound_name.setText(item.sound_name);
            holder.description_txt.setText(item.description);

            if(item.thum!=null && !item.thum.equals("")) {
                Log.d(Variables.tag,item.thum);
                Uri uri = Uri.parse(item.thum);
                holder.sound_image.setImageURI(uri);
            }
            holder.fav_btn.setImageDrawable(context.getDrawable(R.drawable.ic_my_favourite));
            holder.bind(i, datalist.get(i), listener);


        }catch (Exception e){

        }

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageButton done,fav_btn;
        TextView sound_name,description_txt;
        SimpleDraweeView sound_image;

        public CustomViewHolder(View view) {
            super(view);
            done=view.findViewById(R.id.done);
            fav_btn=view.findViewById(R.id.fav_btn);


            sound_name=view.findViewById(R.id.sound_name);
            description_txt=view.findViewById(R.id.description_txt);
            sound_image=view.findViewById(R.id.sound_image);

        }

        public void bind(final int pos , final Sounds_GetSet item, final Favourite_Sound_Adapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,item);
                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,item);
                }
            });

            fav_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,item);
                }
            });

        }


    }




}

