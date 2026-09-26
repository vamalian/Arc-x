package com.example

import android.app.Application
import com.example.data.ArcxDatabase
import com.example.data.ArcxRepository

class ArcxApplication : Application() {

    lateinit var database: ArcxDatabase
        private set

    lateinit var repository: ArcxRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = ArcxDatabase.getDatabase(this)
        repository = ArcxRepository(this, database)
    }
}
