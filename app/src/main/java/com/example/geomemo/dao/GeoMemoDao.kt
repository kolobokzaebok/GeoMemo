package com.example.geomemo.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.geomemo.model.GeoMemoModel
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoMemoDao {

    @Insert
    suspend fun insert(geoMemoModel: GeoMemoModel)

    @Update
    suspend fun update(geoMemoModel: GeoMemoModel)

    @Delete
    suspend fun delete(geoMemoModel: GeoMemoModel)

    @Query("select * from `memo`")
    fun getAllMemos(): Flow<List<GeoMemoModel>>

    @Query("select * from `memo` where id = :id")
    suspend fun getMemoById(id: Int): GeoMemoModel?

}