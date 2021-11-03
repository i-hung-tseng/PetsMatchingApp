package com.example.petsmatchingapp.ui

import android.app.Application
import com.example.petsmatchingapp.BuildConfig
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.ChatViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class MyApp: Application() {

    override fun onCreate() {

        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
        Firebase.database.setPersistenceEnabled(false)
        Timber.d(this.toString())
        super.onCreate()
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
        subscribeToTopic()
        setupKoin()
    }


    private fun setupKoin(){

        val viewModelModule = module {
            viewModel { AccountViewModel() }
            viewModel { MatchingViewModel() }
            viewModel { ChatViewModel() }
        }

        startKoin {
            this@MyApp
            modules(
                viewModelModule
            )
        }
    }

    private fun subscribeToTopic(){
        Firebase.messaging.subscribeToTopic("advertisement")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful){
                    Timber.d("訂閱不成功")
                }else{
                    Timber.d("訂閱成功")
                }
            }
    }
}