package com.peluqueriacanina.app

import android.app.Application
import com.peluqueriacanina.app.data.AppDatabase

class PeluqueriaApp : Application() {
    
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: PeluqueriaApp
            private set
    }
}
