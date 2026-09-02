package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DetailedHuntingWeather
import com.example.data.model.HuntingConditionScore
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Compact floating weather & wind HUD widget designed for the Map Screen.
 * Displays temperature, wind direction arrow, wind speed, and hunting score.
 * Clicking it opens the comprehensive weather bottom sheet.
 */
@Composable
fun MapWeatherFloatingBadge(
    weather: DetailedHuntingWeather,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(1.dp, ForestCardBorder, RoundedCornerShape(14.dp))
            .testTag("map_weather_floating_badge"),
        color = ForestDark.copy(alpha = 0.95f),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Animated/Rotating Wind Direction Compass Arrow
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(ForestSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "ქარის მიმართულება",
                    tint = AccentGold,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(weather.windDirectionDegrees.toFloat())
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${weather.temperatureC.toInt()}°C",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${weather.windSpeedKmh.toInt()} კმ/სთ",
                        color = HuntingGreenLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = weather.windDirectionNameKa.take(16),
                    color = TextSecondary,
                    fontSize = 9.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = AccentGold,
                    strokeWidth = 2.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (weather.huntingConditionScore) {
                                HuntingConditionScore.VERY_GOOD -> HuntingGreenLight
                                HuntingConditionScore.GOOD -> AccentGold
                                HuntingConditionScore.MODERATE -> WarningOrange
                                HuntingConditionScore.POOR -> AlertRed
                            }
                        )
                )
            }
        }
    }
}

/**
 * Comprehensive Hunting Weather & Wind Forecast Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuntingWeatherBottomSheet(
    weather: DetailedHuntingWeather,
    isLoading: Boolean,
    currentHunterLat: Double,
    currentHunterLng: Double,
    tappedLat: Double?,
    tappedLng: Double?,
    onRefreshGpsWeather: () -> Unit,
    onRefreshTappedWeather: (Double, Double) -> Unit,
    onSelectRegion: (Double, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ForestDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AccentGold) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title & Location Switcher Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "სანადირო ამინდი & ქარი",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    Text(
                        text = "Open-Meteo მეტეოროლოგიური პროგნოზი",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = {
                        if (tappedLat != null && tappedLng != null) {
                            onRefreshTappedWeather(tappedLat, tappedLng)
                        } else {
                            onRefreshGpsWeather()
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AccentGold,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "განახლება",
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Quick Location Switcher Buttons (GPS vs Tapped Point vs Popular Regions)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isGpsSelected = (tappedLat == null)
                FilterChip(
                    selected = isGpsSelected,
                    onClick = { onRefreshGpsWeather() },
                    label = { Text("ჩემი GPS", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (isGpsSelected) ForestBlack else HuntingGreenLight
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HuntingGreenPrimary,
                        selectedLabelColor = ForestBlack,
                        containerColor = ForestSurfaceVariant,
                        labelColor = TextPrimary
                    )
                )

                if (tappedLat != null && tappedLng != null) {
                    FilterChip(
                        selected = !isGpsSelected,
                        onClick = { onRefreshTappedWeather(tappedLat, tappedLng) },
                        label = { Text("მონიშნული წერტილი", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (!isGpsSelected) ForestBlack else AccentGold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGold,
                            selectedLabelColor = ForestBlack,
                            containerColor = ForestSurfaceVariant,
                            labelColor = TextPrimary
                        )
                    )
                }
            }

            // Location Name & Coordinates Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ForestSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = weather.locationName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%.4f°N, %.4f°E", weather.latitude, weather.longitude),
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (weather.huntingConditionScore) {
                            HuntingConditionScore.VERY_GOOD -> HuntingGreenDark
                            HuntingConditionScore.GOOD -> ForestDark
                            HuntingConditionScore.MODERATE -> WarningOrange.copy(alpha = 0.2f)
                            HuntingConditionScore.POOR -> AlertRed.copy(alpha = 0.2f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (weather.huntingConditionScore) {
                                HuntingConditionScore.VERY_GOOD -> HuntingGreenLight
                                HuntingConditionScore.GOOD -> AccentGold
                                HuntingConditionScore.MODERATE -> WarningOrange
                                HuntingConditionScore.POOR -> AlertRed
                            }
                        )
                    ) {
                        Text(
                            text = "პირობები: ${weather.huntingConditionScore.labelKa}",
                            color = when (weather.huntingConditionScore) {
                                HuntingConditionScore.VERY_GOOD -> HuntingGreenLight
                                HuntingConditionScore.GOOD -> AccentGold
                                HuntingConditionScore.MODERATE -> WarningOrange
                                HuntingConditionScore.POOR -> AlertRed
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Main Metrics: Temperature & Wind Compass Rose Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Card: Temperature & General Condition
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = ForestSurfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("ტემპერატურა", color = TextSecondary, fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${weather.temperatureC.toInt()}°C",
                                color = TextPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "იგრძნობა: ${weather.feelsLikeC.toInt()}°C",
                            color = AccentGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "მაქს: ${weather.tempMaxC.toInt()}° / მინ: ${weather.tempMinC.toInt()}°",
                            color = TextSecondary,
                            fontSize = 10.5.sp
                        )
                        HorizontalDivider(color = ForestCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 2.dp))
                        Text(
                            text = weather.conditionDescription,
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2
                        )
                    }
                }

                // Right Card: Interactive Wind Compass Rose
                Card(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = ForestSurfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("ქარის კომპასი & დაბერვა", color = TextSecondary, fontSize = 11.sp)

                        // Visual Wind Compass Rose
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            WindCompassRoseCanvas(
                                windDegrees = weather.windDirectionDegrees,
                                windSpeedKmh = weather.windSpeedKmh
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${weather.windSpeedKmh.toInt()} კმ/სთ",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${weather.windDirectionNameKa} (${weather.windDirectionDegrees}°)",
                                color = AccentGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "დაბერვა: ${weather.windGustsKmh.toInt()} კმ/სთ",
                                color = WarningOrange,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Secondary Details Grid (Pressure, Humidity, Rain Probability, Sunrise & Sunset)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ForestSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeatherDetailBadge(
                            icon = Icons.Default.Compress,
                            label = "ჰაერის წნევა",
                            value = "${weather.surfacePressureHpa.toInt()} hPa",
                            subtitle = if (weather.surfacePressureHpa >= 1015) "სტაბილური (კარგი)" else "დაბალი წნევა"
                        )
                        WeatherDetailBadge(
                            icon = Icons.Default.WaterDrop,
                            label = "ნალექის ალბათობა",
                            value = "${weather.precipitationProbabilityPercent}%",
                            subtitle = if (weather.precipitationMm > 0) "${weather.precipitationMm} მმ" else "მშრალი"
                        )
                        WeatherDetailBadge(
                            icon = Icons.Default.Opacity,
                            label = "ტენიანობა",
                            value = "${weather.humidityPercent}%",
                            subtitle = "ჰაერის ტენი"
                        )
                    }

                    HorizontalDivider(color = ForestCardBorder.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeatherDetailBadge(
                            icon = Icons.Default.WbSunny,
                            label = "მზის ამოსვლა",
                            value = weather.sunriseTime,
                            subtitle = "დილის გარიჟრაჟი"
                        )
                        WeatherDetailBadge(
                            icon = Icons.Default.WbTwilight,
                            label = "მზის ჩასვლა",
                            value = weather.sunsetTime,
                            subtitle = "საღამოს ბინდი"
                        )
                        WeatherDetailBadge(
                            icon = Icons.Default.Air,
                            label = "ქარის დაბერვა",
                            value = "${weather.windGustsKmh.toInt()} კმ/სთ",
                            subtitle = "მაქსიმუმი"
                        )
                    }
                }
            }

            // Tactical Hunting Advice Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HuntingGreenDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, HuntingGreenLight.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "მონადირის ტაქტიკური რჩევა",
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = weather.huntingAdvice,
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Custom Canvas Compass Rose with Cardinal Directions & Arrow.
 */
