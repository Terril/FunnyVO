package com.funnyvo.android.soundlists;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.facebook.drawee.view.SimpleDraweeView;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.Variables;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<SoundCategory> datalist;

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Sounds item);
    }

    public SoundAdapter.OnItemClickListener listener;

    public SoundAdapter(Context context, ArrayList<SoundCategory> arrayList, SoundAdapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.listener = listener;
    }


    @Override
    public SoundAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_category_sound_layout, viewGroup, false);
        SoundAdapter.CustomViewHolder viewHolder = new SoundAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final SoundAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);


        SoundCategory item = datalist.get(i);

        holder.title.setText(item.catagory);


        SoundItemsAdapter adapter = new SoundItemsAdapter(context, item.sound_list, new SoundItemsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Sounds item) {

                listener.onItemClick(view, postion, item);
            }
        });

        GridLayoutManager gridLayoutManager;
        if (item.sound_list.size() == 1)
            gridLayoutManager = new GridLayoutManager(context, 1);

        else if (item.sound_list.size() == 2)
            gridLayoutManager = new GridLayoutManager(context, 2);

        else
            gridLayoutManager = new GridLayoutManager(context, 3);

        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        holder.recyclerView.setLayoutManager(gridLayoutManager);
        holder.recyclerView.setAdapter(adapter);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.findSnapView(gridLayoutManager);
        snapHelper.attachToRecyclerView(holder.recyclerView);


    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        RecyclerView recyclerView;

        public CustomViewHolder(View view) {
            super(view);
            //  image=view.findViewById(R.id.image);
            title = view.findViewById(R.id.title);
            recyclerView = view.findViewById(R.id.horizontal_recylerview);


        }


    }


}


class SoundItemsAdapter extends RecyclerView.Adapter<SoundItemsAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<Sounds> datalist;

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Sounds item);
    }

    public SoundItemsAdapter.OnItemClickListener listener;


    public SoundItemsAdapter(Context context, ArrayList<Sounds> arrayList, SoundItemsAdapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public SoundItemsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sound_layout, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(Variables.screen_width - 50, RecyclerView.LayoutParams.WRAP_CONTENT));
        SoundItemsAdapter.CustomViewHolder viewHolder = new SoundItemsAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final SoundItemsAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        Sounds item = datalist.get(i);
        try {

            holder.bind(i, datalist.get(i), listener);

            holder.sound_name.setText(item.sound_name);
            holder.description_txt.setText(item.description);

            if (item.fav.equals("1"))
                holder.fav_btn.setImageDrawable(context.getDrawable(R.drawable.ic_my_favourite));
            else
                holder.fav_btn.setImageDrawable(context.getDrawable(R.drawable.ic_my_un_favourite));


            if (item.thum.equals("")) {
                item.thum = "Null";
            }


            if (item.thum != null && !item.thum.equals("")) {
                Log.d(Variables.tag, item.thum);
                Uri uri = Uri.parse(item.thum);
                holder.sound_image.setImageURI(uri);
            }


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

        public void bind(final int pos, final Sounds item, final SoundItemsAdapter.OnItemClickListener listener) {

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

