package com.example.petsmatchingapp.model

data class Invitation(
        val id: String = "",
        val user_id: String = "",
        val pet_image: String = "",
        val pet_type: String = "",
        val pet_type_description: String = "",
        val area: String = "",
        val date_place: String = "",
        val date_time: String = "",
        val note: String = ""
)
