package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY updatedAt DESC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: Long): ClientEntity?

    @Query("SELECT * FROM clients WHERE phone = :phone LIMIT 1")
    suspend fun getClientByPhone(phone: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE tier = :tier ORDER BY name ASC")
    fun getClientsByTier(tier: String): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteClientById(id: Long)

    @Query("UPDATE clients SET loyaltyPoints = loyaltyPoints + :pointsDelta, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLoyaltyPoints(id: Long, pointsDelta: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE clients SET totalSpent = totalSpent + :amount, visitsCount = visitsCount + 1, loyaltyPoints = loyaltyPoints + :pointsEarned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun recordPurchase(id: Long, amount: Double, pointsEarned: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun countClients(): Int
}
