package com.example.dogbreeds

import android.app.Application

class DogBreedsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDependencies.init(this)
    }
}
