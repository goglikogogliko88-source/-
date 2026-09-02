package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HuntingTripDao {
    @Query("SELECT * FROM hunting_trips ORDER BY createdAt DESC")
    fun getAllTrips(): Flow<List<HuntingTripEntity>>

    @Query("SELECT * FROM hunting_trips WHERE id = :id")
    suspend fun getTripById(id: Long): HuntingTripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: HuntingTripEntity): Long

    @Update
    suspend fun updateTrip(trip: HuntingTripEntity)

    @Delete
    suspend fun deleteTrip(trip: HuntingTripEntity)

    @Query("DELETE FROM hunting_trips WHERE id = :id")
    suspend fun deleteTripById(id: Long)

    @Query("SELECT COUNT(*) FROM hunting_trips")
    fun getTripCount(): Flow<Int>
}

@Dao
interface HuntingSpotDao {
    @Query("SELECT * FROM hunting_spots ORDER BY createdAt DESC")
    fun getAllSpots(): Flow<List<HuntingSpotEntity>>

    @Query("SELECT * FROM hunting_spots WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteSpots(): Flow<List<HuntingSpotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: HuntingSpotEntity): Long

    @Update
    suspend fun updateSpot(spot: HuntingSpotEntity)

    @Delete
    suspend fun deleteSpot(spot: HuntingSpotEntity)

    @Query("DELETE FROM hunting_spots WHERE id = :id")
    suspend fun deleteSpotById(id: Long)
}

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment ORDER BY id DESC")
    fun getAllEquipment(): Flow<List<EquipmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: EquipmentEntity): Long

    @Update
    suspend fun updateEquipment(equipment: EquipmentEntity)

    @Delete
    suspend fun deleteEquipment(equipment: EquipmentEntity)

    @Query("DELETE FROM equipment WHERE id = :id")
    suspend fun deleteEquipmentById(id: Long)
}

@Dao
interface SpeciesDao {
    @Query("SELECT * FROM species ORDER BY nameGeo ASC")
    fun getAllSpecies(): Flow<List<SpeciesEntity>>

    @Query("SELECT * FROM species WHERE category = :category ORDER BY nameGeo ASC")
    fun getSpeciesByCategory(category: String): Flow<List<SpeciesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(species: List<SpeciesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecies(species: SpeciesEntity): Long

    @Update
    suspend fun updateSpecies(species: SpeciesEntity)

    @Delete
    suspend fun deleteSpecies(species: SpeciesEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM hunting_checklists ORDER BY isPreset DESC, createdAt ASC")
    fun getAllChecklists(): Flow<List<HuntingChecklistEntity>>

    @Query("SELECT * FROM hunting_checklists WHERE id = :id")
    suspend fun getChecklistById(id: Long): HuntingChecklistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(checklist: HuntingChecklistEntity): Long

    @Update
    suspend fun updateChecklist(checklist: HuntingChecklistEntity)

    @Delete
    suspend fun deleteChecklist(checklist: HuntingChecklistEntity)

    @Query("DELETE FROM hunting_checklists WHERE id = :id")
    suspend fun deleteChecklistById(id: Long)

    @Query("SELECT * FROM checklist_items WHERE checklistId = :checklistId ORDER BY sortOrder ASC, id ASC")
    fun getItemsForChecklist(checklistId: Long): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items ORDER BY sortOrder ASC, id ASC")
    fun getAllItems(): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ChecklistItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ChecklistItemEntity>)

    @Update
    suspend fun updateItem(item: ChecklistItemEntity)

    @Query("UPDATE checklist_items SET isPacked = :isPacked WHERE id = :itemId")
    suspend fun updateItemPacked(itemId: Long, isPacked: Boolean)

    @Query("UPDATE checklist_items SET isPacked = :isPacked WHERE checklistId = :checklistId")
    suspend fun setAllItemsPackedForChecklist(checklistId: Long, isPacked: Boolean)

    @Query("DELETE FROM checklist_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Long)

    @Query("DELETE FROM checklist_items WHERE checklistId = :checklistId")
    suspend fun deleteItemsForChecklist(checklistId: Long)
}
