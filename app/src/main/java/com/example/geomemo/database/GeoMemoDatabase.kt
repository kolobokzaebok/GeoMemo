package com.example.geomemo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.geomemo.dao.GeoMemoDao
import com.example.geomemo.model.GeoMemoModel

@Database(entities = [GeoMemoModel::class], version = 1)
abstract class GeoMemoDatabase : RoomDatabase() {

    abstract fun geoMemoDao(): GeoMemoDao

    companion object {

        private const val DATABASE_NAME: String = "geo_memo_database"

        @Volatile
        private var INSTANCE: GeoMemoDatabase? = null

        fun getInstance(context: Context): GeoMemoDatabase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        GeoMemoDatabase::class.java,
                        DATABASE_NAME
                    ).fallbackToDestructiveMigration().build()
                }
                return INSTANCE as GeoMemoDatabase
//                var instance = INSTANCE
//                if (instance == null) {
//                    instance = Room.databaseBuilder(
//                        context.applicationContext,
//                        GeoMemoDatabase::class.java,
//                        DATABASE_NAME
//                    ).fallbackToDestructiveMigration().build()
//                    INSTANCE = instance
//                }
//                return instance
            }
        }

    }

}