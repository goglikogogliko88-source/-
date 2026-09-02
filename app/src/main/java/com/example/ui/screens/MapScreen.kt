package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.HuntingSpotEntity
import com.example.ui.components.HuntingWeatherBottomSheet
import com.example.ui.components.MapWeatherFloatingBadge
import com.example.ui.theme.*
import com.example.viewmodel.MonadireViewModel
import okhttp3.OkHttpClient
import kotlin.math.*

enum class MapLayerType(val title: String, val subtitle: String, val icon: ImageVector) {
    SATELLITE("სატელიტი", "ArcGIS World Imagery", Icons.Default.SatelliteAlt),
    OPENSTREETMAP("OpenStreetMap", "დეტალური ქუჩები & ბუნება", Icons.Default.Map),
    OPENTOPOMAP("OpenTopoMap", "ტოპოგრაფიული რელიეფი", Icons.Default.Terrain),
    TACTICAL_DARK("ტაქტიკური ბნელი", "CartoDB Dark Matter", Icons.Default.DarkMode)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MonadireViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val allSpots by viewModel.allSpots.collectAsState()
    val selectedCategory by viewModel.selectedSpotCategory.collectAsState()
    val detailedWeather by viewModel.detailedWeather.collectAsState()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsState()

    var selectedSpot by remember { mutableStateOf<HuntingSpotEntity?>(null) }
    var editingSpot by remember { mutableStateOf<HuntingSpotEntity?>(null) }
    var spotToDelete by remember { mutableStateOf<HuntingSpotEntity?>(null) }

    var showAddSpotDialog by remember { mutableStateOf(false) }
    var showSpotsListSheet by remember { mutableStateOf(false) }
    var showLayerSelectionSheet by remember { mutableStateOf(false) }
    var showPrivacyInfoDialog by remember { mutableStateOf(false) }
    var showWeatherSheet by remember { mutableStateOf(false) }

    // Selected Map Tile Layer (Default: Satellite for true hunting outdoor awareness)
    var selectedMapLayer by remember { mutableStateOf(MapLayerType.SATELLITE) }

    // Map Transformations (Pan Offset & Zoom Level)
    var mapZoom by remember { mutableFloatStateOf(13.5f) }
    var mapOffset by remember { mutableStateOf(Offset.Zero) }

    // Hunter GPS Position (Defaults to Georgia Sagarejo / Gardabani hunting fields)
    var hunterLat by remember { mutableDoubleStateOf(41.7335) }
    var hunterLng by remember { mutableDoubleStateOf(45.3312) }
    var hunterElevation by remember { mutableIntStateOf(720) }
    var isGpsActive by remember { mutableStateOf(false) }
    var gpsAccuracyMeters by remember { mutableFloatStateOf(6.0f) }

    // Map Tap Location (for placing custom pins by tapping)
    var tappedMapCoordinate by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // Distance Measurement Tool State
    var isMeasurementMode by remember { mutableStateOf(false) }
    val measurementPoints = remember { mutableStateListOf<Pair<Double, Double>>() }

    // Direct Active Navigation State
    var navigatingToSpot by remember { mutableStateOf<HuntingSpotEntity?>(null) }

    // Search query for spots
    var searchQuery by remember { mutableStateOf("") }

