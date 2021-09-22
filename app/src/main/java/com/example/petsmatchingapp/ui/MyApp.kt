package com.example.petsmatchingapp.ui

import android.app.Application
import com.example.petsmatchingapp.BuildConfig
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }

        setupKoin()
    }

    private fun setupKoin(){

        val viewModelModule = module {
            viewModel { AccountViewModel() }
            viewModel { MatchingViewModel() }
        }

        startKoin {
            this@MyApp
            modules(
                viewModelModule
            )
        }
    }
}