package com.example.petsmatchingapp.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.model.LastMessage
import com.example.petsmatchingapp.model.Message
import com.example.petsmatchingapp.ui.fragment.AddInvitationFragment
import com.example.petsmatchingapp.ui.fragment.ChatRoomFragment
import com.example.petsmatchingapp.utils.Constant
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel:ViewModel() {



    private val _messageList = MutableLiveData<List<Message>>()
    val messageList: LiveData<List<Message>>
    get() = _messageList

    private val _lastMessageList = MutableLiveData<List<LastMessage>>()
    val lastMessageList: LiveData<List<LastMessage>>
    get() = _lastMessageList

    private val _selectedChatRoomUserDetail = MutableLiveData<LastMessage>()
    val selectedChatRoomUserDetail: LiveData<LastMessage>
    get() = _selectedChatRoomUserDetail

    private val _fromDetail = MutableLiveData<Boolean>()
    val fromDetail: LiveData<Boolean>
    get() = _fromDetail


    private val _imageFail = MutableLiveData<String>()
    val imageFail: LiveData<String>
    get() = _imageFail


    fun sendMessage(message: Message){


        FirebaseDatabase.getInstance().reference.child(message.send_user_id!!).child(message.accept_user_id!!).push().setValue(message)
            .addOnSuccessListener {
                Timber.d("成功上至 send_user那邊囉")
            }
            .addOnFailureListener {
                Timber.d("Fail至 send_user cause$it")
            }

        FirebaseDatabase.getInstance().reference.child(message.accept_user_id).child(message.send_user_id).push().setValue(message)
            .addOnSuccessListener {
                Timber.d("成功上至 accept_user那邊囉")
            }
            .addOnFailureListener {
                Timber.d("Fail至 accept_user cause$it")
            }
    }


    fun saveLastMessage(message: Message){


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

        lastMessageRef.child(message.send_user_id!!).child(message.accept_user_id!!).setValue(lastMessageForAccept)


        lastMessageRef.child(message.accept_user_id!!).child(message.send_user_id!!).setValue(lastMessageForSend)

    }


    fun messageValueListener(currentUID: String, invitationUID: String){

        val ref = FirebaseDatabase.getInstance().reference.child(currentUID).child(invitationUID)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                for (i in snapshot.children){
                    val message = i.getValue(Message::class.java)
                    if (message != null) {

                        val format = "yyyy-MM-dd"
                        val sdf = SimpleDateFormat(format, Locale.getDefault())
                        val updateTime = sdf.format(message.time)
                        Timber.d("updateTime $updateTime")

                        list.add(message)

                    }
                }
                _messageList.postValue(list.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }



    fun getLastMessage(user_id: String){

        FirebaseDatabase.getInstance().reference.child(Constant.LAST_MESSAGE).child(user_id)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newList: MutableList<LastMessage> = mutableListOf()
                    for (eachPerson in snapshot.children){
                        val lastMessage = eachPerson.getValue(LastMessage::class.java)
                        lastMessage?.let {
                            newList.add(it)
                        }

                    }
                    newList.sortByDescending{ it -> it.send_time}
                    _lastMessageList.postValue(newList)
                    Timber.d("newList: $newList")

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun saveSelectedChatRoomUserDetail(lastMessage: LastMessage){
        _selectedChatRoomUserDetail.postValue(lastMessage)
    }

    fun setFromDetailOrNot(boolean: Boolean){
        _fromDetail.postValue(boolean)
    }

    fun saveImageToFireStorage(type: String, uri: Uri,message: Message) {


            val sdf: StorageReference = FirebaseStorage.getInstance().reference.child(
                Constant.CHAT_IMAGE + "_" + System.currentTimeMillis() + "_" + type
                )


            sdf.putFile(uri)
                .addOnSuccessListener { it ->
                    it.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { Uri ->
                            val newMessage =  Message(
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
                            saveLastMessage(newMessage)
                        }
                        ?.addOnFailureListener {
                            _imageFail.postValue(it.toString())
                        }


                }
                .addOnFailureListener {
                    _imageFail.postValue(it.toString())
                }
        }

    fun resetImageFail(){
        _imageFail.postValue(null)
    }



    }

