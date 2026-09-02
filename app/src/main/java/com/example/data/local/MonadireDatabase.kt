package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.TypeConverters
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HuntingTripEntity::class,
        HuntingSpotEntity::class,
        EquipmentEntity::class,
        SpeciesEntity::class,
        NotificationEntity::class,
        HuntingChecklistEntity::class,
        ChecklistItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MonadireDatabase : RoomDatabase() {
    abstract fun huntingTripDao(): HuntingTripDao
    abstract fun huntingSpotDao(): HuntingSpotDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun speciesDao(): SpeciesDao
    abstract fun notificationDao(): NotificationDao
    abstract fun checklistDao(): ChecklistDao

    companion object {
        @Volatile
        private var INSTANCE: MonadireDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MonadireDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MonadireDatabase::class.java,
                    "monadire_hunting_db"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(db: MonadireDatabase) {
                // Populate initial species
                db.speciesDao().insertAll(InitialData.sampleSpecies)

                // Populate initial spots
                InitialData.sampleSpots.forEach {
                    db.huntingSpotDao().insertSpot(it)
                }

                // Populate initial equipment
                InitialData.sampleEquipment.forEach {
                    db.equipmentDao().insertEquipment(it)
                }

                // Populate initial trips
                InitialData.sampleTrips.forEach {
                    db.huntingTripDao().insertTrip(it)
                }

                // Populate initial notifications
                InitialData.sampleNotifications.forEach {
                    db.notificationDao().insertNotification(it)
                }

                // Populate initial checklists and items
                InitialData.sampleChecklists.forEach {
                    db.checklistDao().insertChecklist(it)
                }
                db.checklistDao().insertItems(InitialData.sampleChecklistItems)
            }
        }
    }
}
