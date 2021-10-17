package com.example.petsmatchingapp.ui.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petsmatchingapp.databinding.LastMessageItemListBinding
import com.example.petsmatchingapp.model.LastMessage
import com.example.petsmatchingapp.model.Message
import com.example.petsmatchingapp.ui.fragment.NotificationsFragment
import com.example.petsmatchingapp.utils.Constant
import timber.log.Timber

class NotificationAdapter(private val fragment: NotificationsFragment):ListAdapter<LastMessage,NotificationAdapter.MyViewHolder>(DiffCallback) {



    companion object DiffCallback: DiffUtil.ItemCallback<LastMessage>(){
        override fun areItemsTheSame(oldItem: LastMessage, newItem: LastMessage ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: LastMessage , newItem: LastMessage ): Boolean {

            return oldItem == newItem
        }
    }

    class MyViewHolder(val binding:LastMessageItemListBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item: LastMessage){
            binding.lastMessage = item


            Constant.loadUserImage(item.display_image,binding.ivLastMessageImage)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LastMessageItemListBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)
       Timber.d("enter id = $model")
        holder.itemView.setOnClickListener {
            fragment.goChatRoom(model)
        }
    }


}