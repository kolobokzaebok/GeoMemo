package com.example.geomemo

import android.app.Application
import com.example.geomemo.database.GeoMemoDatabase

class GeoMemoApp : Application() {

    val db by lazy {
        GeoMemoDatabase.getInstance(this)
    }

}