package com.example.petsmatchingapp.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petsmatchingapp.databinding.BannerItemLayoutBinding
import com.example.petsmatchingapp.utils.Constant
import kotlinx.android.synthetic.main.banner_item_layout.view.*
import timber.log.Timber

class MultiplePhotoAdapter( private val list: List<Uri>
): RecyclerView.Adapter<MultiplePhotoAdapter.MyViewHolder>() {




    class MyViewHolder(val binding: BannerItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return MyViewHolder(BannerItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))


    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val uri = list[position]
        Timber.d("測試 uri: $uri")
        Constant.loadPetImage(uri,holder.binding.bannerImage)
    }

    override fun getItemCount(): Int = list.size

}
