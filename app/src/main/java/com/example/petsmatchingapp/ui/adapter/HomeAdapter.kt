package com.example.petsmatchingapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petsmatchingapp.databinding.FragmentAddInvitationBinding
import com.example.petsmatchingapp.databinding.HomeInvitationItemListBinding
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.fragment.HomeFragment
import com.example.petsmatchingapp.utils.Constant
import java.text.SimpleDateFormat
import java.util.*

class HomeAdapter(val fragment: HomeFragment): ListAdapter<Invitation, HomeAdapter.HomeViewHolder>(DiffCallback) {


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
            val mFormat = "yyyy-MM-dd"
            val sdf = SimpleDateFormat(mFormat, Locale.getDefault())
            val stampToTime = item.date_time?.toDate()?.time
            val time = "時間: " + sdf.format(stampToTime)
            binding.tvHomeInvitationItemListDateTime.text = time
            Constant.loadPetImage(item.pet_image,binding.ivHomeInvitationItemListImage)
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
            fragment.setAndShowDeleteDialog(model.id)
        }
        holder.itemView.setOnClickListener{
            fragment.addSelectedInvitationToViewModel(model)
        }

    }
}