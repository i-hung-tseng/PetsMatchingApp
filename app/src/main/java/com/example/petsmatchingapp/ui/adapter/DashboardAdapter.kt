package com.example.petsmatchingapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.DashboardInvitationItemListBinding
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.fragment.DashboardFragment
import com.example.petsmatchingapp.utils.Constant

class DashboardAdapter(private val fragment: DashboardFragment): ListAdapter<Invitation, DashboardAdapter.DashboardViewHolder>(DiffCallback){




    class DashboardViewHolder(val binding: DashboardInvitationItemListBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Invitation){
            binding.invitation = item
            binding.executePendingBindings()
            Constant.loadPetImage(item.pet_image,binding.ivDashboardInvitationItemListImage)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val binding: DashboardInvitationItemListBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.dashboard_invitation_item_list,parent,false)
        return DashboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {

            val model = getItem(position)
            holder.bind(model)
            holder.itemView.setOnClickListener{
                fragment.addSelectedInvitationToViewModel(model)
            }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<Invitation>(){
        override fun areItemsTheSame(oldItem: Invitation, newItem: Invitation): Boolean {
            return oldItem === newItem

        }

        override fun areContentsTheSame(oldItem: Invitation, newItem: Invitation): Boolean {
            return oldItem.id == oldItem.id
        }
    }

}