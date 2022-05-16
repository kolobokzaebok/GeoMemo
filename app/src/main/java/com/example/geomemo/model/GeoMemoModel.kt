package com.example.geomemo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "memo")
data class GeoMemoModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String = "title",
    var imagePath: String = "imagePath",
    var description: String = "description",
    var date: String = "date",
    var location: String = "location",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) : Serializable