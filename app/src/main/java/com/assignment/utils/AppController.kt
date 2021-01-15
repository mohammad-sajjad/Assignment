package com.assignment.utils

import android.content.Context
import com.pixel.saloonapp.app.AppContextGoneException
import java.lang.ref.WeakReference




class AppController: AppControllerContract {

    companion object {
        private var INSTANCE: AppController? = null
        private var context: WeakReference<Context>? = null

        fun init(context: WeakReference<Context>): AppController? {
            Companion.context = context
            INSTANCE =
                AppController()
            return INSTANCE
        }

        fun get(): AppController = INSTANCE
            ?: throw AppContextGoneException()
    }


    override fun getProperContext(): Context {
        val localContext = context?.get()
        return localContext ?: throw AppContextGoneException()
    }

    override fun getSavedUsername() = SharedPrefsManager.getUserName()
    override fun getImage(): String {
        return SharedPrefsManager.getUserImage()
    }

    override fun getEmail(): String {
        return SharedPrefsManager.getUserEmail()
    }

}