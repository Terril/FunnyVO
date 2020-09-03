package com.funnyvo.android.videorecording.stickers

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.funnyvo.android.R
import com.funnyvo.android.extensions.inflate
// import com.funnyvo.android.videorecording.data.RecordingFilters
import kotlinx.android.synthetic.main.view_stickers.view.*

//class StickerAdapter(private val filters: List<RecordingFilters>, private val clickListener: OnItemClickListener) : RecyclerView.Adapter<StickerAdapter.PhotoHolder>() {
//
//    interface OnItemClickListener {
//        fun onItemClick(item: RecordingFilters?)
//    }
//
//    class PhotoHolder(v: View) : RecyclerView.ViewHolder(v) {
//        //2
//        private var view: View = v
//        private var photoUrl: String? = null
//
//        companion object {
//            //5
//            private val PHOTO_KEY = "PHOTO"
//        }
//
//        fun bindPhoto(recordingFilters: RecordingFilters, listener: OnItemClickListener) {
//            this.photoUrl = recordingFilters.main_image
//            Glide.with(view.context)
//                    .load(recordingFilters.main_image)
//                    .centerCrop()
//                    .apply(RequestOptions().override(40, 40).transform(CenterCrop(), RoundedCorners(5)))
//                    .into(view.imvSticker)
//            view.txtStickerTitle.text = recordingFilters.name
//
//            view.setOnClickListener {
//                listener.onItemClick(recordingFilters)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
//        val inflatedView = parent.inflate(R.layout.view_stickers, false)
//        return PhotoHolder(inflatedView)
//    }
//
//    override fun getItemCount(): Int = filters.size
//
//    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
//        val itemPhoto = filters[position]
//        holder.bindPhoto(itemPhoto, clickListener)
//    }
//}