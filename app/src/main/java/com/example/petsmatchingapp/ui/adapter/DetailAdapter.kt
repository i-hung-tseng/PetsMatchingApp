package com.example.petsmatchingapp.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petsmatchingapp.databinding.BannerItemLayoutBinding
import com.example.petsmatchingapp.utils.Constant
import timber.log.Timber



class DetailAdapter( private val list: List<String>
): RecyclerView.Adapter<DetailAdapter.MyViewHolder>() {




    class MyViewHolder(val binding: BannerItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return MyViewHolder(BannerItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))


    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val uri = list[position]
        Constant.loadPetImage(uri,holder.binding.bannerImage)
        Timber.d("uri list : $uri")
    }

    override fun getItemCount(): Int = list.size

}