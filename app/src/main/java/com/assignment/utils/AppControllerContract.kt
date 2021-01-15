package com.assignment.utils

import android.content.Context


interface AppControllerContract {

    companion object {
        fun get(): AppControllerContract = AppController.get()
    }

    fun getProperContext(): Context

    fun getSavedUsername(): String

    fun getImage(): String
    fun getEmail(): String

}