package com.example.petsmatchingapp.ui.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import com.example.petsmatchingapp.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        @Suppress("DEPRECATION")
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
            val controller =window.insetsController
            controller?.hide(WindowManager.LayoutParams.TYPE_STATUS_BAR)
        }else{
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        Handler(Looper.getMainLooper()).postDelayed(
            {
                startActivity(Intent(this@SplashActivity,AccountActivity::class.java))
                finish()
            },
            2000
        )
        val font = Typeface.createFromAsset(assets,"jfopenhuninn.ttf")

    }
}