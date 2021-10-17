package com.example.petsmatchingapp.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.petsmatchingapp.R
import java.net.URL

object Constant {

    const val USER: String = "user"
    const val MAN: String = "man"
    const val FEMALE: String = "female"
    const val USER_IMAGE: String = "user_image"
    const val REQUEST_CODE_READ = 1001
    const val NAME: String = "name"
    const val AREA: String = "area"
    const val GENDER: String = "gender"
    const val IMAGE: String = "image"
    const val PROFILE_COMPLETED: String = "profileCompleted"

    const val PET_IMAGE: String = "pet_image"

    const val INVITATION: String = "invitation"
    const val ID: String = "id"
    const val PET_TYPE = "pet_type"

    const val MESSAGE: String = "message"

    const val RESULT_SORT_UPDATE_DAY = "result_sort_update_day"
    const val RESULT_SORT_INVITATION_DAY = "result_sort_invitation_day"

    const val UPDATE_TIME = "update_time"
    const val DATE_TIME = "date_time"

    const val LAST_MESSAGE = "last_message"
    const val CHAT_IMAGE = "chat_image"


    fun loadUserImage(url: Any, v:ImageView){

        val mRequestOptions = RequestOptions.circleCropTransform()
        Glide.with(v)
            .load(url)
            .apply(mRequestOptions)
            .placeholder(R.drawable.placeholder)
            .into(v)
    }


    fun loadPetImage(url: Any, v:ImageView){

        Glide.with(v)
            .load(url)
            .placeholder(R.drawable.placeholder)
            .into(v)
    }

    fun getFileExtension(activity: Activity, uri: Uri): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri))
    }

}