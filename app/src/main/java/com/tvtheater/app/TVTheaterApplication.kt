package com.tvtheater.app

import android.app.Application
import com.tvtheater.app.di.AppContainer
import com.tvtheater.app.di.DefaultAppContainer

class TVTheaterApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
    }
}
