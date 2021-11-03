package com.example.petsmatchingapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.Resource
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.HomeInvitationItemListBinding
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.fragment.HomeFragment
import com.example.petsmatchingapp.utils.Constant
import java.text.SimpleDateFormat
import java.util.*

class HomeAdapter(): ListAdapter<Invitation, HomeAdapter.HomeViewHolder>(DiffCallback) {


    var clickItemViewEvent:(invitation: Invitation) -> Unit = { }
    var clickGarbageEvent:(id: String) -> Unit = { }


    companion object DiffCallback: DiffUtil.ItemCallback<Invitation>(){
        override fun areItemsTheSame(oldItem: Invitation, newItem: Invitation): Boolean {
            return oldItem === newItem

        }

        override fun areContentsTheSame(oldItem: Invitation, newItem: Invitation): Boolean {
            return oldItem.id == oldItem.id
        }
    }



    class HomeViewHolder(val binding: HomeInvitationItemListBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Invitation){
            binding.invitation = item
            val stampToTime = item.date_time?.toDate()?.time
            val time = binding.root.context.resources.getString(R.string.time_form,SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(stampToTime))
//            val time = "時間: " + SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(stampToTime)
            binding.tvHomeInvitationItemListDateTime.text = time
            item.photoUriList?.get(0)?.let { Constant.loadPetImage(it,binding.ivHomeInvitationItemListImage) }
            binding.executePendingBindings()

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(HomeInvitationItemListBinding.inflate(LayoutInflater.from(parent.context)))


    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)
        holder.binding.ivHomeInvitationItemListDelete.setOnClickListener{
            clickGarbageEvent(model.id)
        }
        holder.itemView.setOnClickListener{
            clickItemViewEvent(model)
        }

    }
}