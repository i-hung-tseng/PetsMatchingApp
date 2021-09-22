package com.example.petsmatchingapp.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.petsmatchingapp.R
import java.net.URL

object Constant {

    const val USER: String = "user"


    fun loadUserImage(url: Any, v:ImageView){

        val mRequestOptions = RequestOptions.circleCropTransform()
        Glide.with(v)
            .load(url)
            .apply(mRequestOptions)
            .placeholder(R.drawable.placeholder)
            .into(v)
    }

}