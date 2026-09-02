package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hunting_trips")
data class HuntingTripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String = "",
    val durationMinutes: Int = 0,
    val locationName: String,
    val latitude: Double = 41.7151,
    val longitude: Double = 44.8271,
    val weatherSummary: String = "მზიანი",
    val temperatureC: Int = 18,
    val windKmh: Int = 12,
    val windDirection: String = "ჩრდილო-დასავლეთი",
    val huntingType: String = "ფრინველზე ნადირობა",
    val targetSpecies: String = "მწყერი",
    val hunterCount: Int = 1,
    val equipmentUsed: String = "ორლულიანი თოფი 12/76",
    val ammoUsed: String = "N9 28გრ (15 გასროლა)",
    val isSuccessful: Boolean = true,
    val harvestCount: Int = 4,
    val harvestDetails: String = "4 ცალი მწყერი",
    val notes: String = "სანადირო ძაღლმა კარგად იმუშავა. დილის 06:30-ზე დაიწყო ფრენა.",
    val photoUrl: String = "",
    val isSynced: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hunting_spots")
data class HuntingSpotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // სანადირო ადგილი, წყარო, ტყე, გზა, საფრთხე, პარკინგი, კარავი, სხვა
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Int = 650,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // თოფი, ოპტიკა, ვაზნები, ტანსაცმელი, ფეხსაცმელი, ზურგჩანთა, ფანარი, GPS, სხვა
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val purchaseDate: String = "",
    val lastMaintenanceDate: String = "",
    val nextMaintenanceDate: String = "",
    val notes: String = "",
    val isReminderEnabled: Boolean = true
)

@Entity(tableName = "hunting_checklists")
data class HuntingChecklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val huntType: String, // BIRD_HUNTING, BIG_GAME, WATERFOWL, PREDATOR, MOUNTAIN, CUSTOM
    val huntTypeLabelKa: String = "ფრინველზე ნადირობა",
    val description: String = "",
    val targetSeason: String = "",
    val isPreset: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val checklistId: Long,
    val title: String,
    val category: String = "ეკიპირება", // იარაღი & ვაზნები, უსაფრთხოება & საბუთები, ტანსაცმელი & ფეხსაცმელი, ნავიგაცია & კავშირი, ძაღლის აღჭურვილობა, ბანაკი & კვება, ოპტიკა & აქსესუარები
    val quantity: String = "1",
    val isPacked: Boolean = false,
    val isMandatory: Boolean = false,
    val notes: String = "",
    val sortOrder: Int = 0
)

data class ChecklistWithItems(
    val checklist: HuntingChecklistEntity,
    val items: List<ChecklistItemEntity>
) {
    val totalItems: Int get() = items.size
    val packedItems: Int get() = items.count { it.isPacked }
    val progressPercent: Float get() = if (totalItems == 0) 0f else packedItems.toFloat() / totalItems.toFloat()
    val isFullyPacked: Boolean get() = totalItems > 0 && packedItems == totalItems
    val mandatoryMissingCount: Int get() = items.count { it.isMandatory && !it.isPacked }
}

@Entity(tableName = "species")
data class SpeciesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameGeo: String,
    val scientificName: String,
    val category: String, // გადამფრენი ფრინველი, წყალმცურავი ფრინველი, ფრინველები, მტაცებლები, ჩლიქოსნები, სხვა ნადირი
    val isSeasonOpen: Boolean = false,
    val seasonDates: String = "",
    val dailyLimit: Int = 0, // მაქსიმალური დღიური რაოდენობა ერთ მონადირეზე
    val status: String = "დაშვებულია", // "დაშვებულია", "აკრძალულია", "დაშვებულია (სეზონზე)", "სალიცენზიო"
    val prohibitedMethods: List<String> = emptyList(),
    val description: String = "",
    val habitat: String = "",
    val huntingTips: String = "",
    val identification: String = "",
    val legalStatus: String = "",
    val isProtected: Boolean = false
) {
    val nameKa: String get() = nameGeo
    val nameLatin: String get() = scientificName
    val seasonInfo: String get() = seasonDates
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // MAINTENANCE, SEASON, WEATHER, SYSTEM
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class WeatherInfo(
    val locationName: String = "ბორჯომის ხეობა",
    val temperatureC: Int = 16,
    val feelsLikeC: Int = 15,
    val condition: String = "ნაწილობრივ ღრუბლიანი",
    val windKmh: Int = 11,
    val windDirection: String = "ჩრდილო-აღმოსავლეთი",
    val humidityPercent: Int = 62,
    val pressureHpa: Int = 1018,
    val rainProbabilityPercent: Int = 15,
    val sunriseTime: String = "06:22",
    val sunsetTime: String = "19:48",
    val uvIndex: Int = 4
)

enum class HuntingConditionScore(
    val labelKa: String,
    val score: Int,
    val descriptionKa: String
) {
    POOR("ცუდი", 1, "არახელსაყრელი ამინდი (ძლიერი ქარი ან წვიმა)"),
    MODERATE("საშუალო", 2, "მისაღები პირობები, საჭიროებს ყურადღებას"),
    GOOD("კარგი", 3, "სტაბილური წნევა, ზომიერი ქარი, კარგი ხილვადობა"),
    VERY_GOOD("ძალიან კარგი", 4, "იდეალური პირობები: ოპტიმალური ტემპერატურა და სუსტი ნიავი")
}

data class UserProfile(
    val name: String = "გიორგი ბერიძე",
    val email: String = "monadire.user@georgia.ge",
    val huntingTypePreference: String = "ფრინველზე და ჩლიქოსნებზე",
    val favoriteSpecies: String = "მწყერი, ქედანი, გარეული ღორი",
    val hunterIdNumber: String = "GEO-HN-88412",
    val isOfflineMode: Boolean = false,
    val isAdmin: Boolean = false
)
