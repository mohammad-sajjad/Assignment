package com.assignment

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.assignment.utils.launchActivity
import com.google.firebase.FirebaseApp



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        Handler().postDelayed({
            launchActivity<LoginActivity>()
        }, 3000)

    }


}