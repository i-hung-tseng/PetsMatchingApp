package com.example.petsmatchingapp.ui.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.ChatRoomTimeBinding
import com.example.petsmatchingapp.databinding.GalleryPhotoItemViewBinding
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.model.SelectedUri
import kotlinx.android.synthetic.main.fragment_chat_room.view.*
import kotlinx.android.synthetic.main.gallery_photo_item_view.view.*
import timber.log.Timber

class GalleryAdapter(val context: Context) :

    ListAdapter<SelectedUri, GalleryAdapter.GalleryViewHolder>(DiffCallBack) {

    var clickEvent:(selectedUri: SelectedUri) -> Unit = {  }


    class GalleryViewHolder(val binding: GalleryPhotoItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SelectedUri) {
                Glide.with(binding.ivGalleryImage)
                    .load(item.uri)
                    .fitCenter()
                    .into(binding.ivGalleryImage)
                if (item.selected){
                    binding.ivSelectedPhoto.visibility = View.VISIBLE
                }else{
                    binding.ivSelectedPhoto.visibility = View.GONE
                }
        }


    }
    companion object DiffCallBack : DiffUtil.ItemCallback<SelectedUri>() {
        override fun areContentsTheSame(oldItem: SelectedUri, newItem: SelectedUri): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: SelectedUri, newItem: SelectedUri): Boolean {
            return oldItem === newItem
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
       return GalleryViewHolder(GalleryPhotoItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)
        holder.itemView.setOnClickListener {
                model.selected = !model.selected
//            model.selected = !model.selected
//            if (model.selected){
//                holder.itemView.iv_selected_photo.visibility = View.VISIBLE
//            }else{
//                holder.itemView.iv_selected_photo.visibility = View.GONE
//            }
            clickEvent(model)
            holder.bind(model)
//            it.setBackgroundColor(ContextCompat.getColor(context,R.color.gallery_selected_color))
//            holder.itemView.iv_selected_photo.visibility = View.VISIBLE
        }

    }
}