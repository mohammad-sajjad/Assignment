package com.assignment.utils

import android.app.Application
import java.lang.ref.WeakReference


/**
 * Created by Rishabh Shukla
 * on 2/12/20
 * Email : rishabh1450@gmail.com
 */
 
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AppController.init(
            WeakReference(this)
        )
    }
}