package com.example.petsmatchingapp.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val image: String = "",
    val gender: String = "",
    val area: String =  "",
    val profileCompleted: Boolean = false
)

object CurrentUser{
    var currentUser: FirebaseUser? = null
}