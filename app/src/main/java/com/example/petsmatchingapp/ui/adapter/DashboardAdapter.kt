package com.example.petsmatchingapp.ui.adapter

import android.util.Log.d
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.DashboardInvitationItemListBinding
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.fragment.DashboardFragment
import com.example.petsmatchingapp.utils.Constant
import kotlinx.android.synthetic.main.dashboard_invitation_item_list.view.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class DashboardAdapter(): ListAdapter<Invitation, DashboardAdapter.DashboardViewHolder>(DiffCallback){


    var clickEvent:(invitation: Invitation) -> Unit = { }

    class DashboardViewHolder(val binding: DashboardInvitationItemListBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Invitation){
            binding.invitation = item
            val dateTime = SimpleDateFormat( "yyyy-MM-dd", Locale.getDefault()).format(item.date_time?.toDate()?.time)
            val time = binding.root.context.resources.getString(R.string.time_form,dateTime)
            binding.tvDashboardInvitationItemListDateTime.text = time
            binding.executePendingBindings()
            item.photoUriList?.get(0)?.let { Constant.loadPetImage(it,binding.ivDashboardInvitationItemListImage) }

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
            ViewCompat.setTransitionName(holder.itemView.tv_dashboard_invitation_item_list_date_time,"image")
            holder.itemView.setOnClickListener{
//                fragment.addSelectedInvitationToViewModel(model,holder.itemView.iv_dashboard_invitation_item_list_image)
                clickEvent(model)
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