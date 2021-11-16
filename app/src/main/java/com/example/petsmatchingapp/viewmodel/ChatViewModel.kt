package com.example.petsmatchingapp.viewmodel

import android.app.Activity
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petsmatchingapp.model.*
import com.example.petsmatchingapp.ui.fragment.AddInvitationFragment
import com.example.petsmatchingapp.ui.fragment.ChatRoomFragment
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.utils.SingleLiveEvent
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatViewModel : ViewModel() {


    private val _messageList = MutableLiveData<List<Message>>()
    val messageList: LiveData<List<Message>> = _messageList

    private val _lastMessageList = MutableLiveData<List<LastMessage>>()
    val lastMessageList: LiveData<List<LastMessage>> = _lastMessageList

    private val _selectedChatRoomUserDetail = MutableLiveData<LastMessage>()
    val selectedChatRoomUserDetail: LiveData<LastMessage> = _selectedChatRoomUserDetail

    private val _fromDetail = MutableLiveData<Boolean>()
    val fromDetail: LiveData<Boolean> = _fromDetail


    private val _imageFail = MutableLiveData<String>()
    val imageFail: LiveData<String> = _imageFail


    private val _messageState = SingleLiveEvent<Boolean>()
    val messageState: SingleLiveEvent<Boolean> = _messageState


    init {
        getLastMessage()
    }

    fun sendMessage(message: Message) {


        FirebaseDatabase.getInstance().reference.child(message.send_user_id!!)
            .child(message.accept_user_id!!).push().setValue(message)
            .addOnSuccessListener {
                saveLastMessage(message)
            }
            .addOnFailureListener {
                _messageState.postValue(false)

            }

        FirebaseDatabase.getInstance().reference.child(message.accept_user_id)
            .child(message.send_user_id).push().setValue(message)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }


    private fun saveLastMessage(message: Message) {


        val lastMessageForAccept = LastMessage(
            message = message.message!!,
            display_name = message.accept_user_name!!,
            display_image = message.accept_user_image!!,
            display_id = message.accept_user_id!!,
            send_time = System.currentTimeMillis()
        )

        val lastMessageForSend = LastMessage(
            message = message.message!!,
            display_name = message.send_user_name!!,
            display_image = message.send_user_image!!,
            display_id = message.send_user_id!!,
            send_time = System.currentTimeMillis()

        )
        val lastMessageRef = FirebaseDatabase.getInstance().reference.child(Constant.LAST_MESSAGE)
//
//        val user = User(id = message.accept_user_id,image = message.accept_user_image,name = message.accept_user_name)
//        val user2 = User(id = message.send_user_id,image = message.send_user_image,name = message.send_user_name)
//        lastMessageRef.child(message.send_user_id).child("user_detail").setValue(user)
//        lastMessageRef.child(message.accept_user_id).child("user_detail").setValue(user2)


        lastMessageRef.child(message.send_user_id).child(message.accept_user_id)
            .setValue(lastMessageForAccept)

        lastMessageRef.child(message.accept_user_id).child(message.send_user_id)
            .setValue(lastMessageForSend)



    }


    fun messageValueListener(currentUID: String, invitationUID: String) {

        val ref = FirebaseDatabase.getInstance().reference.child(currentUID).child(invitationUID)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                for (i in snapshot.children) {
                    val message = i.getValue(Message::class.java)
                    if (message != null) {
                        val updateTime =
                            SimpleDateFormat("MM-dd", Locale.getDefault()).format(message.time)
                        val timeMessage = Message(message = updateTime, send_user_id = "time")
                        when {
                            list.isEmpty() -> {
                                list.add(timeMessage)
                                list.add(message)
                            }
                            updateTime != SimpleDateFormat(
                                "MM-dd",
                                Locale.getDefault()
                            ).format(list.last().time) -> {
                                list.add(timeMessage)
                                list.add(message)
                                Timber.d(
                                    "chat測試time $updateTime last ${
                                        SimpleDateFormat(
                                            "MM-dd",
                                            Locale.getDefault()
                                        ).format(list.last().time)
                                    }"
                                )
                            }

                            else -> {
                                list.add(message)
                            }
                        }


                    }
                }
                Timber.d("chatTest list.size = ${list.size} list = $list")
                _messageList.postValue(list.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.d("onCancelled error: $error")
            }
        })
    }


    private fun getLastMessage() {

        CurrentUser.currentUser?.uid?.let {
            FirebaseDatabase.getInstance().reference.child(Constant.LAST_MESSAGE).child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newList: MutableList<LastMessage> = mutableListOf()
                        for (eachPerson in snapshot.children) {
                            val lastMessage = eachPerson.getValue(LastMessage::class.java)
                            lastMessage?.let {
                                newList.add(it)
                            }

                        }
                        newList.sortByDescending { it -> it.send_time }
                        _lastMessageList.postValue(newList)
                        Timber.d("newList: $newList")

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }


    fun saveSelectedChatRoomUserDetail(lastMessage: LastMessage) {
        _selectedChatRoomUserDetail.postValue(lastMessage)
    }

    fun setFromDetailOrNot(boolean: Boolean) {
        _fromDetail.postValue(boolean)
    }

    fun saveImageToFireStorage(type: String, uri: Uri, message: Message) {


        val sdf: StorageReference = FirebaseStorage.getInstance().reference.child(
            Constant.CHAT_IMAGE + "_" + System.currentTimeMillis() + "_" + type
        )


        sdf.putFile(uri)
            .addOnSuccessListener { it ->
                it.metadata?.reference?.downloadUrl
                    ?.addOnSuccessListener { Uri ->
                        val newMessage = Message(
                            user_name = message.user_name,
                            message = message.message,
                            send_user_id = message.send_user_id,
                            send_user_image = message.send_user_image,
                            send_user_name = message.send_user_name,
                            accept_user_name = message.accept_user_name,
                            accept_user_image = message.accept_user_image,
                            accept_user_id = message.accept_user_id,
                            time = ServerValue.TIMESTAMP,
                            image = Uri.toString()
                        )
                        sendMessage(newMessage)
                        _messageState.postValue(true)
                    }
                    ?.addOnFailureListener {
                        _imageFail.postValue(it.toString())
                    }


            }
            .addOnFailureListener {
                _imageFail.postValue(it.toString())
            }
    }
    fun resetMessageList(){
        _messageList.postValue(mutableListOf())
    }


}

