package com.example.petsmatchingapp.model

import com.google.firebase.Timestamp
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.HashMap

data class Message(
    val user_name: String? = null,
    val message: String? = null,
    val time: Any? = null,
    val send_user_id: String? = null,
    val send_user_name: String? = null,
    val accept_user_id: String? = null,
    val accept_user_name: String? = null,
    val accept_user_image: String? = null,
    val send_user_image: String? = null,
    val image: String? = null,
)