@Composable
private fun WindCompassRoseCanvas(
    windDegrees: Int,
    windSpeedKmh: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 4.dp.toPx()

        // Outer dial circle
        drawCircle(
            color = ForestCardBorder,
            radius = radius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Inner soft glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(HuntingGreenPrimary.copy(alpha = 0.15f), Color.Transparent),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // Cardinal Ticks (N, E, S, W)
        val cardinalAngles = listOf(0f, 90f, 180f, 270f)
        cardinalAngles.forEach { angle ->
            val rad = Math.toRadians((angle - 90.0)).toFloat()
            val start = Offset(
                center.x + (radius - 6.dp.toPx()) * cos(rad),
                center.y + (radius - 6.dp.toPx()) * sin(rad)
            )
            val end = Offset(
                center.x + radius * cos(rad),
                center.y + radius * sin(rad)
            )
            val color = if (angle == 0f) AlertRed else TextSecondary
            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = 2.dp.toPx()
            )
        }

        // Rotating Wind Vector Arrow
        val windRad = Math.toRadians((windDegrees - 90.0)).toFloat()
        val arrowLength = radius - 10.dp.toPx()
        val arrowTip = Offset(
            center.x + arrowLength * cos(windRad),
            center.y + arrowLength * sin(windRad)
        )
        val arrowTail = Offset(
            center.x - (arrowLength * 0.5f) * cos(windRad),
            center.y - (arrowLength * 0.5f) * sin(windRad)
        )

        // Draw Arrow line
        drawLine(
            color = AccentGold,
            start = arrowTail,
            end = arrowTip,
            strokeWidth = 3.dp.toPx()
        )

        // Arrow Head Pointer
        val headRad1 = Math.toRadians((windDegrees - 90.0 + 150.0)).toFloat()
        val headRad2 = Math.toRadians((windDegrees - 90.0 - 150.0)).toFloat()
        val headSize = 8.dp.toPx()

        val wing1 = Offset(
            arrowTip.x + headSize * cos(headRad1),
            arrowTip.y + headSize * sin(headRad1)
        )
        val wing2 = Offset(
            arrowTip.x + headSize * cos(headRad2),
            arrowTip.y + headSize * sin(headRad2)
        )

        val path = Path().apply {
            moveTo(arrowTip.x, arrowTip.y)
            lineTo(wing1.x, wing1.y)
            lineTo(wing2.x, wing2.y)
            close()
        }
        drawPath(path, color = AccentGold)

        // Center Pivot Pin
        drawCircle(color = AccentGold, radius = 3.5.dp.toPx(), center = center)
    }
}

@Composable
private fun WeatherDetailBadge(
    icon: ImageVector,
    label: String,
    value: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HuntingGreenLight,
            modifier = Modifier.size(16.dp)
        )
        Text(text = label, color = TextSecondary, fontSize = 9.5.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = subtitle, color = TextMuted, fontSize = 8.5.sp)
    }
}
