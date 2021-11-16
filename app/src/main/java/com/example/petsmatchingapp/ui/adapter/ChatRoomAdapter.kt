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
import com.example.petsmatchingapp.databinding.ChatRoomTimeBinding
import com.example.petsmatchingapp.databinding.MessageItemListMeBinding
import com.example.petsmatchingapp.databinding.MessageListItemOtherBinding
import com.example.petsmatchingapp.model.Message
import com.example.petsmatchingapp.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.message_item_list_me.view.*
import kotlinx.android.synthetic.main.message_list_item_other.view.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class ChatRoomAdapter(): ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {


    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    private val MESSAGE_TYPE_TIME = 2

    private var firebaseUser: FirebaseUser? = null


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

        return when(viewType) {
            MESSAGE_TYPE_RIGHT -> { MyMessageViewHolder(MessageItemListMeBinding.inflate(LayoutInflater.from(parent.context)))}
            MESSAGE_TYPE_LEFT -> { OtherMessageViewHolder(MessageListItemOtherBinding.inflate(LayoutInflater.from(parent.context)))}
            else -> {TimeViewHolder(ChatRoomTimeBinding.inflate(LayoutInflater.from(parent.context)))}
    } }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = getItem(position)
        when (holder) {
            is MyMessageViewHolder -> {
                Timber.d("chatTest My message ${model.message}")

                holder.bind(model)
                if (model.image != null) {
                    Glide.with(holder.binding.ivItemListMe)
                        .load(model.image)
                        .transform(RoundedCorners(50))
                        .into(holder.binding.ivItemListMe)

                    holder.itemView.iv_item_list_me.visibility = View.VISIBLE
                    holder.itemView.tv_item_message_me_message.visibility = View.GONE
                }
            }

            is TimeViewHolder -> {
                Timber.d("chatTest Time message ${model.message}")

                holder.bind(model)
            }

            is OtherMessageViewHolder -> {
                Timber.d("chatTest other message ${model.message}")

                holder.bind(model)
                if (model.image != null) {
//                    Constant.loadPetImage(model.image, holder.binding.ivListItemOtherImage)
                    Glide.with(holder.binding.ivListItemOtherImage)
                        .load(model.image)
                        .transform(RoundedCorners(50))
                        .into(holder.binding.ivListItemOtherImage)


                    holder.itemView.iv_list_item_other_image.visibility = View.VISIBLE
                    holder.itemView.item_message_other_message.visibility = View.GONE
                }

            }
        }
    }

    class MyMessageViewHolder(val binding: MessageItemListMeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun getDate(timestamp: Any): String {

            val mFormat = "HH:mm"
            Timber.d("timestamp: $timestamp")
            val sdf = SimpleDateFormat(mFormat, Locale.getDefault())
            return sdf.format(timestamp)
        }

        fun bind(item: Message) {
            binding.message = item
            item.time?.let {
                binding.tvItemMessageMeTime.text = getDate(it)
            }
            if (item.image != null) {
                Timber.d("enter chatRoomAdapter layout")
                val time = RelativeLayout.LayoutParams(binding.tvItemMessageMeTime.layoutParams)
                time.removeRule(RelativeLayout.START_OF)
                time.addRule(RelativeLayout.START_OF, binding.ivItemListMe.id)
                time.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, binding.ivItemListMe.id)
                binding.tvItemMessageMeTime.layoutParams = time
            }
            binding.executePendingBindings()

        }
    }
        class TimeViewHolder(val binding: ChatRoomTimeBinding): RecyclerView.ViewHolder(binding.root){
            fun bind(item:Message){
                binding.tvTime.text = item.message
                binding.tvTime.alpha = 0.5F
                Timber.d("測試 ")
                binding.executePendingBindings()
            }
        }

        class OtherMessageViewHolder(val binding: MessageListItemOtherBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun getDate(timestamp: Any): String {
                val mFormat = "HH:mm"
                val sdf = SimpleDateFormat(mFormat, Locale.getDefault())
                return sdf.format(timestamp)
            }

            fun bind(item: Message) {
                binding.message = item
                binding.itemMessageOtherTime.setText(getDate(item.time!!))
                item.send_user_image?.let {
                    Constant.loadUserImage(
                        it,
                        binding.ivItemMessageOtherImage
                    )
                }
                if (item.image != null){
                    val time = RelativeLayout.LayoutParams(binding.itemMessageOtherTime.layoutParams)
                    time.removeRule(RelativeLayout.END_OF)
                    time.addRule(RelativeLayout.END_OF,binding.ivListItemOtherImage.id)
                    time.addRule(RelativeLayout.ALIGN_BOTTOM,binding.ivListItemOtherImage.id)
                    binding.itemMessageOtherTime.layoutParams = time

                }
                binding.executePendingBindings()

            }
        }

        override fun getItemViewType(position: Int): Int {
            firebaseUser = FirebaseAuth.getInstance().currentUser
            return when(getItem(position).send_user_id){
                firebaseUser?.uid -> {
                    MESSAGE_TYPE_RIGHT
                }
                "time" -> {
                    MESSAGE_TYPE_TIME
                }
                else -> {
                    MESSAGE_TYPE_LEFT
                }
            }

        }


    }
