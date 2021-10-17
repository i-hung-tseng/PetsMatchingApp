package com.example.petsmatchingapp.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import timber.log.Timber

class MyFirebaseMessagingService: FirebaseMessagingService(){




    override fun onNewToken(p0: String) {
        Timber.d("onNewToken $p0")
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {

        if (p0.notification != null){
            Timber.d("訊息是title = ${p0.notification!!.title}")
            Timber.d("訊息是body = ${p0.notification!!.body    }")

        }
        super.onMessageReceived(p0)
    }

}