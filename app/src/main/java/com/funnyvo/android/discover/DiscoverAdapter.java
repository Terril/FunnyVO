package com.funnyvo.android.discover;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.discover.datamodel.Discover;

import java.util.ArrayList;

import static com.funnyvo.android.simpleclasses.Variables.APP_NAME;

public class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.CustomViewHolder> implements Filterable {
    public Context context;

    ArrayList<Discover> datalist;
    ArrayList<Discover> datalist_filter;

    public interface OnItemClickListener {
        void onItemClick(ArrayList<Home> video_list, int postion);
    }

    public DiscoverAdapter.OnItemClickListener listener;

    public DiscoverAdapter(Context context, ArrayList<Discover> arrayList, DiscoverAdapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        datalist_filter = arrayList;
        this.listener = listener;
    }


    @Override
    public DiscoverAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_discover_layout, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        DiscoverAdapter.CustomViewHolder viewHolder = new DiscoverAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist_filter.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        RecyclerView horizontal_reycerview;

        TextView title;

        public CustomViewHolder(View view) {
            super(view);

            horizontal_reycerview = view.findViewById(R.id.horizontal_recylerview);
            title = view.findViewById(R.id.title);
        }


    }


    @Override
    public void onBindViewHolder(final DiscoverAdapter.CustomViewHolder holder, final int i) {
        Discover item = datalist_filter.get(i);
        holder.title.setText(item.title);
        HorizontalAdapter adapter = new HorizontalAdapter(context, item.arrayList);
        holder.horizontal_reycerview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.horizontal_reycerview.setAdapter(adapter);
    }


    // that function will filter the result
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    datalist_filter = datalist;
                } else {
                    ArrayList<Discover> filteredList = new ArrayList<>();
                    for (Discover row : datalist) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.title.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    datalist_filter = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = datalist_filter;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                datalist_filter = (ArrayList<Discover>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.CustomViewHolder> {
        public Context context;
        ArrayList<Home> datalist;

        public HorizontalAdapter(Context context, ArrayList<Home> arrayList) {
            this.context = context;
            datalist = arrayList;
        }

        @Override
        public HorizontalAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_discover_horizontal_layout, viewGroup, false);
            view.setLayoutParams(new RecyclerView.LayoutParams((Variables.screen_width / 3) - 20, RecyclerView.LayoutParams.WRAP_CONTENT));
            HorizontalAdapter.CustomViewHolder viewHolder = new HorizontalAdapter.CustomViewHolder(view);
            return viewHolder;
        }

        @Override
        public int getItemCount() {
            return datalist.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            ImageView video_thumbnail;
            ImageView userThumbnail;

            public CustomViewHolder(View view) {
                super(view);
                video_thumbnail = view.findViewById(R.id.video_thumbnail);
                userThumbnail = view.findViewById(R.id.userImageInDiscovery);
            }

            public void bind(final int pos, final ArrayList<Home> datalist) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(datalist, pos);
                    }
                });
            }
        }

        @Override
        public void onBindViewHolder(final HorizontalAdapter.CustomViewHolder holder, final int i) {
            holder.setIsRecyclable(false);

            try {
                Home item = datalist.get(i);
                holder.bind(i, datalist);
                try {
                    Glide.with(context)
                            .asGif()
                            .load(item.gif)
                            .skipMemoryCache(true)
                            .thumbnail(new RequestBuilder[]{Glide
                                    .with(context)
                                    .load(item.thum)})
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)
                                    .placeholder(context.getResources().getDrawable(R.drawable.image_placeholder)).centerCrop())
                            .into(holder.video_thumbnail);

                    Log.e(APP_NAME, "Discovery : "+item.profile_pic);
                    Glide.with(context)
                            .load(item.profile_pic)
                            .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder)).centerCrop()
                            .into(holder.userThumbnail);

                } catch (Exception e) {

                }


            } catch (Exception e) {

            }
        }

    }


}