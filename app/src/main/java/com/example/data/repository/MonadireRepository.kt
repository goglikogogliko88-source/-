package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MonadireRepository(
    private val tripDao: HuntingTripDao,
    private val spotDao: HuntingSpotDao,
    private val equipmentDao: EquipmentDao,
    private val speciesDao: SpeciesDao,
    private val notificationDao: NotificationDao,
    private val checklistDao: ChecklistDao
) {
    val allTrips: Flow<List<HuntingTripEntity>> = tripDao.getAllTrips()
    val allSpots: Flow<List<HuntingSpotEntity>> = spotDao.getAllSpots()
    val favoriteSpots: Flow<List<HuntingSpotEntity>> = spotDao.getFavoriteSpots()
    val allEquipment: Flow<List<EquipmentEntity>> = equipmentDao.getAllEquipment()
    val allSpecies: Flow<List<SpeciesEntity>> = speciesDao.getAllSpecies()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val allChecklists: Flow<List<HuntingChecklistEntity>> = checklistDao.getAllChecklists()
    val allChecklistItems: Flow<List<ChecklistItemEntity>> = checklistDao.getAllItems()

    fun getItemsForChecklist(checklistId: Long): Flow<List<ChecklistItemEntity>> = checklistDao.getItemsForChecklist(checklistId)

    suspend fun insertTrip(trip: HuntingTripEntity): Long = tripDao.insertTrip(trip)
    suspend fun deleteTrip(id: Long) = tripDao.deleteTripById(id)

    suspend fun insertSpot(spot: HuntingSpotEntity): Long = spotDao.insertSpot(spot)
    suspend fun updateSpot(spot: HuntingSpotEntity) = spotDao.updateSpot(spot)
    suspend fun deleteSpot(id: Long) = spotDao.deleteSpotById(id)

    suspend fun insertEquipment(equipment: EquipmentEntity): Long = equipmentDao.insertEquipment(equipment)
    suspend fun updateEquipment(equipment: EquipmentEntity) = equipmentDao.updateEquipment(equipment)
    suspend fun deleteEquipment(id: Long) = equipmentDao.deleteEquipmentById(id)

    suspend fun insertSpecies(species: SpeciesEntity): Long = speciesDao.insertSpecies(species)
    suspend fun updateSpecies(species: SpeciesEntity) = speciesDao.updateSpecies(species)
    suspend fun deleteSpecies(species: SpeciesEntity) = speciesDao.deleteSpecies(species)

    suspend fun insertNotification(notification: NotificationEntity) = notificationDao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = notificationDao.markAsRead(id)
    suspend fun deleteNotification(id: Long) = notificationDao.deleteNotification(id)

    suspend fun insertChecklist(checklist: HuntingChecklistEntity): Long = checklistDao.insertChecklist(checklist)
    suspend fun updateChecklist(checklist: HuntingChecklistEntity) = checklistDao.updateChecklist(checklist)
    suspend fun deleteChecklist(id: Long) {
        checklistDao.deleteItemsForChecklist(id)
        checklistDao.deleteChecklistById(id)
    }

    suspend fun insertChecklistItem(item: ChecklistItemEntity): Long = checklistDao.insertItem(item)
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>) = checklistDao.insertItems(items)
    suspend fun updateChecklistItem(item: ChecklistItemEntity) = checklistDao.updateItem(item)
    suspend fun updateItemPacked(itemId: Long, isPacked: Boolean) = checklistDao.updateItemPacked(itemId, isPacked)
    suspend fun setAllItemsPacked(checklistId: Long, isPacked: Boolean) = checklistDao.setAllItemsPackedForChecklist(checklistId, isPacked)
    suspend fun deleteChecklistItem(itemId: Long) = checklistDao.deleteItemById(itemId)

    suspend fun ensureDataInitialized() {
        val speciesList = speciesDao.getAllSpecies().first()
        if (speciesList.isEmpty()) {
            speciesDao.insertAll(InitialData.sampleSpecies)
            InitialData.sampleSpots.forEach { spotDao.insertSpot(it) }
            InitialData.sampleEquipment.forEach { equipmentDao.insertEquipment(it) }
            InitialData.sampleTrips.forEach { tripDao.insertTrip(it) }
            InitialData.sampleNotifications.forEach { notificationDao.insertNotification(it) }
        } else if (speciesList.any { it.dailyLimit == 0 && it.nameGeo == "მწყერი" }) {
            // Update preset species with official regulations
            speciesDao.insertAll(InitialData.sampleSpecies)
        }

        // Initialize checklists if empty
        val checklists = checklistDao.getAllChecklists().first()
        if (checklists.isEmpty()) {
            InitialData.sampleChecklists.forEach { checklistDao.insertChecklist(it) }
            checklistDao.insertItems(InitialData.sampleChecklistItems)
        }
    }

    fun calculateHuntingCondition(weather: WeatherInfo): HuntingConditionScore {
        // Evaluate temperature (10-22 optimal), wind (< 15km/h), rain (< 20%), pressure (1012-1022)
        var scorePoints = 0
        if (weather.temperatureC in 8..24) scorePoints += 1
        if (weather.windKmh <= 15) scorePoints += 1
        if (weather.rainProbabilityPercent <= 25) scorePoints += 1
        if (weather.pressureHpa in 1010..1025) scorePoints += 1

        return when {
            scorePoints >= 4 -> HuntingConditionScore.VERY_GOOD
            scorePoints == 3 -> HuntingConditionScore.GOOD
            scorePoints == 2 -> HuntingConditionScore.MODERATE
            else -> HuntingConditionScore.POOR
        }
    }
}
