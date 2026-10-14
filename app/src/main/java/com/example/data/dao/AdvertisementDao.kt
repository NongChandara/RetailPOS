package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdvertisementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdvertisementDao {
    @Query("SELECT * FROM advertisements ORDER BY displayOrder ASC, id DESC")
    fun getAllAdvertisements(): Flow<List<AdvertisementEntity>>

    @Query("SELECT * FROM advertisements WHERE isRunning = 1 ORDER BY displayOrder ASC, id DESC")
    fun getRunningAdvertisements(): Flow<List<AdvertisementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvertisement(ad: AdvertisementEntity): Long

    @Update
    suspend fun updateAdvertisement(ad: AdvertisementEntity)

    @Delete
    suspend fun deleteAdvertisement(ad: AdvertisementEntity)

    @Query("DELETE FROM advertisements WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE advertisements SET isRunning = :isRunning WHERE id = :id")
    suspend fun toggleRunning(id: Long, isRunning: Boolean)

    @Query("SELECT COUNT(*) FROM advertisements")
    suspend fun getCount(): Int
}
