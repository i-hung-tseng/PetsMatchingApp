package com.example.petsmatchingapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.petsmatchingapp.databinding.*
import com.example.petsmatchingapp.model.Message
import com.example.petsmatchingapp.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.message_item_list_me.view.*
import kotlinx.android.synthetic.main.message_list_item_other.view.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class ChatRoomAdapter() : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {


    private val MESSAGE_TYPE_LEFT = 0
    private val IMAGE_TYPE_LEFT = 1
    private val MESSAGE_TYPE_RIGHT = 2
    private val IMAGE_TYPE_RIGHT = 3
    private val MESSAGE_TYPE_TIME = 4


    companion object DiffCallback : DiffUtil.ItemCallback<Message>() {

        override fun
                areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            Timber.d("DiffUtil測試 areItems ${oldItem === newItem}")
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            Timber.d("DiffUtil測試 contents ${oldItem == newItem}")
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            MESSAGE_TYPE_RIGHT -> {
                MyMessageViewHolder(MessageItemListMeBinding.inflate(LayoutInflater.from(parent.context)))
            }
            MESSAGE_TYPE_LEFT -> {
                OtherMessageViewHolder(
                    MessageListItemOtherBinding.inflate(LayoutInflater.from(parent.context)))
            }
            IMAGE_TYPE_RIGHT -> {
                MyImageViewHolder(
                   MessageItemListImageMeBinding.inflate(LayoutInflater.from(parent.context))
                )
            }
            IMAGE_TYPE_LEFT -> {
                OtherImageViewHolder(
                    MessageItemListImageOtherBinding.inflate(LayoutInflater.from(parent.context))
                )
            }
            else -> {
                TimeViewHolder(ChatRoomTimeBinding.inflate(LayoutInflater.from(parent.context)))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = getItem(position)
        when(holder){
            is MyMessageViewHolder -> {holder.bind(model)}
            is MyImageViewHolder -> {holder.bind(model)}
            is OtherMessageViewHolder -> {holder.bind(model)}
            is OtherImageViewHolder -> {holder.bind(model)}
            is TimeViewHolder -> {holder.bind(model)}
            else -> {Timber.d(" onBindViewHolder 錯誤")}
        }
    }


    class MyMessageViewHolder(val binding: MessageItemListMeBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(item: Message) {
            binding.message = item
            item.time?.let {
                binding.tvItemMessageMeTime.text = Constant.getDataForm(it)
            }
            binding.executePendingBindings()
        }
    }
    class OtherMessageViewHolder(val binding: MessageListItemOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Message) {
            binding.message = item
            binding.itemMessageOtherTime.setText(item.time?.let { Constant.getDataForm(it) })
            item.send_user_image?.let {
                Constant.loadUserImage(
                    it,
                    binding.ivItemMessageOtherImage
                )
            }
            binding.executePendingBindings()
        }
    }

    class TimeViewHolder(val binding: ChatRoomTimeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message) {
            binding.tvTime.text = item.message
            binding.tvTime.alpha = 0.5F
            binding.executePendingBindings()
        }
    }

    class MyImageViewHolder(val binding: MessageItemListImageMeBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Message){
            binding.tvItemMessageMeTime.setText(item.time?.let { Constant.getDataForm(it) })

            Glide.with(binding.ivItemListMe)
                .load(item.image)
                .transform(RoundedCorners(20))
                .into(binding.ivItemListMe)
        }
    }


    class OtherImageViewHolder(val binding: MessageItemListImageOtherBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Message){
            binding.itemMessageOtherTime.setText(item.time?.let { Constant.getDataForm(it)})
            Glide.with(binding.ivListItemOtherImage)
                .load(item.image)
                .transform(RoundedCorners(20))
                .into(binding.ivListItemOtherImage)

            val mRequestOptions = RequestOptions.circleCropTransform()
            Glide.with(binding.ivItemMessageOtherImage)
                .load(item.send_user_image)
                .apply(mRequestOptions)
                .into(binding.ivItemMessageOtherImage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val id = FirebaseAuth.getInstance().currentUser?.uid
//            return when(getItem(position).send_user_id){
//                firebaseUser?.uid -> {
//                    MESSAGE_TYPE_RIGHT
//                }
//                "time" -> {
//                    MESSAGE_TYPE_TIME
//                }
//                else -> {
//                    MESSAGE_TYPE_LEFT
//                }
//            }

        val model = getItem(position)
        return when {
            model.send_user_id == id && model.image == null && model.send_user_id != "time" -> {
                MESSAGE_TYPE_RIGHT
            }
            model.send_user_id == id && model.image != null -> {
                IMAGE_TYPE_RIGHT
            }
            model.send_user_id != id && model.image == null && model.send_user_id != "time" -> {
                MESSAGE_TYPE_LEFT
            }
            model.send_user_id != id && model.image != null -> {
                IMAGE_TYPE_LEFT
            }
            else -> {
                Timber.d("時間測試 enter getType")
                MESSAGE_TYPE_TIME
            }
        }

    }
}
