package com.labs.applabs.firebase

import android.app.Application
import com.google.firebase.FirebaseApp

/*Specific class to initialize globally*/

class App: Application(){
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
