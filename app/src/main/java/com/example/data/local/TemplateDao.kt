package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY updatedAtTimestamp DESC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE isFavorite = 1 ORDER BY updatedAtTimestamp DESC")
    fun getFavoriteTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE isUserCreated = 1 ORDER BY updatedAtTimestamp DESC")
    fun getUserCreatedTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: String): TemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<TemplateEntity>)

    @Query("UPDATE templates SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE templates SET title = :newTitle, updatedAtTimestamp = :timestamp WHERE id = :id")
    suspend fun renameTemplate(id: String, newTitle: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplateById(id: String)
}
