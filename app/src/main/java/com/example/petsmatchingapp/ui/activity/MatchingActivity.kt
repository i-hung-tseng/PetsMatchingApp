package com.example.petsmatchingapp.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.model.CurrentUser
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.fragment.EditProfileFragment
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingRegistrar
import com.google.firebase.messaging.FirebaseMessagingService
import org.koin.androidx.viewmodel.compat.SharedViewModelCompat.sharedViewModel
import timber.log.Timber

class MatchingActivity : AppCompatActivity() {


    lateinit var navController: NavController

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navController = findNavController(R.id.nav_host_fragment)


        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )

//        // TODO: 2021/11/3 想一下可以放在哪裡
//        Firebase.firestore.collection(Constant.INVITATION).addSnapshotListener{ snapshot, e ->
//            Timber.d("測試 list 進入funtion")
//            if (e != null){
//                Timber.d("測試 e:$e ")
//                return@addSnapshotListener
//            }
//            if (snapshot != null && !snapshot.isEmpty){
//                val list = mutableListOf<Invitation>()
//                for (i in snapshot.documents){
//                    val model = i.toObject(Invitation::class.java)
//                    if (model?.user_id != FirebaseAuth.getInstance().currentUser?.uid && model != null){
//                        list.add(model)
//                    }
//                }
//                Timber.d("測試 list.size = ${list.size}")
//
//            }else{
//                Timber.d("測試 current is null ")
//            }
//        }


//        setBottomNavigationState()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.searchFragment ||
                destination.id == R.id.addInvitationFragment ||
                destination.id == R.id.profileFragment ||
                destination.id == R.id.editProfileFragment ||
                destination.id == R.id.chatRoomFragment ||
                destination.id == R.id.invitationDetailFragment
            ) {
                supportActionBar?.hide()
                findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE
            } else {
                findViewById<BottomNavigationView>(R.id.nav_view).visibility =
                    View.VISIBLE
                supportActionBar?.show()

            }
        }


//        FirebaseMessaging.getInstance().token.addOnSuccessListener {
//            Timber.d("messaging token: $it")
//        }.addOnFailureListener {
//            Timber.d("messaging token fail")
//        }


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

}