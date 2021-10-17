package com.example.petsmatchingapp.model

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Invitation(
        val id: String = "",
        val user_id: String = "",
        val user_name: String? = "",
        val user_image: String? = "",
        val pet_type: String = "",
        val pet_type_description: String = "",
        val area: String = "",
        val date_place: String = "",
        val date_time: Timestamp? = null,
        val note: String = "",
        val update_time: Timestamp? = null ,
        val photoUriList: List<String>? = null

)
