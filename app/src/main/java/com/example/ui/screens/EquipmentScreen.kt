package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.ChecklistWithItems
import com.example.data.model.EquipmentEntity
import com.example.data.model.HuntingChecklistEntity
import com.example.ui.theme.*
import com.example.viewmodel.MonadireViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class EquipmentTab(val titleKa: String, val icon: ImageVector) {
    CHECKLISTS("ჩეკლისტები & ჩალაგება", Icons.Default.Checklist),
    INVENTORY("არსენალი & ინვენტარი", Icons.Default.Shield)
}

@Composable
fun EquipmentScreen(
    viewModel: MonadireViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(EquipmentTab.CHECKLISTS) }

    // Checklists & Items state
    val checklistsWithItems by viewModel.checklistsWithItems.collectAsState()
    val selectedHuntTypeFilter by viewModel.selectedChecklistHuntType.collectAsState()
    val checklistSearchQuery by viewModel.checklistSearchQuery.collectAsState()

    // Equipment state
    val allEquipment by viewModel.allEquipment.collectAsState()
    val selectedEquipmentCategory by viewModel.selectedEquipmentCategory.collectAsState()

    // Dialog states
    var showCreateChecklistDialog by remember { mutableStateOf(false) }
    var itemDialogChecklistId by remember { mutableStateOf<Long?>(null) }
    var showAddGearDialog by remember { mutableStateOf(false) }
    var expandedChecklistId by remember { mutableStateOf<Long?>(null) }

    // Initialize expanded checklist on first load
    LaunchedEffect(checklistsWithItems) {
        if (expandedChecklistId == null && checklistsWithItems.isNotEmpty()) {
            expandedChecklistId = checklistsWithItems.first().checklist.id
        }
    }

    val huntTypeFilters = listOf(
        "ყველა" to "ყველა",
        "BIRD_HUNTING" to "ფრინველი",
        "BIG_GAME" to "დიდი ნადირი",
        "WATERFOWL" to "წყალმცურავი",
        "PREDATOR" to "მტაცებელი",
        "MOUNTAIN" to "მაღალმთიანი",
        "CUSTOM" to "საკუთარი"
    )

    val filteredChecklists = remember(checklistsWithItems, selectedHuntTypeFilter, checklistSearchQuery) {
        checklistsWithItems.filter { checklistWithItems ->
            val matchesType = if (selectedHuntTypeFilter == "ყველა") {
                true
            } else if (selectedHuntTypeFilter == "CUSTOM") {
                !checklistWithItems.checklist.isPreset || checklistWithItems.checklist.huntType == "CUSTOM"
            } else {
                checklistWithItems.checklist.huntType == selectedHuntTypeFilter
            }

            val matchesSearch = if (checklistSearchQuery.isBlank()) {
                true
            } else {
                checklistWithItems.checklist.title.contains(checklistSearchQuery, ignoreCase = true) ||
                        checklistWithItems.checklist.description.contains(checklistSearchQuery, ignoreCase = true) ||
                        checklistWithItems.items.any { it.title.contains(checklistSearchQuery, ignoreCase = true) }
            }

            matchesType && matchesSearch
        }
    }

    // Equipment categories
    val gearCategories = listOf("ყველა", "თოფი", "ოპტიკა", "ვაზნები", "ტანსაცმელი", "ფეხსაცმელი", "ზურგჩანთა", "ფანარი", "GPS", "სხვა")
    val filteredEquipment = remember(allEquipment, selectedEquipmentCategory) {
        if (selectedEquipmentCategory == "ყველა") allEquipment
        else allEquipment.filter { it.category == selectedEquipmentCategory }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ForestBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == EquipmentTab.CHECKLISTS) {
                        showCreateChecklistDialog = true
                    } else {
                        showAddGearDialog = true
                    }
                },
                containerColor = AccentGold,
                contentColor = ForestBlack,
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("equipment_fab_add")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(
                        text = if (selectedTab == EquipmentTab.CHECKLISTS) "ახალი ჩეკლისტი" else "ნივთის დამატება",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
        ) {
            // Screen Title Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ForestSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentGold.copy(alpha = 0.2f))
                                .border(1.dp, AccentGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == EquipmentTab.CHECKLISTS) Icons.Default.Checklist else Icons.Default.Shield,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ეკიპირება და სანადირო ჩეკლისტები",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "მოემზადეთ ნადირობისთვის: შექმენით სიები და მონიშნეთ ჩალაგებული ნივთები",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Top Tab Switcher
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ForestSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EquipmentTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) AccentGold else Color.Transparent)
                                    .clickable { selectedTab = tab }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) ForestBlack else TextSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = tab.titleKa,
                                        color = if (isSelected) ForestBlack else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: CHECKLISTS & PACKING
            if (selectedTab == EquipmentTab.CHECKLISTS) {
                // Hunt Type Filters
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(huntTypeFilters) { (typeKey, typeLabel) ->
                            val isSelected = selectedHuntTypeFilter == typeKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSelectedChecklistHuntType(typeKey) },
                                label = {
                                    Text(
                                        text = typeLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getHuntTypeIcon(typeKey),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentGold,
                                    selectedLabelColor = ForestBlack,
                                    selectedLeadingIconColor = ForestBlack,
                                    containerColor = ForestSurface,
                                    labelColor = TextPrimary,
                                    iconColor = AccentGold
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

                // Global Packing Summary Card
                item {
                    val totalChecklistsCount = filteredChecklists.size
                    val totalAllItems = filteredChecklists.sumOf { it.totalItems }
                    val totalPackedItems = filteredChecklists.sumOf { it.packedItems }
                    val overallProgress = if (totalAllItems == 0) 0f else totalPackedItems.toFloat() / totalAllItems.toFloat()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ForestSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Luggage,
                                        contentDescription = null,
                                        tint = AccentGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "ჩალაგების საერთო სტატუსი",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (overallProgress >= 1f && totalAllItems > 0) HuntingGreenDark else ForestDark,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (overallProgress >= 1f && totalAllItems > 0) HuntingGreenLight else AccentGold.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = "$totalPackedItems / $totalAllItems ნივთი (${(overallProgress * 100).toInt()}%)",
                                        color = if (overallProgress >= 1f && totalAllItems > 0) HuntingGreenLight else AccentGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { overallProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (overallProgress >= 1f) HuntingGreenLight else AccentGold,
                                trackColor = ForestDark,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$totalChecklistsCount აქტიური ჩეკლისტი",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )

                                val totalMandatoryMissing = filteredChecklists.sumOf { it.mandatoryMissingCount }
                                if (totalMandatoryMissing > 0) {
                                    Text(
                                        text = "⚠️ $totalMandatoryMissing სავალდებულო ნივთი აკლია",
                                        color = AlertRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (totalAllItems > 0 && overallProgress >= 1f) {
                                    Text(
                                        text = "✓ ყველა ნივთი ჩალაგებულია",
                                        color = HuntingGreenLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Checklists List
                if (filteredChecklists.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.ChecklistRtl,
                            title = "ჩეკლისტი ვერ მოიძებნა",
                            description = "დააჭირეთ „ახალი ჩეკლისტი“ ღილაკს ახალი სანადირო სიის შესაქმნელად."
                        )
                    }
                } else {
                    items(filteredChecklists, key = { it.checklist.id }) { checklistWithItems ->
                        val isExpanded = expandedChecklistId == checklistWithItems.checklist.id

                        HuntChecklistCard(
                            checklistWithItems = checklistWithItems,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedChecklistId = if (isExpanded) null else checklistWithItems.checklist.id
                            },
                            onToggleItemPacked = { itemId, isPacked ->
                                viewModel.toggleItemPacked(itemId, isPacked)
                            },
                            onSetAllPacked = { isPacked ->
                                viewModel.setAllItemsPacked(checklistWithItems.checklist.id, isPacked)
                            },
                            onAddItem = {
                                itemDialogChecklistId = checklistWithItems.checklist.id
                            },
                            onDeleteItem = { itemId ->
                                viewModel.deleteChecklistItem(itemId)
                            },
                            onDeleteChecklist = {
                                viewModel.deleteChecklist(checklistWithItems.checklist.id)
                            },
                            onShareChecklist = {
                                val shareText = buildChecklistShareText(checklistWithItems)
                                clipboardManager.setText(AnnotatedString(shareText))
                                Toast.makeText(context, "ჩეკლისტი დაკოპირდა ბუფერში!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // TAB 2: ARSENAL & EQUIPMENT INVENTORY
            if (selectedTab == EquipmentTab.INVENTORY) {
                // Maintenance Tip Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ForestSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = AccentGold, modifier = Modifier.size(22.dp))
                            Column {
                                Text(
                                    text = "იარაღის ტექნიკური მოვლა და რეგისტრაცია",
                                    color = AccentGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "შეინახეთ იარაღის სერიული ნომრები და ჩაინიშნეთ გეგმიური გაწმენდის თარიღები.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Equipment Category Chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(gearCategories) { cat ->
                            val isSelected = selectedEquipmentCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setEquipmentCategory(cat) },
                                label = { Text(cat, fontSize = 12.sp) },
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

                // Equipment Inventory List
                if (filteredEquipment.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.Inventory,
                            title = "ინვენტარი ცარიელია",
                            description = "დაამატეთ თქვენი სანადირო თოფი, ოპტიკა ან ეკიპირება ქვედა ღილაკით (+)."
                        )
                    }
                } else {
                    items(filteredEquipment, key = { it.id }) { gear ->
                        EquipmentInventoryCard(
                            equipment = gear,
                            onDelete = { viewModel.deleteEquipment(gear.id) }
                        )
                    }
                }
            }
        }
    }

    // DIALOG: Create Checklist
    if (showCreateChecklistDialog) {
        CreateChecklistDialog(
            onDismiss = { showCreateChecklistDialog = false },
            onCreate = { title, huntType, labelKa, desc, season, autoPopulate ->
                viewModel.createChecklist(title, huntType, labelKa, desc, season, autoPopulate)
                showCreateChecklistDialog = false
                Toast.makeText(context, "ჩეკლისტი წარმატებით შეიქმნა!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // DIALOG: Add Item to Checklist
    if (itemDialogChecklistId != null) {
        val checklistId = itemDialogChecklistId!!
        val currentChecklist = checklistsWithItems.find { it.checklist.id == checklistId }

        AddItemToChecklistDialog(
            checklistTitle = currentChecklist?.checklist?.title ?: "ჩეკლისტი",
            availableGuns = allEquipment,
            onDismiss = { itemDialogChecklistId = null },
            onAdd = { title, category, quantity, isMandatory, notes ->
                viewModel.addChecklistItem(checklistId, title, category, quantity, isMandatory, notes)
                itemDialogChecklistId = null
                Toast.makeText(context, "ნივთი დაემატა ჩეკლისტს!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // DIALOG: Add Equipment to Inventory
    if (showAddGearDialog) {
        AddEquipmentDialog(
            onDismiss = { showAddGearDialog = false },
            onAdd = { name, cat, brand, model, serial, pDate, mDate, notes ->
                viewModel.addEquipment(name, cat, brand, model, serial, pDate, mDate, notes)
                showAddGearDialog = false
                Toast.makeText(context, "ეკიპირება დაემატა არსენალში!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ------------------------------------------------------------------------------------------------
// HUNT CHECKLIST CARD COMPONENT
// ------------------------------------------------------------------------------------------------

@Composable
private fun HuntChecklistCard(
    checklistWithItems: ChecklistWithItems,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleItemPacked: (itemId: Long, isPacked: Boolean) -> Unit,
    onSetAllPacked: (isPacked: Boolean) -> Unit,
    onAddItem: () -> Unit,
    onDeleteItem: (itemId: Long) -> Unit,
    onDeleteChecklist: () -> Unit,
    onShareChecklist: () -> Unit
) {
    val checklist = checklistWithItems.checklist
    val items = checklistWithItems.items
    val isFullyPacked = checklistWithItems.isFullyPacked
    val progressPercent = checklistWithItems.progressPercent

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isFullyPacked) HuntingGreenLight.copy(alpha = 0.8f) else ForestCardBorder,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = ForestSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row (Icon, Title, Progress, Expand Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isFullyPacked) HuntingGreenDark.copy(alpha = 0.7f)
                                else AccentGold.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isFullyPacked) HuntingGreenLight else AccentGold.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFullyPacked) Icons.Default.CheckCircle else getHuntTypeIcon(checklist.huntType),
                            contentDescription = null,
                            tint = if (isFullyPacked) HuntingGreenLight else AccentGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = checklist.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ForestSurfaceVariant
                            ) {
                                Text(
                                    text = checklist.huntTypeLabelKa,
                                    color = AccentGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }

                            if (checklist.targetSeason.isNotEmpty()) {
                                Text(
                                    text = "• ${checklist.targetSeason}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onToggleExpand, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "ჩაკეცვა" else "გაშლა",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar and Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${checklistWithItems.packedItems} / ${checklistWithItems.totalItems} ჩალაგებულია",
                        color = if (isFullyPacked) HuntingGreenLight else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(${(progressPercent * 100).toInt()}%)",
                        color = if (isFullyPacked) HuntingGreenLight else AccentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (checklistWithItems.mandatoryMissingCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AlertRed.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "⚠️ ${checklistWithItems.mandatoryMissingCount} სავალდებულო",
                            color = AlertRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isFullyPacked) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = HuntingGreenDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HuntingGreenLight)
                    ) {
                        Text(
                            text = "✓ მზადაა გასასვლელად!",
                            color = HuntingGreenLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isFullyPacked) HuntingGreenLight else AccentGold,
                trackColor = ForestDark
            )

            // Expanded Items and Actions
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (checklist.description.isNotEmpty()) {
                        Text(
                            text = checklist.description,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    // Action Buttons Row (Mark all, Reset, Add Item, Share, Delete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mark All Packed / Unpacked Button
                        FilledTonalButton(
                            onClick = { onSetAllPacked(!isFullyPacked) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isFullyPacked) ForestSurfaceVariant else AccentGold.copy(alpha = 0.2f),
                                contentColor = if (isFullyPacked) TextSecondary else AccentGold
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isFullyPacked) Icons.Default.RemoveDone else Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFullyPacked) "განულება" else "ყველა",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Add Item Button
                        Button(
                            onClick = onAddItem,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentGold,
                                contentColor = ForestBlack
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ ნივთი", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Share Button
                        IconButton(
                            onClick = onShareChecklist,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ForestSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "გაზიარება",
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete Checklist Button (if custom or allowed)
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ForestSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "ჩეკლისტის წაშლა",
                                tint = AlertRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = ForestCardBorder.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Items List
                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ჩეკლისტში ნივთები არ არის. დაამატეთ „+ ნივთი“ ღილაკით.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Group items by packed status or category
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items.forEach { item ->
                                ChecklistItemRow(
                                    item = item,
                                    onTogglePacked = { isPacked ->
                                        onToggleItemPacked(item.id, isPacked)
                                    },
                                    onDelete = {
                                        onDeleteItem(item.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Checklist Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = ForestDark,
            title = {
                Text("ჩეკლისტის წაშლა", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "ნამდვილად გსურთ „${checklist.title}“-ის წაშლა თავისი ყველა ნივთით?",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteChecklist()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("წაშლა", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("გაუქმება", color = TextSecondary)
                }
            }
        )
    }
}

// ------------------------------------------------------------------------------------------------
// CHECKLIST ITEM ROW COMPONENT
// ------------------------------------------------------------------------------------------------

@Composable
private fun ChecklistItemRow(
    item: ChecklistItemEntity,
    onTogglePacked: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onTogglePacked(!item.isPacked) }
            .border(
                1.dp,
                if (item.isPacked) HuntingGreenLight.copy(alpha = 0.3f) else ForestCardBorder.copy(alpha = 0.6f),
                RoundedCornerShape(10.dp)
            ),
        color = if (item.isPacked) ForestSurfaceVariant.copy(alpha = 0.4f) else ForestSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = item.isPacked,
                    onCheckedChange = { onTogglePacked(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = HuntingGreenLight,
                        checkmarkColor = ForestBlack,
                        uncheckedColor = AccentGold
                    ),
                    modifier = Modifier.size(24.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.title,
                            color = if (item.isPacked) TextMuted else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (item.isPacked) FontWeight.Normal else FontWeight.SemiBold,
                            textDecoration = if (item.isPacked) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Tag
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ForestDark
                        ) {
                            Text(
                                text = item.category,
                                color = TextSecondary,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        // Quantity Tag
                        if (item.quantity.isNotBlank() && item.quantity != "1") {
                            Text(
                                text = "• ${item.quantity}",
                                color = TextGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Mandatory badge
                        if (item.isMandatory && !item.isPacked) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AlertRed.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "სავალდებულო",
                                    color = AlertRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    if (item.notes.isNotBlank()) {
                        Text(
                            text = item.notes,
                            color = if (item.isPacked) TextMuted.copy(alpha = 0.7f) else TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "ნივთის წაშლა",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// ARSENAL & INVENTORY ITEM CARD
// ------------------------------------------------------------------------------------------------

@Composable
private fun EquipmentInventoryCard(
    equipment: EquipmentEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = ForestSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ForestSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getEquipmentIcon(equipment.category),
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = equipment.name,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${equipment.brand} ${equipment.model}".trim().ifEmpty { equipment.category },
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "წაშლა", tint = AlertRed)
                }
            }

            if (equipment.serialNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "სერიული #: ${equipment.serialNumber}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            if (equipment.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = equipment.notes,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = ForestCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (equipment.nextMaintenanceDate.isNotBlank()) "შემდეგი მოვლა: ${equipment.nextMaintenanceDate}" else "სტატუსი: მზადყოფნაში",
                    color = HuntingGreenLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ForestSurfaceVariant
                ) {
                    Text(
                        text = equipment.category,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// CREATE CHECKLIST DIALOG
// ------------------------------------------------------------------------------------------------

@Composable
private fun CreateChecklistDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, huntType: String, labelKa: String, description: String, season: String, autoPopulate: Boolean) -> Unit
) {
    val huntTypes = listOf(
        Triple("BIRD_HUNTING", "ფრინველზე ნადირობა", "მწყერი, ქედანი, კაკაბი, ხოხობი"),
        Triple("BIG_GAME", "დიდი ნადირი (Big Game)", "გარეული ღორი, ირემი, შველი"),
        Triple("WATERFOWL", "წყალმცურავი ფრინველი", "იხვი, ბატი, მელოტა"),
        Triple("PREDATOR", "მტაცებლები", "მელა, ტურა, მგელი"),
        Triple("MOUNTAIN", "მაღალმთიანი ექსპედიცია", "ჯიხვი, არჩვი, მთის ნადირობა"),
        Triple("CUSTOM", "საკუთარი სია", "ინდივიდუალური სანადირო ჩეკლისტი")
    )

    var selectedTypeIndex by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf(huntTypes[0].second) }
    var description by remember { mutableStateOf("საველე აღჭურვილობა, საბუთები და იარაღი") }
    var targetSeason by remember { mutableStateOf("სექტემბერი - იანვარი") }
    var autoPopulateTemplate by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PostAdd, contentDescription = null, tint = AccentGold)
                Text("ახალი სანადირო ჩეკლისტი", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("აირჩიეთ ნადირობის ტიპი:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(huntTypes.indices.toList()) { index ->
                        val (typeKey, labelKa, _) = huntTypes[index]
                        val isSelected = selectedTypeIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTypeIndex = index
                                title = labelKa
                            },
                            label = { Text(labelKa, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(imageVector = getHuntTypeIcon(typeKey), contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGold,
                                selectedLabelColor = ForestBlack,
                                containerColor = ForestSurface,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ჩეკლისტის სათაური") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetSeason,
                    onValueChange = { targetSeason = it },
                    label = { Text("სეზონი / პერიოდი (მაგ. აგვისტო - დეკემბერი)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("აღწერა / სამიზნე სახეობები") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ForestSurfaceVariant)
                        .clickable { autoPopulateTemplate = !autoPopulateTemplate }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "რეკომენდებული ნივთების დამატება",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "ავტომატურად ჩაამატებს იარაღს, ვაზნებს, ჟილეტს და საბუთებს",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Checkbox(
                        checked = autoPopulateTemplate,
                        onCheckedChange = { autoPopulateTemplate = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentGold, checkmarkColor = ForestBlack)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val currentType = huntTypes[selectedTypeIndex]
                        onCreate(
                            title,
                            currentType.first,
                            currentType.second,
                            description,
                            targetSeason,
                            autoPopulateTemplate
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("შექმნა", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("გაუქმება", color = TextSecondary)
            }
        }
    )
}

// ------------------------------------------------------------------------------------------------
// ADD ITEM TO CHECKLIST DIALOG
// ------------------------------------------------------------------------------------------------

@Composable
private fun AddItemToChecklistDialog(
    checklistTitle: String,
    availableGuns: List<EquipmentEntity>,
    onDismiss: () -> Unit,
    onAdd: (title: String, category: String, quantity: String, isMandatory: Boolean, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("იარაღი & ვაზნები") }
    var quantity by remember { mutableStateOf("1 ცალი") }
    var isMandatory by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    val categories = listOf(
        "იარაღი & ვაზნები",
        "უსაფრთხოება & საბუთები",
        "ტანსაცმელი & ფეხსაცმელი",
        "ნავიგაცია & კავშირი",
        "ძაღლის აღჭურვილობა",
        "ბანაკი & კვება",
        "ოპტიკა & აქსესუარები"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Column {
                Text("ნივთის დამატება ჩეკლისტში", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(checklistTitle, color = AccentGold, fontSize = 12.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick pick from arsenal if any
                if (availableGuns.isNotEmpty()) {
                    Text("ჩემი არსენალიდან სწრაფი არჩევა:", color = TextSecondary, fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(availableGuns.take(5)) { gun ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ForestSurfaceVariant,
                                modifier = Modifier.clickable {
                                    title = gun.name
                                    selectedCategory = when (gun.category) {
                                        "თოფი", "ვაზნები" -> "იარაღი & ვაზნები"
                                        "ოპტიკა" -> "ოპტიკა & აქსესუარები"
                                        "GPS" -> "ნავიგაცია & კავშირი"
                                        else -> "ტანსაცმელი & ფეხსაცმელი"
                                    }
                                }
                            ) {
                                Text(
                                    text = "+ ${gun.name}",
                                    color = AccentGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ნივთის დასახელება") },
                    placeholder = { Text("მაგ. ვაზნები N9 (50 ცალი)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("კატეგორია:", color = TextSecondary, fontSize = 11.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGold,
                                selectedLabelColor = ForestBlack,
                                containerColor = ForestSurface,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("რაოდენობა") },
                        placeholder = { Text("1 ცალი / 50 ცალი") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = ForestCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("შენიშვნა / კომენტარი") },
                    placeholder = { Text("მაგ. თან იქონიეთ ორიგინალი") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ForestSurfaceVariant)
                        .clickable { isMandatory = !isMandatory }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("სავალდებულო ნივთი (კანონი / უსაფრთხოება)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("აღინიშნება გამაფრთხილებელი ნიშნით", color = TextSecondary, fontSize = 9.sp)
                    }
                    Checkbox(
                        checked = isMandatory,
                        onCheckedChange = { isMandatory = it },
                        colors = CheckboxDefaults.colors(checkedColor = AlertRed, checkmarkColor = TextPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, selectedCategory, quantity, isMandatory, notes)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("დამატება", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("გაუქმება", color = TextSecondary)
            }
        }
    )
}

// ------------------------------------------------------------------------------------------------
// ADD EQUIPMENT TO INVENTORY DIALOG
// ------------------------------------------------------------------------------------------------

@Composable
private fun AddEquipmentDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, cat: String, brand: String, model: String, serial: String, pDate: String, mDate: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("თოფი") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var nextMaintenanceDate by remember { mutableStateOf("2026-10-01") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("თოფი", "ოპტიკა", "ვაზნები", "ტანსაცმელი", "ფეხსაცმელი", "ზურგჩანთა", "ფანარი", "GPS", "სხვა")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Text("არსენალში ეკიპირების დამატება", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("დასახელება") },
                    placeholder = { Text("მაგ. Beretta Silver Pigeon 12/76") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("კატეგორია:", color = TextSecondary, fontSize = 11.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGold,
                                selectedLabelColor = ForestBlack,
                                containerColor = ForestSurface,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("ბრენდი") },
                        placeholder = { Text("Beretta") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = ForestCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("მოდელი / ყალიბი") },
                        placeholder = { Text("686 Silver") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = ForestCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    label = { Text("სერიული ნომერი (დაცულია)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nextMaintenanceDate,
                    onValueChange = { nextMaintenanceDate = it },
                    label = { Text("შემდეგი მოვლა (წწწწ-თთ-დდ)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("შენიშვნა / ჩოკები") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = ForestCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val pDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        onAdd(name, selectedCategory, brand, model, serialNumber, pDate, nextMaintenanceDate, notes)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("შენახვა", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("გაუქმება", color = TextSecondary)
            }
        }
    )
}

// ------------------------------------------------------------------------------------------------
// HELPER FUNCTIONS & FORMATTERS
// ------------------------------------------------------------------------------------------------

fun getHuntTypeIcon(huntType: String): ImageVector {
    return when (huntType) {
        "BIRD_HUNTING" -> Icons.Default.Flight
        "BIG_GAME" -> Icons.Default.Shield
        "WATERFOWL" -> Icons.Default.Water
        "PREDATOR" -> Icons.Default.Pets
        "MOUNTAIN" -> Icons.Default.Terrain
        else -> Icons.Default.Checklist
    }
}

fun getEquipmentIcon(category: String): ImageVector {
    return when (category) {
        "თოფი" -> Icons.Default.Shield
        "ოპტიკა" -> Icons.Default.Visibility
        "ვაზნები" -> Icons.Default.Adjust
        "ტანსაცმელი" -> Icons.Default.Checkroom
        "ფეხსაცმელი" -> Icons.Default.Hiking
        "ზურგჩანთა" -> Icons.Default.Backpack
        "ფანარი" -> Icons.Default.FlashlightOn
        "GPS" -> Icons.Default.Navigation
        else -> Icons.Default.Build
    }
}

private fun buildChecklistShareText(checklistWithItems: ChecklistWithItems): String {
    val sb = StringBuilder()
    sb.append("🎯 სანადირო ჩეკლისტი: ${checklistWithItems.checklist.title}\n")
    sb.append("📌 ტიპი: ${checklistWithItems.checklist.huntTypeLabelKa}\n")
    if (checklistWithItems.checklist.targetSeason.isNotEmpty()) {
        sb.append("🗓️ სეზონი: ${checklistWithItems.checklist.targetSeason}\n")
    }
    sb.append("📊 ჩალაგებულია: ${checklistWithItems.packedItems} / ${checklistWithItems.totalItems} (${(checklistWithItems.progressPercent * 100).toInt()}%)\n\n")

    val grouped = checklistWithItems.items.groupBy { it.category }
    grouped.forEach { (cat, items) ->
        sb.append("[$cat]\n")
        items.forEach { item ->
            val status = if (item.isPacked) "✅" else "⬜"
            val mandatory = if (item.isMandatory) " (სავალდებულო)" else ""
            sb.append("$status ${item.title} - ${item.quantity}$mandatory\n")
        }
        sb.append("\n")
    }
    sb.append("შექმნილია Monadire აპლიკაციით")
    return sb.toString()
}
