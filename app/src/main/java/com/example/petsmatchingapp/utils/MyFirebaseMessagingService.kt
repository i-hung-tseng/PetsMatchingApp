package com.example.petsmatchingapp.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import timber.log.Timber

class MyFirebaseMessagingService: FirebaseMessagingService(){




    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {

        if (p0.notification != null){

        }
        super.onMessageReceived(p0)
    }

}