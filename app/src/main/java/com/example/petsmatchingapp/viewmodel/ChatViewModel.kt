package com.example.petsmatchingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.model.LastMessage
import com.example.petsmatchingapp.model.Message
import com.example.petsmatchingapp.ui.fragment.ChatRoomFragment
import com.example.petsmatchingapp.utils.Constant
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber

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


    fun saveLastMessage(message: Message,time: Long){


        val lastMessageForAccept = LastMessage(
            message = message.message!!,
            display_name = message.accept_user_name!!,
            display_image = message.accept_user_image!!,
            display_id = message.accept_user_id!!,
            send_time = time
        )

        val lastMessageForSend = LastMessage(
            message = message.message!!,
            display_name = message.send_user_name!!,
            display_image = message.send_user_image!!,
            display_id = message.send_user_id!!,
            send_time = time
        )
        val lastMessageRef = FirebaseDatabase.getInstance().reference.child(Constant.LAST_MESSAGE)

        lastMessageRef.child(message.send_user_id!!).child(message.accept_user_id!!).setValue(lastMessageForAccept)


        lastMessageRef.child(message.accept_user_id!!).child(message.send_user_id!!).setValue(lastMessageForSend)

    }


    fun messageValueListener(fragment: ChatRoomFragment, currentUID: String, invitationUID: String){

        val ref = FirebaseDatabase.getInstance().reference.child(currentUID).child(invitationUID)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                for (i in snapshot.children){
                    val message = i.getValue(Message::class.java)
                    if (message != null) {
                        list.add(message)
                        Timber.d("i:$i messaage:$message")
                    }
                }
                _messageList.postValue(list.reversed())
//                fragment.setPosition(list.size)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun saveDateInApp(){
        Firebase.database.setPersistenceEnabled(true)
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

}