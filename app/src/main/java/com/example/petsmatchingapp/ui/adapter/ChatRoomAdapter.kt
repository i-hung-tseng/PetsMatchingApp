package com.example.petsmatchingapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.MessageItemListMeBinding
import com.example.petsmatchingapp.databinding.MessageListItemOtherBinding
import com.example.petsmatchingapp.model.Message
import com.example.petsmatchingapp.utils.Constant
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.synthetic.main.message_list_item_other.view.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class ChatRoomAdapter(): ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {




    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    private var firebaseUser: FirebaseUser? = null

    companion object DiffCallback: DiffUtil.ItemCallback<Message>(){
        override fun areItemsTheSame(oldItem: Message, newItem: Message ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Message , newItem: Message ): Boolean {

            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == MESSAGE_TYPE_RIGHT){

            return MyMessageViewHolder(MessageItemListMeBinding.inflate(LayoutInflater.from(parent.context)))
        }else{
            return OtherMessageViewHolder(MessageListItemOtherBinding.inflate(LayoutInflater.from(parent.context)))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = getItem(position)
        when(holder){
            is  MyMessageViewHolder ->{
                holder.bind(model)


            }
            is  OtherMessageViewHolder -> {
                holder.bind(model)

            }
        }
    }




    class MyMessageViewHolder(val binding: MessageItemListMeBinding):RecyclerView.ViewHolder(binding.root){

        fun getDate(timestamp: Any): String{

            val mFormat = "HH:mm"
            Timber.d("timestamp: $timestamp")
            val sdf = SimpleDateFormat(mFormat, Locale.getDefault())
            return sdf.format(timestamp)
        }

        fun bind(item:Message){
            binding.message = item
            item.time?.let {
                binding.tvItemMessageMeTime.text = getDate(it)
            }

            binding.executePendingBindings()
        }
    }

    class OtherMessageViewHolder(val binding: MessageListItemOtherBinding): RecyclerView.ViewHolder(binding.root){

        fun getDate(timestamp: Any): String{
            val mFormat = "HH:mm"
            val sdf = SimpleDateFormat(mFormat, Locale.getDefault())
            return sdf.format(timestamp)
        }

        fun bind(item: Message){
            binding.message = item
            binding.itemMessageOtherTime.setText(getDate(item.time!!))
            item.send_user_image?.let { Constant.loadUserImage(it,binding.ivItemMessageOtherImage) }
            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser?.uid == getItem(position).send_user_id){
            return MESSAGE_TYPE_RIGHT
        }else{
            return MESSAGE_TYPE_LEFT
        }
    }

}