package com.example.petsmatchingapp.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingRegistrar
import com.google.firebase.messaging.FirebaseMessagingService
import org.koin.androidx.viewmodel.compat.SharedViewModelCompat.sharedViewModel
import timber.log.Timber

class MatchingActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )


        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Timber.d("messaging token: $it")
        }.addOnFailureListener {
            Timber.d("messaging token fail")
        }


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}