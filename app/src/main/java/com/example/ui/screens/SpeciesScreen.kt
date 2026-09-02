package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeciesEntity
import com.example.ui.theme.*
import com.example.viewmodel.MonadireViewModel

@Composable
fun SpeciesScreen(
    viewModel: MonadireViewModel,
    modifier: Modifier = Modifier
) {
    val allSpecies by viewModel.allSpecies.collectAsState()
    val selectedCategory by viewModel.selectedSpeciesCategory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var expandedSpeciesId by remember { mutableStateOf<Long?>(null) }

    val categories = listOf(
        "ყველა",
        "გადამფრენი ფრინველი",
        "წყალმცურავი ფრინველი",
        "ფრინველები",
        "ჩლიქოსნები",
        "მტაცებლები",
        "სხვა ნადირი"
    )

    val filteredSpecies = remember(allSpecies, selectedCategory, searchQuery) {
        allSpecies.filter { sp ->
            val matchesCategory = selectedCategory == "ყველა" || sp.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    sp.nameGeo.contains(searchQuery, ignoreCase = true) ||
                    sp.scientificName.contains(searchQuery, ignoreCase = true) ||
                    sp.description.contains(searchQuery, ignoreCase = true) ||
                    sp.habitat.contains(searchQuery, ignoreCase = true) ||
                    sp.category.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // Global Law & Prohibited Methods Warning Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AlertRed.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AlertRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = AlertRed, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "კანონმდებლობა და აკრძალული მეთოდები",
                                color = AlertRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "საქართველოს გარემოს დაცვის სამინისტროს რეგულაციები",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = ForestCardBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    ProhibitedRuleRow("🚫", "ელექტრონული მანოკების (ხმის გამომცემი მოწყობილობების) გამოყენება მკაცრად აკრძალულია და ისჯება კანონით (ჯარიმა და იარაღის კონფისკაცია).")
                    ProhibitedRuleRow("🚫", "სატრანსპორტო საშუალებიდან (მანქანა, კვადროციკლი, ძრავიანი ნავი) ნადირობა და დევნა კატეგორიულად აკრძალულია.")
                    ProhibitedRuleRow("🚫", "წითელ ნუსხაში შეტანილ სახეობებზე ნადირობა კატეგორიულად აკრძალულია (სისხლის სამართლის პასუხისმგებლობა).")
                    ProhibitedRuleRow("📜", "გადამფრენ ფრინველებზე ნადირობისას სავალდებულოა სახელმწიფო მოსაკრებლის (10 ₾) გადახდის ქვითარი და დღიური ლიმიტების დაცვა.")
                }
            }
        }

        // Header info banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HuntingGreenLight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = HuntingGreenLight, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text(
                            text = "საქართველოს სანადირო სახეობები",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ოფიციალური დღიური ლიმიტები, სეზონის ვადები და წესები",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ძიება ფაუნაში (მაგ. მწყერი, ქედანი, იხვი, ტახი)...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGold) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "გასუფთავება", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGold,
                    unfocusedBorderColor = ForestCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = ForestSurface,
                    unfocusedContainerColor = ForestSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("species_search_input")
            )
        }

        // Category Chips Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSpeciesCategory(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = getSpeciesCategoryIcon(cat),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isSelected) ForestBlack else AccentGold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGold,
                            selectedLabelColor = ForestBlack,
                            containerColor = ForestSurface,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) AccentGold else ForestCardBorder
                        )
                    )
                }
            }
        }

        // Species List
        if (filteredSpecies.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.SearchOff,
                    title = "სახეობა ვერ მოიძებნა",
                    description = "სცადეთ სხვა საძიებო სიტყვა ან კატეგორია."
                )
            }
        } else {
            items(filteredSpecies, key = { it.id }) { species ->
                val isExpanded = expandedSpeciesId == species.id
                SpeciesCard(
                    species = species,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedSpeciesId = if (isExpanded) null else species.id
                    }
                )
            }
        }
    }
}

