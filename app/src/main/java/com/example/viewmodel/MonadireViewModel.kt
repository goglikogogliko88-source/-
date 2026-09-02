package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.InitialData
import com.example.data.local.MonadireDatabase
import com.example.data.model.*
import com.example.data.repository.MonadireRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppDestination {
    HOME,
    MAP,
    ACTIVE_HUNT,
    JOURNAL,
    PROFILE,
    SPECIES_CATALOG,
    EQUIPMENT_INVENTORY,
    SAFETY_AND_RULES,
    ADMIN_PANEL,
    GLOBAL_SEARCH
}

data class ActiveHuntSession(
    val startTimeFormatted: String,
    val startTimestamp: Long = System.currentTimeMillis(),
    val locationName: String = "საგარეჯოს ველები",
    val latitude: Double = 41.7335,
    val longitude: Double = 45.3312,
    val targetSpecies: String = "მწყერი",
    val hunterCount: Int = 1,
    val equipmentUsed: String = "ორლულიანი თოფი (12/76)",
    val weatherSnapshot: WeatherInfo = WeatherInfo(),
    val elapsedSeconds: Long = 0,
    val harvestCount: Int = 0,
    val isTracking: Boolean = true
)

class MonadireViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MonadireRepository
    private var timerJob: Job? = null

    val allTrips: StateFlow<List<HuntingTripEntity>>
    val allSpots: StateFlow<List<HuntingSpotEntity>>
    val favoriteSpots: StateFlow<List<HuntingSpotEntity>>
    val allEquipment: StateFlow<List<EquipmentEntity>>
    val allSpecies: StateFlow<List<SpeciesEntity>>
    val allNotifications: StateFlow<List<NotificationEntity>>
    val allChecklists: StateFlow<List<HuntingChecklistEntity>>
    val allChecklistItems: StateFlow<List<ChecklistItemEntity>>

    val checklistsWithItems: StateFlow<List<ChecklistWithItems>>

    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _currentWeather = MutableStateFlow(
        WeatherInfo(
            locationName = "ბორჯომი / თბილისის შემოგარენი",
            temperatureC = 18,
            feelsLikeC = 17,
            condition = "ნაწილობრივ ღრუბლიანი, მშრალი",
            windKmh = 10,
            windDirection = "ჩრდილო-დასავლეთი (სუსტი)",
            humidityPercent = 58,
            pressureHpa = 1016,
            rainProbabilityPercent = 10,
            sunriseTime = "06:24",
            sunsetTime = "19:46"
        )
    )
    val currentWeather: StateFlow<WeatherInfo> = _currentWeather.asStateFlow()

    private val _huntingCondition = MutableStateFlow(HuntingConditionScore.VERY_GOOD)
    val huntingCondition: StateFlow<HuntingConditionScore> = _huntingCondition.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _activeHunt = MutableStateFlow<ActiveHuntSession?>(null)
    val activeHunt: StateFlow<ActiveHuntSession?> = _activeHunt.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSpeciesCategory = MutableStateFlow("ყველა")
    val selectedSpeciesCategory: StateFlow<String> = _selectedSpeciesCategory.asStateFlow()

    private val _selectedTripFilter = MutableStateFlow("ყველა")
    val selectedTripFilter: StateFlow<String> = _selectedTripFilter.asStateFlow()

    private val _selectedEquipmentCategory = MutableStateFlow("ყველა")
    val selectedEquipmentCategory: StateFlow<String> = _selectedEquipmentCategory.asStateFlow()

    private val _selectedSpotCategory = MutableStateFlow("ყველა")
    val selectedSpotCategory: StateFlow<String> = _selectedSpotCategory.asStateFlow()

    private val _selectedChecklistHuntType = MutableStateFlow("ყველა")
    val selectedChecklistHuntType: StateFlow<String> = _selectedChecklistHuntType.asStateFlow()

    private val _checklistSearchQuery = MutableStateFlow("")
    val checklistSearchQuery: StateFlow<String> = _checklistSearchQuery.asStateFlow()

    init {
        val db = MonadireDatabase.getDatabase(application, viewModelScope)
        repository = MonadireRepository(
            db.huntingTripDao(),
            db.huntingSpotDao(),
            db.equipmentDao(),
            db.speciesDao(),
            db.notificationDao(),
            db.checklistDao()
        )

        allTrips = repository.allTrips.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allSpots = repository.allSpots.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        favoriteSpots = repository.favoriteSpots.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allEquipment = repository.allEquipment.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allSpecies = repository.allSpecies.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allNotifications = repository.allNotifications.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allChecklists = repository.allChecklists.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allChecklistItems = repository.allChecklistItems.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        checklistsWithItems = combine(allChecklists, allChecklistItems) { checklists, items ->
            val itemsByChecklist = items.groupBy { it.checklistId }
            checklists.map { checklist ->
                ChecklistWithItems(
                    checklist = checklist,
                    items = itemsByChecklist[checklist.id] ?: emptyList()
                )
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.ensureDataInitialized()
            updateHuntingConditionScore()
        }
    }

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSpeciesCategory(category: String) {
        _selectedSpeciesCategory.value = category
    }

    fun setTripFilter(filter: String) {
        _selectedTripFilter.value = filter
    }

    fun setEquipmentCategory(category: String) {
        _selectedEquipmentCategory.value = category
    }

    fun setSpotCategory(category: String) {
        _selectedSpotCategory.value = category
    }

    fun setRegionWeather(regionName: String, temp: Int, condition: String, wind: Int, windDir: String, rainProb: Int) {
        val updated = _currentWeather.value.copy(
            locationName = regionName,
            temperatureC = temp,
            condition = condition,
            windKmh = wind,
            windDirection = windDir,
            rainProbabilityPercent = rainProb
        )
        _currentWeather.value = updated
        updateHuntingConditionScore()
    }

    private fun updateHuntingConditionScore() {
        _huntingCondition.value = repository.calculateHuntingCondition(_currentWeather.value)
    }

    // Quick 10-second Start Hunt Workflow
    fun startQuickHunt(
        targetSpecies: String = "მწყერი",
        locationName: String = "საგარეჯოს ველები",
        hunterCount: Int = 1,
        equipment: String = "ორლულიანი თოფი (12/76)"
    ) {
        val nowFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val session = ActiveHuntSession(
            startTimeFormatted = nowFormatted,
            startTimestamp = System.currentTimeMillis(),
            locationName = locationName,
            targetSpecies = targetSpecies,
            hunterCount = hunterCount,
            equipmentUsed = equipment,
            weatherSnapshot = _currentWeather.value
        )
        _activeHunt.value = session
        _currentDestination.value = AppDestination.ACTIVE_HUNT

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeHunt.value?.let { current ->
                    val elapsed = (System.currentTimeMillis() - current.startTimestamp) / 1000
                    _activeHunt.value = current.copy(elapsedSeconds = elapsed)
                }
            }
        }
    }

    fun updateActiveHarvestCount(count: Int) {
        _activeHunt.value?.let {
            _activeHunt.value = it.copy(harvestCount = count)
        }
    }

    // Finish Active Hunt Workflow
    fun finishActiveHunt(
        isSuccessful: Boolean,
        harvestCount: Int,
        harvestDetails: String,
        notes: String,
        ammoUsed: String
    ) {
        val session = _activeHunt.value ?: return
        timerJob?.cancel()
        timerJob = null

        val endFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val durationMins = (session.elapsedSeconds / 60).toInt().coerceAtLeast(1)

        val trip = HuntingTripEntity(
            title = "${session.targetSpecies}ზე ნადირობა (${session.locationName})",
            date = dateFormatted,
            startTime = session.startTimeFormatted,
            endTime = endFormatted,
            durationMinutes = durationMins,
            locationName = session.locationName,
            latitude = session.latitude,
            longitude = session.longitude,
            weatherSummary = "${session.weatherSnapshot.condition}, ${session.weatherSnapshot.temperatureC}°C",
            temperatureC = session.weatherSnapshot.temperatureC,
            windKmh = session.weatherSnapshot.windKmh,
            windDirection = session.weatherSnapshot.windDirection,
            huntingType = if (session.targetSpecies in listOf("მწყერი", "ქედანი", "ტყის ქათამი", "გარეული იხვი", "ხოხობი")) "ფრინველზე" else "ნადირზე",
            targetSpecies = session.targetSpecies,
            hunterCount = session.hunterCount,
            equipmentUsed = session.equipmentUsed,
            ammoUsed = ammoUsed.ifEmpty { "10-15 ვაზნა" },
            isSuccessful = isSuccessful,
            harvestCount = harvestCount,
            harvestDetails = harvestDetails.ifEmpty { if (harvestCount > 0) "$harvestCount ცალი ${session.targetSpecies}" else "უშედეგო" },
            notes = notes.ifEmpty { "ნადირობა წარმატებით დასრულდა და ჩაიწერა მონადირის დღიურში." },
            isSynced = true
        )

        viewModelScope.launch {
            repository.insertTrip(trip)
            _activeHunt.value = null
            _currentDestination.value = AppDestination.JOURNAL
        }
    }

    fun cancelActiveHunt() {
        timerJob?.cancel()
        timerJob = null
        _activeHunt.value = null
        _currentDestination.value = AppDestination.HOME
    }

    // Manual Trip Addition
    fun addTrip(trip: HuntingTripEntity) {
        viewModelScope.launch {
            repository.insertTrip(trip)
        }
    }

    fun deleteTrip(id: Long) {
        viewModelScope.launch {
            repository.deleteTrip(id)
        }
    }

    // Hunting Spots
    fun addSpot(name: String, category: String, lat: Double, lng: Double, elevation: Int, notes: String) {
        viewModelScope.launch {
            val spot = HuntingSpotEntity(
                name = name,
                category = category,
                latitude = lat,
                longitude = lng,
                elevationMeters = elevation,
                notes = notes,
                isFavorite = false
            )
            repository.insertSpot(spot)
        }
    }

    fun toggleSpotFavorite(spot: HuntingSpotEntity) {
        viewModelScope.launch {
            repository.updateSpot(spot.copy(isFavorite = !spot.isFavorite))
        }
    }

    fun updateSpot(spot: HuntingSpotEntity) {
        viewModelScope.launch {
            repository.updateSpot(spot)
        }
    }

    fun deleteSpot(id: Long) {
        viewModelScope.launch {
            repository.deleteSpot(id)
        }
    }

    // Equipment
    fun addEquipment(
        name: String,
        category: String,
        brand: String,
        model: String,
        serialNumber: String,
        purchaseDate: String,
        nextMaintenanceDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            val eq = EquipmentEntity(
                name = name,
                category = category,
                brand = brand,
                model = model,
                serialNumber = serialNumber,
                purchaseDate = purchaseDate,
                lastMaintenanceDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                nextMaintenanceDate = nextMaintenanceDate,
                notes = notes,
                isReminderEnabled = true
            )
            repository.insertEquipment(eq)
        }
    }

    fun deleteEquipment(id: Long) {
        viewModelScope.launch {
            repository.deleteEquipment(id)
        }
    }

    // Species
    fun addSpecies(species: SpeciesEntity) {
        viewModelScope.launch {
            repository.insertSpecies(species)
        }
    }

    fun deleteSpecies(species: SpeciesEntity) {
        viewModelScope.launch {
            repository.deleteSpecies(species)
        }
    }

    // Notifications
    fun addNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            repository.insertNotification(notification)
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // Profile
    fun updateUserProfile(name: String, email: String, huntingType: String, favoriteSpecies: String) {
        _userProfile.value = _userProfile.value.copy(
            name = name,
            email = email,
            huntingTypePreference = huntingType,
            favoriteSpecies = favoriteSpecies
        )
    }

    fun toggleAdminMode() {
        _userProfile.value = _userProfile.value.copy(
            isAdmin = !_userProfile.value.isAdmin
        )
    }

    // Checklist Management
    fun setSelectedChecklistHuntType(huntType: String) {
        _selectedChecklistHuntType.value = huntType
    }

    fun setChecklistSearchQuery(query: String) {
        _checklistSearchQuery.value = query
    }

    fun toggleItemPacked(itemId: Long, isPacked: Boolean) {
        viewModelScope.launch {
            repository.updateItemPacked(itemId, isPacked)
        }
    }

    fun setAllItemsPacked(checklistId: Long, isPacked: Boolean) {
        viewModelScope.launch {
            repository.setAllItemsPacked(checklistId, isPacked)
        }
    }

    fun createChecklist(
        title: String,
        huntType: String,
        huntTypeLabelKa: String,
        description: String,
        targetSeason: String,
        populateDefaultItems: Boolean
    ) {
        viewModelScope.launch {
            val checklistId = repository.insertChecklist(
                HuntingChecklistEntity(
                    title = title,
                    huntType = huntType,
                    huntTypeLabelKa = huntTypeLabelKa,
                    description = description,
                    targetSeason = targetSeason,
                    isPreset = false
                )
            )

            if (populateDefaultItems) {
                // Populate default template items based on hunt type
                val templateItems = when (huntType) {
                    "BIRD_HUNTING" -> listOf(
                        ChecklistItemEntity(checklistId = checklistId, title = "თოფი 12/76 კალიბრი", category = "იარაღი & ვაზნები", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "საფანტის ვაზნები N9/N8", category = "იარაღი & ვაზნები", quantity = "50 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "სანადირო იარაღის მოწმობა და მოსაკრებლის ქვითარი", category = "უსაფრთხოება & საბუთები", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "ნარინჯისფერი უსაფრთხოების ჟილეტი / ქუდი", category = "უსაფრთხოება & საბუთები", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "პირველადი დახმარების მინი-აფთიაქი", category = "უსაფრთხოება & საბუთები", quantity = "1 კომპლექტი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "სანადირო ჟილეტი (იაგდტაში) და პატრონტაში", category = "ტანსაცმელი & ეკიპირება", quantity = "1 ცალი", isMandatory = false),
                        ChecklistItemEntity(checklistId = checklistId, title = "წყალი მონადირისა და ძაღლისთვის", category = "ბანაკი & კვება", quantity = "2 ლიტრი", isMandatory = true)
                    )
                    "BIG_GAME" -> listOf(
                        ChecklistItemEntity(checklistId = checklistId, title = "ხრახნილლულიანი კარაბინი", category = "იარაღი & ვაზნები", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "მძიმე ექსპანსიური ვაზნები", category = "იარაღი & ვაზნები", quantity = "20 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "დიდ ნადირზე ნადირობის ლიცენზია", category = "უსაფრთხოება & საბუთები", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "Hi-Viz ნარინჯისფერი ჟილეტი", category = "უსაფრთხოება & საბუთები", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "IFAK ტრავმა-აფთიაქი და ტურნიკეტი", category = "უსაფრთხოება & საბუთები", quantity = "1 კომპლექტი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "ოპტიკური სამიზნე და ბინოკლი", category = "ოპტიკა & აქსესუარები", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "VHF რადიოსადგური (რაცია)", category = "ნავიგაცია & კავშირი", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "სანადირო დანა და სალესი", category = "ტანსაცმელი & ეკიპირება", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "ფანარი თავზე (Headlamp)", category = "ტანსაცმელი & ეკიპირება", quantity = "1 ცალი", isMandatory = true)
                    )
                    "WATERFOWL" -> listOf(
                        ChecklistItemEntity(checklistId = checklistId, title = "თოფი 12/76 და წყალგაუმტარი ვაზნები N5", category = "იარაღი & ვაზნები", quantity = "50 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "ნეოპრენის მაღალი კომბინეზონი (Waders)", category = "ტანსაცმელი & ეკიპირება", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "იხვის სატყუარები (Decoys) და მანოკი", category = "ტანსაცმელი & ეკიპირება", quantity = "1 კომპლექტი", isMandatory = false),
                        ChecklistItemEntity(checklistId = checklistId, title = "მოსაკრებლის ქვითარი და მოწმობა", category = "უსაფრთხოება & საბუთები", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "ჰერმეტული ჩანთა (Dry Bag)", category = "უსაფრთხოება & საბუთები", quantity = "1 ცალი", isMandatory = true)
                    )
                    "PREDATOR" -> listOf(
                        ChecklistItemEntity(checklistId = checklistId, title = "კარაბინი / გლუვლულიანი თოფი და ვაზნები", category = "იარაღი & ვაზნები", quantity = "25 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "აკუსტიკური მანოკი", category = "იარაღი & ვაზნები", quantity = "1 ცალი", isMandatory = false),
                        ChecklistItemEntity(checklistId = checklistId, title = "სასროლი შტატივი (Bipod)", category = "ოპტიკა & აქსესუარები", quantity = "1 ცალი", isMandatory = false),
                        ChecklistItemEntity(checklistId = checklistId, title = "სრული 3D კამუფლირება", category = "ტანსაცმელი & ეკიპირება", quantity = "1 კომპლექტი", isMandatory = true)
                    )
                    "MOUNTAIN" -> listOf(
                        ChecklistItemEntity(checklistId = checklistId, title = "შორი მანძილის კარაბინი და ოპტიკა", category = "იარაღი & ვაზნები", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "მთის სალაშქრო ბათინკები", category = "ტანსაცმელი & ეკიპირება", quantity = "1 წყვილი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "ლაზერული მანძილმზომი", category = "ოპტიკა & აქსესუარები", quantity = "1 ცალი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "ზურგჩანთა, კარავი და საძილე ტომარა", category = "ბანაკი & კვება", quantity = "1 კომპლექტი", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "სატელიტური GPS / SOS ტრეკერი", category = "ნავიგაცია & კავშირი", quantity = "1 ცალი", isMandatory = true)
                    )
                    else -> listOf(
                        ChecklistItemEntity(checklistId = checklistId, title = "სანადირო იარაღი და ვაზნები", category = "იარაღი & ვაზნები", quantity = "1", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "სანადირო დოკუმენტები და მოწმობა", category = "უსაფრთხოება & საბუთები", quantity = "1", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "სასიგნალო ნარინჯისფერი ჟილეტი", category = "უსაფრთხოება & საბუთები", quantity = "1", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "პირველადი დახმარების აფთიაქი", category = "უსაფრთხოება & საბუთები", quantity = "1", isMandatory = true),
                        ChecklistItemEntity(checklistId = checklistId, title = "სასმელი წყალი და ულუფა", category = "ბანაკი & კვება", quantity = "2ლ", isMandatory = true)
                    )
                }
                repository.insertChecklistItems(templateItems)
            }
        }
    }

    fun addChecklistItem(
        checklistId: Long,
        title: String,
        category: String,
        quantity: String,
        isMandatory: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertChecklistItem(
                ChecklistItemEntity(
                    checklistId = checklistId,
                    title = title,
                    category = category,
                    quantity = quantity.ifBlank { "1 ცალი" },
                    isPacked = false,
                    isMandatory = isMandatory,
                    notes = notes
                )
            )
        }
    }

    fun deleteChecklist(checklistId: Long) {
        viewModelScope.launch {
            repository.deleteChecklist(checklistId)
        }
    }

    fun deleteChecklistItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteChecklistItem(itemId)
        }
    }

    fun resetAllChecklistsToPresets() {
        viewModelScope.launch {
            InitialData.sampleChecklists.forEach { repository.insertChecklist(it) }
            repository.insertChecklistItems(InitialData.sampleChecklistItems)
        }
    }
}
