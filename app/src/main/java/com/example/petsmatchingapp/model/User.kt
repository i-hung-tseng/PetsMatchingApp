package com.example.petsmatchingapp.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val image: String = "",
    val gender: String = "",
    val profileCompleted: Boolean = false
)