    // Dedicated OkHttpClient with explicit User-Agent compliant with OpenStreetMap and tile server policies
    val tileImageLoader = remember {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "MonadireApp/1.0 (Android; Georgian Hunter GIS; contact: dls.service@yahoo.com)")
                    .header("Accept", "image/webp,image/png,image/jpeg,image/*;q=0.8")
                    .build()
                chain.proceed(request)
            }
            .build()

        ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .build()
    }

    // Live GPS Location Manager & Listener
    val locationListener = remember {
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                hunterLat = location.latitude
                hunterLng = location.longitude
                if (location.hasAltitude()) {
                    hunterElevation = location.altitude.toInt()
                }
                if (location.hasAccuracy()) {
                    gpsAccuracyMeters = location.accuracy
                }
                isGpsActive = true
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) { isGpsActive = true }
            override fun onProviderDisabled(provider: String) {}
        }
    }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            isGpsActive = true
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastKnown = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastKnown != null) {
                    hunterLat = lastKnown.latitude
                    hunterLng = lastKnown.longitude
                    hunterElevation = lastKnown.altitude.toInt().coerceAtLeast(300)
                    gpsAccuracyMeters = lastKnown.accuracy
                }
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    3f,
                    locationListener
                )
                Toast.makeText(context, "GPS ლოკაცია წარმატებით ჩაირთო", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                // Ignore
            }
        } else {
            Toast.makeText(context, "GPS ნებართვა არ არის მინიჭებული. რუკა გახსნილია სტანდარტულ კოორდინატებზე", Toast.LENGTH_LONG).show()
        }
    }

    // Auto-request location updates if permission already granted
    DisposableEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        if (fineGranted || coarseGranted) {
            isGpsActive = true
            try {
                val lastKnown = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastKnown != null) {
                    hunterLat = lastKnown.latitude
                    hunterLng = lastKnown.longitude
                    hunterElevation = lastKnown.altitude.toInt().coerceAtLeast(300)
                    gpsAccuracyMeters = lastKnown.accuracy
                }
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    3f,
                    locationListener
                )
            } catch (e: SecurityException) {
                // Ignore
            }
        }

        onDispose {
            try {
                locationManager?.removeUpdates(locationListener)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Automatically fetch weather for initial or updated GPS coordinates
    LaunchedEffect(hunterLat, hunterLng) {
        viewModel.fetchWeatherForCoordinates(hunterLat, hunterLng, sourceLabel = "GPS ლოკაცია")
    }

    val categories = listOf("ყველა", "სანადირო ადგილი", "წყარო", "პარკინგი", "საფრთხე", "კარავი", "ტყე", "გზა", "რჩეულები")

    val filteredSpots = remember(allSpots, selectedCategory, searchQuery) {
        var list = allSpots
        if (selectedCategory == "რჩეულები") {
            list = list.filter { it.isFavorite }
        } else if (selectedCategory != "ყველა") {
            list = list.filter { it.category == selectedCategory }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
        list
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestBlack)
    ) {
        // Tile Mapping Engine with Seamless Mercator Projection & Tactical Overlays
        TileMercatorMap(
            layerType = selectedMapLayer,
            tileImageLoader = tileImageLoader,
            spots = filteredSpots,
            selectedSpot = selectedSpot,
            navigatingSpot = navigatingToSpot,
            hunterLat = hunterLat,
            hunterLng = hunterLng,
            mapZoom = mapZoom,
            mapOffset = mapOffset,
            isMeasurementMode = isMeasurementMode,
            measurementPoints = measurementPoints,
            onTransform = { pan, zoomFactor ->
                mapOffset += pan
                mapZoom = (mapZoom + (zoomFactor - 1.0f) * 2.0f).coerceIn(9.0f, 18.0f)
            },
            onSpotClicked = { spot ->
                selectedSpot = spot
            },
            onMapTapped = { lat, lng ->
                if (isMeasurementMode) {
                    measurementPoints.add(Pair(lat, lng))
                } else {
                    tappedMapCoordinate = Pair(lat, lng)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top HUD & Controls Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Coordinate & GPS HUD Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ForestCardBorder, RoundedCornerShape(12.dp)),
                color = ForestDark.copy(alpha = 0.94f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            if (!isGpsActive) {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                Toast.makeText(context, "GPS ლოკაცია აქტიურია (სიზუსტე: ±${gpsAccuracyMeters.toInt()}მ)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isGpsActive) HuntingGreenPrimary.copy(alpha = 0.25f) else WarningOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isGpsActive) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                                contentDescription = "GPS",
                                tint = if (isGpsActive) HuntingGreenLight else WarningOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = String.format(java.util.Locale.US, "GPS: %.4f°N, %.4f°E", hunterLat, hunterLng),
                                color = TextPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isGpsActive) "აქტიურია (±${gpsAccuracyMeters.toInt()}მ) • ${hunterElevation}მ ზ.დ." else "დააჭირეთ GPS-ის გასააქტიურებლად",
                                color = if (isGpsActive) HuntingGreenLight else WarningOrange,
                                fontSize = 9.5.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Weather & Wind Forecast Quick Button
                        IconButton(
                            onClick = { showWeatherSheet = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "ამინდი და ქარი",
                                tint = AccentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Map Layer Selector Button
                        IconButton(
                            onClick = { showLayerSelectionSheet = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "რუკის ფენები",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Spots List Button
                        IconButton(
                            onClick = { showSpotsListSheet = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "წერტილების სია",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Local Privacy Dialog Button
                        IconButton(
                            onClick = { showPrivacyInfoDialog = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "კონფიდენციალურობა",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Weather & Wind Floating HUD Badge
            MapWeatherFloatingBadge(
                weather = detailedWeather,
                isLoading = isWeatherLoading,
                onClick = { showWeatherSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            // Category Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSpotCategory(cat) },
                        label = { Text(cat, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (cat == "რჩეულები") Icons.Default.Star else getCategoryIcon(cat),
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSelected) ForestBlack else if (cat == "რჩეულები") AccentGold else getCategoryColor(cat)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGold,
                            selectedLabelColor = ForestBlack,
                            containerColor = ForestDark.copy(alpha = 0.92f),
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

            // Measurement Mode Active Banner
            AnimatedVisibility(
                visible = isMeasurementMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val totalDistKm = remember(measurementPoints, hunterLat, hunterLng) {
                    var dist = 0.0
                    if (measurementPoints.size >= 2) {
                        for (i in 0 until measurementPoints.size - 1) {
                            dist += calculateDistance(
                                measurementPoints[i].first, measurementPoints[i].second,
                                measurementPoints[i + 1].first, measurementPoints[i + 1].second
                            )
                        }
                    } else if (measurementPoints.size == 1) {
                        dist = calculateDistance(hunterLat, hunterLng, measurementPoints[0].first, measurementPoints[0].second)
                    }
                    dist
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentGold.copy(alpha = 0.95f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Straighten, contentDescription = null, tint = ForestBlack, modifier = Modifier.size(20.dp))
                            Column {
                                Text("მანძილის საზომი რეჟიმი", color = ForestBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                val desc = if (measurementPoints.isEmpty()) {
                                    "შეეხეთ რუკას წერტილების დასამატებლად"
                                } else if (measurementPoints.size == 1) {
                                    String.format(java.util.Locale.US, "GPS-იდან: %.2f კმ (~%d წთ ფეხით)", totalDistKm, (totalDistKm / 4.0 * 60).toInt())
                                } else {
                                    String.format(java.util.Locale.US, "%d წერტილი: %.2f კმ (~%d წთ)", measurementPoints.size, totalDistKm, (totalDistKm / 4.0 * 60).toInt())
                                }
                                Text(desc, color = ForestBlack.copy(alpha = 0.85f), fontSize = 10.5.sp)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (measurementPoints.isNotEmpty()) {
                                IconButton(
                                    onClick = { measurementPoints.clear() },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "გასუფთავება", tint = ForestBlack, modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(
                                onClick = {
                                    isMeasurementMode = false
                                    measurementPoints.clear()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "გამოსვლა", tint = ForestBlack, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Direct Navigation Active Banner
            AnimatedVisibility(
                visible = navigatingToSpot != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (navigatingToSpot != null) {
                    val target = navigatingToSpot!!
                    val distanceKm = calculateDistance(hunterLat, hunterLng, target.latitude, target.longitude)
                    val bearing = calculateBearing(hunterLat, hunterLng, target.latitude, target.longitude)
                    val elevDiff = target.elevationMeters - hunterElevation

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HuntingGreenDark.copy(alpha = 0.96f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HuntingGreenLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(HuntingGreenPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = ForestBlack,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .rotate(bearing.toFloat())
                                    )
                                }

                                Column {
                                    Text(
                                        text = "გეზი: ${target.name}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()}მ დარჩენილია" else String.format(java.util.Locale.US, "%.2f კმ დარჩენილია", distanceKm),
                                            color = HuntingGreenLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "• აზიმუტი: $bearing°",
                                            color = AccentGold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { navigatingToSpot = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "შეწყვეტა", tint = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Floating Map Controls (Recenter GPS, Zoom +/- Controls, Distance Measuring Tool, Add Pin)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Layer Switch Quick Indicator
            SmallFloatingActionButton(
                onClick = { showLayerSelectionSheet = true },
                containerColor = ForestDark.copy(alpha = 0.95f),
                contentColor = AccentGold,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(selectedMapLayer.icon, contentDescription = "ფენები", modifier = Modifier.size(20.dp))
            }

            // Recenter onto Hunter GPS Location
            SmallFloatingActionButton(
                onClick = {
                    if (!isGpsActive) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                    mapOffset = Offset.Zero
                    mapZoom = 14.0f
                    Toast.makeText(context, "რუკა ორიენტირებულია GPS ლოკაციაზე", Toast.LENGTH_SHORT).show()
                },
                containerColor = if (isGpsActive) HuntingGreenPrimary else ForestDark.copy(alpha = 0.95f),
                contentColor = if (isGpsActive) ForestBlack else AccentGold,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "ჩემი ლოკაცია", modifier = Modifier.size(20.dp))
            }

            // Zoom In (+)
            SmallFloatingActionButton(
                onClick = {
                    mapZoom = (mapZoom + 1.0f).coerceAtMost(18.0f)
                },
                containerColor = ForestDark.copy(alpha = 0.95f),
                contentColor = TextPrimary,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "მოახლოება", modifier = Modifier.size(20.dp))
            }

            // Zoom Out (-)
            SmallFloatingActionButton(
                onClick = {
                    mapZoom = (mapZoom - 1.0f).coerceAtLeast(9.0f)
                },
                containerColor = ForestDark.copy(alpha = 0.95f),
                contentColor = TextPrimary,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "დაშორება", modifier = Modifier.size(20.dp))
            }

            // Distance Measurement Tool Toggle
            SmallFloatingActionButton(
                onClick = {
                    isMeasurementMode = !isMeasurementMode
                    if (!isMeasurementMode) {
                        measurementPoints.clear()
                    } else {
                        Toast.makeText(context, "შეეხეთ რუკას მანძილის გასაზომად", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = if (isMeasurementMode) AccentGold else ForestDark.copy(alpha = 0.95f),
                contentColor = if (isMeasurementMode) ForestBlack else AccentGold,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Straighten, contentDescription = "მანძილის გაზომვა", modifier = Modifier.size(20.dp))
            }

            // Weather & Wind Forecast Floating Action Button
            SmallFloatingActionButton(
                onClick = {
                    if (tappedMapCoordinate != null) {
                        viewModel.fetchWeatherForCoordinates(
                            tappedMapCoordinate!!.first,
                            tappedMapCoordinate!!.second,
                            sourceLabel = "მონიშნული წერტილი"
                        )
                    } else {
                        viewModel.fetchWeatherForCoordinates(hunterLat, hunterLng, sourceLabel = "GPS ლოკაცია")
                    }
                    showWeatherSheet = true
                },
                containerColor = ForestDark.copy(alpha = 0.95f),
                contentColor = InfoBlue,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Air, contentDescription = "ამინდი და ქარი", modifier = Modifier.size(20.dp))
            }

            // Add Custom Spot FAB
            FloatingActionButton(
                onClick = {
                    showAddSpotDialog = true
                },
                containerColor = AccentGold,
                contentColor = ForestBlack,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("fab_add_spot")
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = "წერტილის დამატება", modifier = Modifier.size(24.dp))
            }
        }

        // Tapped Coordinate Placement Prompt Banner
        if (tappedMapCoordinate != null && !isMeasurementMode) {
            val tapCoord = tappedMapCoordinate!!
            val tapDistKm = calculateDistance(hunterLat, hunterLng, tapCoord.first, tapCoord.second)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ForestDark.copy(alpha = 0.96f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 84.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = AccentGold, modifier = Modifier.size(24.dp))
                        Column {
                            Text("მონიშნული კოორდინატი", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = String.format(java.util.Locale.US, "%.4f°N, %.4f°E (%.1f კმ GPS-იდან)", tapCoord.first, tapCoord.second, tapDistKm),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Weather at Tapped Coordinate
                        OutlinedButton(
                            onClick = {
                                viewModel.fetchWeatherForCoordinates(
                                    tapCoord.first,
                                    tapCoord.second,
                                    sourceLabel = "მონიშნული კოორდინატი"
                                )
                                showWeatherSheet = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = InfoBlue),
                            border = androidx.compose.foundation.BorderStroke(1.dp, InfoBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Air, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ამინდი", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                showAddSpotDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("შენახვა", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { tappedMapCoordinate = null }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "დახურვა", tint = TextSecondary)
                        }
                    }
                }
            }
        }

        // Bottom Selected Spot Detail Card
        if (selectedSpot != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, bottom = 80.dp)
            ) {
                SpotDetailFloatingCard(
                    spot = selectedSpot!!,
                    hunterLat = hunterLat,
                    hunterLng = hunterLng,
                    hunterElevation = hunterElevation,
                    onFavoriteToggle = {
                        viewModel.toggleSpotFavorite(selectedSpot!!)
                        selectedSpot = selectedSpot!!.copy(isFavorite = !selectedSpot!!.isFavorite)
                    },
                    onEdit = {
                        editingSpot = selectedSpot
                    },
                    onDelete = {
                        spotToDelete = selectedSpot
                    },
                    onNavigate = {
                        navigatingToSpot = selectedSpot
                        selectedSpot = null
                    },
                    onExternalNavigate = {
                        openExternalMapNavigation(context, selectedSpot!!.latitude, selectedSpot!!.longitude, selectedSpot!!.name)
                    },
                    onCheckWeather = {
                        viewModel.fetchWeatherForCoordinates(
                            selectedSpot!!.latitude,
                            selectedSpot!!.longitude,
                            sourceLabel = selectedSpot!!.name
                        )
                        showWeatherSheet = true
                    },
                    onClose = { selectedSpot = null }
                )
            }
        }
    }

    // Add Spot Dialog
    if (showAddSpotDialog) {
        val defaultLat = tappedMapCoordinate?.first ?: hunterLat
        val defaultLng = tappedMapCoordinate?.second ?: hunterLng

        AddOrEditHuntingSpotDialog(
            spotToEdit = null,
            defaultLat = defaultLat,
            defaultLng = defaultLng,
            defaultElevation = hunterElevation,
            onDismiss = {
                showAddSpotDialog = false
                tappedMapCoordinate = null
            },
            onSave = { name, cat, lat, lng, elev, notes, isFav ->
                viewModel.addSpot(name, cat, lat, lng, elev, notes)
                showAddSpotDialog = false
                tappedMapCoordinate = null
                Toast.makeText(context, "სანადირო წერტილი შენახულია ლოკალურად", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Spot Dialog
    if (editingSpot != null) {
        AddOrEditHuntingSpotDialog(
            spotToEdit = editingSpot,
            defaultLat = editingSpot!!.latitude,
            defaultLng = editingSpot!!.longitude,
            defaultElevation = editingSpot!!.elevationMeters,
            onDismiss = { editingSpot = null },
            onSave = { name, cat, lat, lng, elev, notes, isFav ->
                val updated = editingSpot!!.copy(
                    name = name,
                    category = cat,
                    latitude = lat,
                    longitude = lng,
                    elevationMeters = elev,
                    notes = notes,
                    isFavorite = isFav
                )
                viewModel.updateSpot(updated)
                if (selectedSpot?.id == updated.id) {
                    selectedSpot = updated
                }
                editingSpot = null
                Toast.makeText(context, "წერტილი წარმატებით განახლდა", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    if (spotToDelete != null) {
        AlertDialog(
            onDismissRequest = { spotToDelete = null },
            containerColor = ForestDark,
            title = {
                Text("წერტილის წაშლა", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "ნამდვილად გსურთ წაშალოთ წერტილი \"${spotToDelete!!.name}\"? ეს მოქმედება შეუქცევადია.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSpot(spotToDelete!!.id)
                        if (selectedSpot?.id == spotToDelete!!.id) {
                            selectedSpot = null
                        }
                        if (navigatingToSpot?.id == spotToDelete!!.id) {
                            navigatingToSpot = null
                        }
                        spotToDelete = null
                        Toast.makeText(context, "წერტილი წაიშალა", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("წაშლა", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { spotToDelete = null }) {
                    Text("გაუქმება", color = TextSecondary)
                }
            }
        )
    }

    // Spots List Bottom Sheet
    if (showSpotsListSheet) {
        SpotsListModalBottomSheet(
            spots = allSpots,
            hunterLat = hunterLat,
            hunterLng = hunterLng,
            onSelectSpot = { spot ->
                selectedSpot = spot
                showSpotsListSheet = false
            },
            onNavigate = { spot ->
                navigatingToSpot = spot
                showSpotsListSheet = false
            },
            onDelete = { spot ->
                spotToDelete = spot
            },
            onDismiss = { showSpotsListSheet = false }
        )
    }

    // Map Layer Selection Bottom Sheet
    if (showLayerSelectionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLayerSelectionSheet = false },
            containerColor = ForestDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = AccentGold) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "რუკის ფენის არჩევა",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "სატელიტური, ტოპოგრაფიული და ქუჩების ფენები ჩაიტვირთება ავტომატურად GPS კოორდინატებზე",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                MapLayerType.values().forEach { layer ->
                    val isSelected = selectedMapLayer == layer
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedMapLayer = layer
                                showLayerSelectionSheet = false
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) HuntingGreenDark else ForestSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentGold else ForestCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) AccentGold else ForestSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = layer.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) ForestBlack else TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = layer.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                    Text(
                                        text = layer.subtitle,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Comprehensive Hunting Weather & Wind Bottom Sheet
    if (showWeatherSheet) {
        HuntingWeatherBottomSheet(
            weather = detailedWeather,
            isLoading = isWeatherLoading,
            currentHunterLat = hunterLat,
            currentHunterLng = hunterLng,
            tappedLat = tappedMapCoordinate?.first,
            tappedLng = tappedMapCoordinate?.second,
            onRefreshGpsWeather = {
                viewModel.fetchWeatherForCoordinates(hunterLat, hunterLng, sourceLabel = "GPS ლოკაცია")
            },
            onRefreshTappedWeather = { lat, lng ->
                viewModel.fetchWeatherForCoordinates(lat, lng, sourceLabel = "მონიშნული წერტილი")
            },
            onSelectRegion = { lat, lng, name ->
                viewModel.fetchWeatherForCoordinates(lat, lng, sourceLabel = name)
            },
            onDismiss = { showWeatherSheet = false }
        )
    }

    // Privacy Info Dialog
    if (showPrivacyInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyInfoDialog = false },
            containerColor = ForestDark,
            icon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AccentGold, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("ლოკალური კონფიდენციალურობა", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "თქვენი სანადირო წერტილები, წყაროები და პირადი ჩანიშვნები ინახება მხოლოდ თქვენს ტელეფონში (Room Local Database).",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        "• არანაირი გარე სერვერული სინქრონიზაცია მონადირის ნებართვის გარეშე.\n• სრული ოფლაინ ფუნქციონალი მთაში ინტერნეტის არარსებობისას.\n• წერტილები ხელმისაწვდომია მხოლოდ ამ მოწყობილობაზე.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("გასაგებია", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * High-performance Web Mercator Slippy Map Tile Renderer with custom Coil ImageLoader
 */
@Composable
private fun TileMercatorMap(
    layerType: MapLayerType,
    tileImageLoader: ImageLoader,
    spots: List<HuntingSpotEntity>,
    selectedSpot: HuntingSpotEntity?,
    navigatingSpot: HuntingSpotEntity?,
    hunterLat: Double,
    hunterLng: Double,
    mapZoom: Float,
    mapOffset: Offset,
    isMeasurementMode: Boolean,
    measurementPoints: List<Pair<Double, Double>>,
    onTransform: (pan: Offset, zoomFactor: Float) -> Unit,
    onSpotClicked: (HuntingSpotEntity) -> Unit,
    onMapTapped: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val intZoom = mapZoom.toInt().coerceIn(8, 18)
    val subZoomScale = (2.0.pow((mapZoom - intZoom).toDouble())).toFloat()

    // Pulse animation for hunter radar & danger zones
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onTransform(pan, zoom)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val screenWidth = size.width.toFloat()
                    val screenHeight = size.height.toFloat()

                    val centerTileX = lon2tileX(hunterLng, intZoom)
                    val centerTileY = lat2tileY(hunterLat, intZoom)

                    val pxFromCenterX = (tapOffset.x - (screenWidth / 2f + mapOffset.x)) / (256f * subZoomScale)
                    val pxFromCenterY = (tapOffset.y - (screenHeight / 2f + mapOffset.y)) / (256f * subZoomScale)

                    val targetTileX = centerTileX + pxFromCenterX
                    val targetTileY = centerTileY + pxFromCenterY

                    val tappedLng = tileX2lon(targetTileX, intZoom)
                    val tappedLat = tileY2lat(targetTileY, intZoom)

                    onMapTapped(tappedLat, tappedLng)
                }
            }
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current.density

        // Hunter center in tile space
        val centerTileX = lon2tileX(hunterLng, intZoom)
        val centerTileY = lat2tileY(hunterLat, intZoom)

        // Viewport center in pixels
        val centerScreenPxX = screenWidthPx / 2f + mapOffset.x
        val centerScreenPxY = screenHeightPx / 2f + mapOffset.y

        val tileSizePx = 256f * subZoomScale * density

        // Determine range of visible tiles
        val tilesAcross = ceil(screenWidthPx / (256f * density)).toInt() + 2
        val tilesDown = ceil(screenHeightPx / (256f * density)).toInt() + 2

        val minTileX = floor(centerTileX - tilesAcross / 2.0).toInt()
        val maxTileX = ceil(centerTileX + tilesAcross / 2.0).toInt()
        val minTileY = floor(centerTileY - tilesDown / 2.0).toInt()
        val maxTileY = ceil(centerTileY + tilesDown / 2.0).toInt()

        val maxTileIndex = (1 shl intZoom) - 1

        // Render Base Map Raster Tiles
        Box(modifier = Modifier.fillMaxSize()) {
            for (tx in minTileX..maxTileX) {
                for (ty in minTileY..maxTileY) {
                    val wrappedTx = ((tx % (1 shl intZoom)) + (1 shl intZoom)) % (1 shl intZoom)
                    if (ty in 0..maxTileIndex) {
                        val tileOffsetX = centerScreenPxX + (tx - centerTileX).toFloat() * (256f * density * subZoomScale)
                        val tileOffsetY = centerScreenPxY + (ty - centerTileY).toFloat() * (256f * density * subZoomScale)

                        val tileUrl = getTileUrl(layerType, intZoom, wrappedTx, ty)

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(tileUrl)
                                .crossfade(true)
                                .build(),
                            imageLoader = tileImageLoader,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .offset {
                                    IntOffset(tileOffsetX.roundToInt(), tileOffsetY.roundToInt())
                                }
                                .size((256f * subZoomScale).dp)
                        )
                    }
                }
            }

            // Subtle dark tactical tint for high-contrast visibility when on satellite or light maps
            if (layerType == MapLayerType.SATELLITE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
            } else if (layerType == MapLayerType.OPENSTREETMAP) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.08f))
                )
            }
        }

        // Vector Canvas Overlay (Bearing Lines, Polylines, Radar, Elevation & Danger Zones)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw Danger Zones
            spots.filter { it.category == "საფრთხე" }.forEach { dangerSpot ->
                val dangerTileX = lon2tileX(dangerSpot.longitude, intZoom)
                val dangerTileY = lat2tileY(dangerSpot.latitude, intZoom)

                val dangerScreenX = centerScreenPxX + (dangerTileX - centerTileX).toFloat() * (256f * density * subZoomScale)
                val dangerScreenY = centerScreenPxY + (dangerTileY - centerTileY).toFloat() * (256f * density * subZoomScale)
                val dangerCenter = Offset(dangerScreenX, dangerScreenY)

                drawCircle(
                    color = AlertRed.copy(alpha = 0.18f),
                    radius = 35f * density * subZoomScale,
                    center = dangerCenter
                )
                drawCircle(
                    color = AlertRed.copy(alpha = 0.45f * (1f - pulseRadius)),
                    radius = (35f + 25f * pulseRadius) * density * subZoomScale,
                    center = dangerCenter,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = AlertRed,
                    radius = 35f * density * subZoomScale,
                    center = dangerCenter,
                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f))
                )
            }

            // Draw Measurement Polyline
            if (isMeasurementMode && measurementPoints.isNotEmpty()) {
                val polyPath = Path()
                measurementPoints.forEachIndexed { index, pt ->
                    val ptTileX = lon2tileX(pt.second, intZoom)
                    val ptTileY = lat2tileY(pt.first, intZoom)
                    val ptScreenX = centerScreenPxX + (ptTileX - centerTileX).toFloat() * (256f * density * subZoomScale)
                    val ptScreenY = centerScreenPxY + (ptTileY - centerTileY).toFloat() * (256f * density * subZoomScale)
                    val ptOffset = Offset(ptScreenX, ptScreenY)

                    if (index == 0) {
                        polyPath.moveTo(ptOffset.x, ptOffset.y)
                    } else {
                        polyPath.lineTo(ptOffset.x, ptOffset.y)
                    }

                    drawCircle(
                        color = AccentGold,
                        radius = 6f * density,
                        center = ptOffset
                    )
                    drawCircle(
                        color = ForestBlack,
                        radius = 3f * density,
                        center = ptOffset
                    )
                }

                if (measurementPoints.size == 1) {
                    val pt0 = measurementPoints[0]
                    val pt0TileX = lon2tileX(pt0.second, intZoom)
                    val pt0TileY = lat2tileY(pt0.first, intZoom)
                    val pt0Offset = Offset(
                        centerScreenPxX + (pt0TileX - centerTileX).toFloat() * (256f * density * subZoomScale),
                        centerScreenPxY + (pt0TileY - centerTileY).toFloat() * (256f * density * subZoomScale)
                    )

                    drawLine(
                        color = AccentGold,
                        start = Offset(centerScreenPxX, centerScreenPxY),
                        end = pt0Offset,
                        strokeWidth = 3f * density,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                    )
                } else if (measurementPoints.size >= 2) {
                    drawPath(
                        path = polyPath,
                        color = AccentGold,
                        style = Stroke(width = 3.5f * density, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 6f), 0f))
                    )
                }
            }

            // Draw Navigation Vector Line (from Hunter GPS to Target Spot)
            if (navigatingSpot != null) {
                val targetTileX = lon2tileX(navigatingSpot.longitude, intZoom)
                val targetTileY = lat2tileY(navigatingSpot.latitude, intZoom)
                val targetOffset = Offset(
                    centerScreenPxX + (targetTileX - centerTileX).toFloat() * (256f * density * subZoomScale),
                    centerScreenPxY + (targetTileY - centerTileY).toFloat() * (256f * density * subZoomScale)
                )

                drawLine(
                    color = HuntingGreenLight,
                    start = Offset(centerScreenPxX, centerScreenPxY),
                    end = targetOffset,
                    strokeWidth = 3.5f * density,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), 0f)
                )
            }

            // Draw Hunter Location Marker (Pulsing Radar Circle)
            val hunterCenter = Offset(centerScreenPxX, centerScreenPxY)
            drawCircle(
                color = HuntingGreenPrimary.copy(alpha = 0.25f * (1f - pulseRadius)),
                radius = (24f + 20f * pulseRadius) * density,
                center = hunterCenter
            )
            drawCircle(
                color = HuntingGreenPrimary.copy(alpha = 0.4f),
                radius = 16f * density,
                center = hunterCenter
            )
            drawCircle(
                color = AccentGold,
                radius = 7f * density,
                center = hunterCenter
            )
            drawCircle(
                color = ForestBlack,
                radius = 3.5f * density,
                center = hunterCenter
            )
        }

        // Overlay Interactive Spot Pin Markers
        spots.forEach { spot ->
            val spotTileX = lon2tileX(spot.longitude, intZoom)
            val spotTileY = lat2tileY(spot.latitude, intZoom)

            val spotScreenX = centerScreenPxX + (spotTileX - centerTileX).toFloat() * (256f * density * subZoomScale)
            val spotScreenY = centerScreenPxY + (spotTileY - centerTileY).toFloat() * (256f * density * subZoomScale)

            val isSelected = selectedSpot?.id == spot.id
            val isNavigating = navigatingSpot?.id == spot.id

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (spotScreenX - 45f * density).roundToInt(),
                            (spotScreenY - 35f * density).roundToInt()
                        )
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSpotClicked(spot) }
                    .background(
                        if (isNavigating) HuntingGreenPrimary
                        else if (isSelected) AccentGold
                        else getCategoryColor(spot.category)
                    )
                    .border(
                        1.2.dp,
                        if (isSelected || isNavigating) Color.White else ForestBlack.copy(alpha = 0.7f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = getCategoryIcon(spot.category),
                        contentDescription = null,
                        tint = if (isSelected || isNavigating) ForestBlack else Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = spot.name,
                        color = if (isSelected || isNavigating) ForestBlack else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (spot.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isSelected || isNavigating) ForestBlack else AccentGold,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Slippy map URL provider with fallbacks
 */
private fun getTileUrl(layer: MapLayerType, z: Int, x: Int, y: Int): String {
    return when (layer) {
        MapLayerType.SATELLITE ->
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/$z/$y/$x"
        MapLayerType.OPENSTREETMAP ->
            "https://tile.openstreetmap.org/$z/$x/$y.png"
        MapLayerType.OPENTOPOMAP ->
            "https://tile.opentopomap.org/$z/$x/$y.png"
        MapLayerType.TACTICAL_DARK ->
            "https://basemaps.cartocdn.com/rastertiles/dark_all/$z/$x/$y.png"
    }
}

// Web Mercator Slippy Map conversions
private fun lon2tileX(lon: Double, zoom: Int): Double {
    return (lon + 180.0) / 360.0 * (1 shl zoom)
}

private fun lat2tileY(lat: Double, zoom: Int): Double {
    val latRad = Math.toRadians(lat.coerceIn(-85.05112878, 85.05112878))
    return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / Math.PI) / 2.0 * (1 shl zoom)
}

private fun tileX2lon(x: Double, zoom: Int): Double {
    return x / (1 shl zoom) * 360.0 - 180.0
}

private fun tileY2lat(y: Double, zoom: Int): Double {
    val n = Math.PI - 2.0 * Math.PI * y / (1 shl zoom)
    return Math.toDegrees(atan(sinh(n)))
}

@Composable
private fun SpotDetailFloatingCard(
    spot: HuntingSpotEntity,
    hunterLat: Double,
    hunterLng: Double,
    hunterElevation: Int,
    onFavoriteToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigate: () -> Unit,
    onExternalNavigate: () -> Unit,
    onCheckWeather: () -> Unit,
    onClose: () -> Unit
) {
    val distanceKm = remember(spot, hunterLat, hunterLng) {
        calculateDistance(hunterLat, hunterLng, spot.latitude, spot.longitude)
    }
    val bearingDeg = remember(spot, hunterLat, hunterLng) {
        calculateBearing(hunterLat, hunterLng, spot.latitude, spot.longitude)
    }
    val elevDiff = spot.elevationMeters - hunterElevation
    val hikingMins = (distanceKm / 3.8 * 60).toInt().coerceAtLeast(1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = ForestDark),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, if (spot.isFavorite) AccentGold else ForestCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(getCategoryColor(spot.category).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(spot.category),
                            contentDescription = null,
                            tint = getCategoryColor(spot.category),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = spot.name,
                            color = TextPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = spot.category,
                                color = getCategoryColor(spot.category),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• GPS: ${String.format(java.util.Locale.US, "%.3f, %.3f", spot.latitude, spot.longitude)}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (spot.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "რჩეული",
                            tint = if (spot.isFavorite) AccentGold else TextSecondary
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "რედაქტირება",
                            tint = AccentGold
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "წაშლა",
                            tint = AlertRed
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "დახურვა",
                            tint = TextSecondary
                        )
                    }
                }
            }

            if (spot.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ForestSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = spot.notes,
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ForestCardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("მანძილი", color = TextSecondary, fontSize = 9.5.sp)
                    Text(
                        text = if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()} მ" else String.format(java.util.Locale.US, "%.1f კმ", distanceKm),
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("აზიმუტი", color = TextSecondary, fontSize = 9.5.sp)
                    Text(
                        text = "$bearingDeg°",
                        color = HuntingGreenLight,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("დრო ფეხით", color = TextSecondary, fontSize = 9.5.sp)
                    Text(
                        text = "~$hikingMins წთ",
                        color = AccentGold,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("სიმაღლე", color = TextSecondary, fontSize = 9.5.sp)
                    Text(
                        text = "${spot.elevationMeters}მ",
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons (In-App Direct Compass Navigation, Weather & External Maps App)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onNavigate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HuntingGreenPrimary,
                        contentColor = ForestBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("გეზი", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCheckWeather,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = InfoBlue
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InfoBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(0.95f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Air, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ამინდი", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExternalNavigate,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentGold
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(0.95f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("რუკაზე", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AddOrEditHuntingSpotDialog(
    spotToEdit: HuntingSpotEntity?,
    defaultLat: Double,
    defaultLng: Double,
    defaultElevation: Int,
    onDismiss: () -> Unit,
    onSave: (name: String, cat: String, lat: Double, lng: Double, elev: Int, notes: String, isFav: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(spotToEdit?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(spotToEdit?.category ?: "სანადირო ადგილი") }
    var latStr by remember { mutableStateOf(String.format(java.util.Locale.US, "%.5f", spotToEdit?.latitude ?: defaultLat)) }
    var lngStr by remember { mutableStateOf(String.format(java.util.Locale.US, "%.5f", spotToEdit?.longitude ?: defaultLng)) }
    var elevationStr by remember { mutableStateOf((spotToEdit?.elevationMeters ?: defaultElevation).toString()) }
    var notes by remember { mutableStateOf(spotToEdit?.notes ?: "") }
    var isFavorite by remember { mutableStateOf(spotToEdit?.isFavorite ?: false) }

    val spotCategories = listOf(
        "სანადირო ადგილი", "წყარო", "პარკინგი", "საფრთხე", "კარავი", "ტყე", "გზა", "სხვა"
    )

    val isEditing = spotToEdit != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.EditLocation else Icons.Default.AddLocationAlt,
                    contentDescription = null,
                    tint = AccentGold
                )
                Text(
                    text = if (isEditing) "წერტილის რედაქტირება" else "ახალი სანადირო წერტილი",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("წერტილის სახელი") },
                        placeholder = { Text("მაგ. მწყრის ველი, ალგეთის წყარო...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = ForestCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("კატეგორია:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(spotCategories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 10.5.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (isSelected) ForestBlack else getCategoryColor(cat)
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

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latStr,
                            onValueChange = { latStr = it },
                            label = { Text("განედი (Lat)") },
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
                            value = lngStr,
                            onValueChange = { lngStr = it },
                            label = { Text("გრძედი (Lng)") },
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
                }

                item {
                    OutlinedTextField(
                        value = elevationStr,
                        onValueChange = { elevationStr = it },
                        label = { Text("სიმაღლე (მეტრი ზ.დ.)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = ForestCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("შენიშვნები, რჩევები და უსაფრთხოება") },
                        placeholder = { Text("მაგ. დილით კარგი გადაფრენაა, მანქანით მისადგომია...") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = ForestCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFavorite = !isFavorite }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isFavorite,
                            onCheckedChange = { isFavorite = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AccentGold,
                                checkmarkColor = ForestBlack
                            )
                        )
                        Text("დამატება რჩეულებში", color = TextPrimary, fontSize = 12.sp)
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ForestSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AccentGold, modifier = Modifier.size(15.dp))
                            Text(
                                text = "ინახება მხოლოდ თქვენს ლოკალურ მოწყობილობაზე (Room DB)",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val lat = latStr.toDoubleOrNull() ?: defaultLat
                        val lng = lngStr.toDoubleOrNull() ?: defaultLng
                        val elev = elevationStr.toIntOrNull() ?: defaultElevation
                        onSave(name, selectedCategory, lat, lng, elev, notes, isFavorite)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ForestBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEditing) "განახლება" else "შენახვა", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("გაუქმება", color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpotsListModalBottomSheet(
    spots: List<HuntingSpotEntity>,
    hunterLat: Double,
    hunterLng: Double,
    onSelectSpot: (HuntingSpotEntity) -> Unit,
    onNavigate: (HuntingSpotEntity) -> Unit,
    onDelete: (HuntingSpotEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var filterCategory by remember { mutableStateOf("ყველა") }
    val categories = listOf("ყველა", "სანადირო ადგილი", "წყარო", "პარკინგი", "საფრთხე", "კარავი", "რჩეულები")

    val displayedSpots = remember(spots, filterCategory) {
        if (filterCategory == "რჩეულები") spots.filter { it.isFavorite }
        else if (filterCategory == "ყველა") spots
        else spots.filter { it.category == filterCategory }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AccentGold) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "შენახული სანადირო წერტილები (${displayedSpots.size})",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ForestSurfaceVariant
                ) {
                    Text(
                        text = "ლოკალური",
                        color = AccentGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    val isSelected = filterCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterCategory = cat },
                        label = { Text(cat, fontSize = 10.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGold,
                            selectedLabelColor = ForestBlack,
                            containerColor = ForestSurface,
                            labelColor = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (displayedSpots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ამ კატეგორიაში წერტილები არ არის", color = TextSecondary, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight(0.6f)
                ) {
                    items(displayedSpots, key = { it.id }) { spot ->
                        val distKm = calculateDistance(hunterLat, hunterLng, spot.latitude, spot.longitude)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSpot(spot) },
                            colors = CardDefaults.cardColors(containerColor = ForestSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (spot.isFavorite) AccentGold else ForestCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(spot.category),
                                        contentDescription = null,
                                        tint = getCategoryColor(spot.category),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = spot.name,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = spot.category,
                                                color = getCategoryColor(spot.category),
                                                fontSize = 10.5.sp
                                            )
                                            Text(
                                                text = "• ${String.format(java.util.Locale.US, "%.1f კმ", distKm)}",
                                                color = TextMuted,
                                                fontSize = 10.5.sp
                                            )
                                            Text(
                                                text = "• ${spot.elevationMeters}მ",
                                                color = TextMuted,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { onNavigate(spot) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Navigation, contentDescription = "გეზი", tint = HuntingGreenLight, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { onDelete(spot) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "წაშლა", tint = AlertRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "სანადირო ადგილი" -> Icons.Default.TrackChanges
        "წყარო" -> Icons.Default.WaterDrop
        "პარკინგი" -> Icons.Default.LocalParking
        "საფრთხე" -> Icons.Default.Warning
        "კარავი" -> Icons.Default.Cabin
        "ტყე" -> Icons.Default.Park
        "გზა" -> Icons.AutoMirrored.Filled.AltRoute
        else -> Icons.Default.Place
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "სანადირო ადგილი" -> AccentGold
        "წყარო" -> InfoBlue
        "პარკინგი" -> TextSecondary
        "საფრთხე" -> AlertRed
        "კარავი" -> WarningOrange
        "ტყე" -> HuntingGreenLight
        "გზა" -> EarthSand
        else -> TextMuted
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Radius of earth in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
    val dLon = Math.toRadians(lon2 - lon1)
    val y = sin(dLon) * cos(Math.toRadians(lat2))
    val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
            sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
    var brng = Math.toDegrees(atan2(y, x))
    if (brng < 0) brng += 360.0
    return brng.toInt()
}

fun openExternalMapNavigation(context: Context, latitude: Double, longitude: Double, label: String) {
    try {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(webIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, "რუკის აპლიკაციის გახსნა ვერ მოხერხდა", Toast.LENGTH_SHORT).show()
        }
    }
}