@Composable
private fun ProhibitedRuleRow(icon: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 13.sp)
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 11.5.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun SpeciesCard(
    species: SpeciesEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggleExpand),
        colors = CardDefaults.cardColors(containerColor = ForestSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) AccentGold else ForestCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title and Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ForestSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getSpeciesCategoryIcon(species.category),
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = species.nameGeo,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = species.scientificName,
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ForestSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
                ) {
                    Text(
                        text = species.category,
                        color = HuntingGreenLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges Row: Season Open/Closed & Status & Daily Limit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Season Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (species.isSeasonOpen) SuccessGreen.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (species.isSeasonOpen) SuccessGreen else AlertRed.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (species.isSeasonOpen) SuccessGreen else AlertRed)
                        )
                        Text(
                            text = if (species.isSeasonOpen) "სეზონი ღიაა" else "სეზონი დახურულია",
                            color = if (species.isSeasonOpen) SuccessGreen else AlertRed,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Daily Limit Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentGold.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = AccentGold, modifier = Modifier.size(11.dp))
                        Text(
                            text = if (species.dailyLimit > 0) "ლიმიტი: ${species.dailyLimit} ცალი/დღე" else "ულიმიტო / სალიცენზიო",
                            color = AccentGold,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Status Badge (დაშვებულია / სალიცენზიო / აკრძალულია)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (species.status) {
                        "დაშვებულია" -> SuccessGreen.copy(alpha = 0.12f)
                        "სალიცენზიო" -> WarningOrange.copy(alpha = 0.12f)
                        else -> ForestSurfaceVariant
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (species.status) {
                            "დაშვებულია" -> SuccessGreen.copy(alpha = 0.4f)
                            "სალიცენზიო" -> WarningOrange.copy(alpha = 0.4f)
                            else -> ForestCardBorder
                        }
                    )
                ) {
                    Text(
                        text = species.status,
                        color = when (species.status) {
                            "დაშვებულია" -> SuccessGreen
                            "სალიცენზიო" -> WarningOrange
                            else -> TextSecondary
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Season Dates Line
            if (species.seasonDates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = TextGold, modifier = Modifier.size(13.dp))
                    Text(
                        text = "სეზონის პერიოდი: ${species.seasonDates}",
                        color = TextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = species.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            // Expanded Details Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = ForestCardBorder)

                    // Prohibited Methods Box
                    if (species.prohibitedMethods.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AlertRed.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = AlertRed, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = "აკრძალული მეთოდები და შეზღუდვები:",
                                        color = AlertRed,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                species.prohibitedMethods.forEach { method ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("•", color = AlertRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = method,
                                            color = TextPrimary,
                                            fontSize = 11.5.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (species.habitat.isNotEmpty()) {
                        SpeciesInfoSection(
                            title = "გავრცელების არეალი (ჰაბიტატი):",
                            content = species.habitat,
                            icon = Icons.Default.Forest
                        )
                    }

                    if (species.huntingTips.isNotEmpty()) {
                        SpeciesInfoSection(
                            title = "სანადირო ტაქტიკა და რჩევები:",
                            content = species.huntingTips,
                            icon = Icons.Default.TrackChanges
                        )
                    }

                    if (species.identification.isNotEmpty()) {
                        SpeciesInfoSection(
                            title = "ამოცნობის ნიშნები:",
                            content = species.identification,
                            icon = Icons.Default.Visibility
                        )
                    }

                    // Legal notice pill
                    if (species.legalStatus.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ForestDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(16.dp))
                                Column {
                                    Text(
                                        text = "სამართლებრივი რეგულაცია:",
                                        color = WarningOrange,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = species.legalStatus,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "ნაკლების ჩვენება" else "სრული ინფორმაცია, ლიმიტები და წესები",
                    color = AccentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SpeciesInfoSection(
    title: String,
    content: String,
    icon: ImageVector
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = HuntingGreenLight, modifier = Modifier.size(14.dp))
            Text(text = title, color = TextGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = content, color = TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

fun getSpeciesCategoryIcon(category: String): ImageVector {
    return when (category) {
        "გადამფრენი ფრინველი", "წყალმცურავი ფრინველი", "ფრინველები" -> Icons.Default.Flight
        "ჩლიქოსნები" -> Icons.Default.Pets
        "მტაცებლები" -> Icons.Default.Shield
        else -> Icons.Default.Park
    }
}
