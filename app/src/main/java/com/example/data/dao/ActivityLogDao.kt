package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE staffId = :staffId ORDER BY timestamp DESC")
    fun getLogsByStaff(staffId: Long): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE category = :category ORDER BY timestamp DESC")
    fun getLogsByCategory(category: String): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE staffId = :staffId AND category = :category ORDER BY timestamp DESC")
    fun getLogsByStaffAndCategory(staffId: Long, category: String): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE entityId = :entityId ORDER BY timestamp DESC")
    fun getLogsByEntityId(entityId: String): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ActivityLogEntity>)

    @Query("SELECT COUNT(*) FROM activity_logs")
    suspend fun countLogs(): Int

    @Query("DELETE FROM activity_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}
