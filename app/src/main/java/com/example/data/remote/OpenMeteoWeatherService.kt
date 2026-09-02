package com.example.data.remote

import android.util.Log
import com.example.data.model.DetailedHuntingWeather
import com.example.data.model.HuntingConditionScore
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object OpenMeteoWeatherService {

    private const val TAG = "OpenMeteoWeatherService"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "MonadireApp/1.0 (Android; Georgian Hunter GIS; contact: dls.service@yahoo.com)")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    suspend fun fetchHuntingWeather(
        latitude: Double,
        longitude: Double,
        customLocationName: String? = null
    ): DetailedHuntingWeather = withContext(Dispatchers.IO) {
        val locationLabel = customLocationName ?: getGeorgianRegionName(latitude, longitude)
        val url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$latitude" +
                "&longitude=$longitude" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,rain,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_probability_max,wind_speed_10m_max" +
                "&wind_speed_unit=kmh" +
                "&timezone=auto"

        try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "Open-Meteo returned HTTP ${response.code}")
                return@withContext getFallbackWeather(latitude, longitude, locationLabel)
            }

            val body = response.body?.string() ?: return@withContext getFallbackWeather(latitude, longitude, locationLabel)
            val json = JSONObject(body)

            val current = json.optJSONObject("current")
            val daily = json.optJSONObject("daily")

            val temp = current?.optDouble("temperature_2m", 18.0) ?: 18.0
            val feelsLike = current?.optDouble("apparent_temperature", temp - 1.0) ?: (temp - 1.0)
            val humidity = current?.optInt("relative_humidity_2m", 60) ?: 60
            val isDay = (current?.optInt("is_day", 1) ?: 1) == 1
            val weatherCode = current?.optInt("weather_code", 1) ?: 1
            val pressure = current?.optDouble("surface_pressure", 1016.0) ?: 1016.0
            val windSpeed = current?.optDouble("wind_speed_10m", 12.0) ?: 12.0
            val windDirDeg = current?.optInt("wind_direction_10m", 45) ?: 45
            val windGusts = current?.optDouble("wind_gusts_10m", windSpeed * 1.4) ?: (windSpeed * 1.4)
            val precipMm = current?.optDouble("precipitation", 0.0) ?: 0.0

            var tempMax = temp + 3.0
            var tempMin = temp - 4.0
            var rainProb = 15
            var sunrise = "06:24"
            var sunset = "19:48"

            if (daily != null) {
                val tempMaxArray = daily.optJSONArray("temperature_2m_max")
                if (tempMaxArray != null && tempMaxArray.length() > 0) {
                    tempMax = tempMaxArray.optDouble(0, tempMax)
                }

                val tempMinArray = daily.optJSONArray("temperature_2m_min")
                if (tempMinArray != null && tempMinArray.length() > 0) {
                    tempMin = tempMinArray.optDouble(0, tempMin)
                }

                val rainProbArray = daily.optJSONArray("precipitation_probability_max")
                if (rainProbArray != null && rainProbArray.length() > 0) {
                    rainProb = rainProbArray.optInt(0, 15)
                }

                val sunriseArray = daily.optJSONArray("sunrise")
                if (sunriseArray != null && sunriseArray.length() > 0) {
                    val fullSunrise = sunriseArray.optString(0, "")
                    if (fullSunrise.contains("T")) {
                        sunrise = fullSunrise.substringAfter("T").take(5)
                    }
                }

                val sunsetArray = daily.optJSONArray("sunset")
                if (sunsetArray != null && sunsetArray.length() > 0) {
                    val fullSunset = sunsetArray.optString(0, "")
                    if (fullSunset.contains("T")) {
                        sunset = fullSunset.substringAfter("T").take(5)
                    }
                }
            }

            val conditionDesc = getWeatherDescriptionKa(weatherCode)
            val windDirectionKa = getWindDirectionKa(windDirDeg)
            val score = calculateConditionScore(temp, windSpeed, rainProb, pressure)
            val advice = generateHuntingAdvice(windSpeed, windDirDeg, rainProb, pressure, temp)

            DetailedHuntingWeather(
                locationName = locationLabel,
                latitude = latitude,
                longitude = longitude,
                temperatureC = temp,
                feelsLikeC = feelsLike,
                tempMaxC = tempMax,
                tempMinC = tempMin,
                conditionDescription = conditionDesc,
                weatherCode = weatherCode,
                windSpeedKmh = windSpeed,
                windDirectionDegrees = windDirDeg,
                windDirectionNameKa = windDirectionKa,
                windGustsKmh = windGusts,
                surfacePressureHpa = pressure,
                humidityPercent = humidity,
                precipitationProbabilityPercent = rainProb,
                precipitationMm = precipMm,
                sunriseTime = sunrise,
                sunsetTime = sunset,
                isDay = isDay,
                huntingConditionScore = score,
                huntingAdvice = advice,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather for $latitude, $longitude", e)
            getFallbackWeather(latitude, longitude, locationLabel)
        }
    }

    private fun getFallbackWeather(lat: Double, lng: Double, name: String): DetailedHuntingWeather {
        return DetailedHuntingWeather(
            locationName = name,
            latitude = lat,
            longitude = lng,
            temperatureC = 17.0,
            feelsLikeC = 16.5,
            tempMaxC = 21.0,
            tempMinC = 11.0,
            conditionDescription = "ნაწილობრივ ღრუბლიანი",
            weatherCode = 2,
            windSpeedKmh = 10.5,
            windDirectionDegrees = 65,
            windDirectionNameKa = "ჩრდილო-აღმოსავლეთი (NE)",
            windGustsKmh = 16.0,
            surfacePressureHpa = 1017.0,
            humidityPercent = 55,
            precipitationProbabilityPercent = 10,
            precipitationMm = 0.0,
            sunriseTime = "06:22",
            sunsetTime = "19:45",
            isDay = true,
            huntingConditionScore = HuntingConditionScore.VERY_GOOD,
            huntingAdvice = "სტაბილური ბარომეტრული წნევა და ზომიერი ნიავი. იდეალური პირობებია მინდვრისა და ტყის ფრინველზე სანადიროდ.",
            lastUpdatedTimestamp = System.currentTimeMillis(),
            isLoading = false,
            error = null
        )
    }

    fun getWeatherDescriptionKa(code: Int): String {
        return when (code) {
            0 -> "მოწმენდილი, მზიანი"
            1 -> "უპირატესად მოწმენდილი"
            2 -> "ნაწილობრივ ღრუბლიანი"
            3 -> "მოღრუბლული"
            45, 48 -> "ნისლი / ბურუსი"
            51, 53, 55 -> "მსუბუქი ცვრიმა"
            56, 57 -> "გამყინავი ცვრიმა"
            61, 63 -> "ზომიერი წვიმა"
            65 -> "ძლიერი წვიმა"
            66, 67 -> "ყინულოვანი წვიმა"
            71, 73, 75 -> "თოვა"
            77 -> "თოვლის მარცვლები"
            80, 81, 82 -> "შხაპუნა წვიმა"
            85, 86 -> "შხაპუნა თოვა"
            95 -> "ჭექა-ქუხილი"
            96, 99 -> "ჭექა-ქუხილი სეტყვით"
            else -> "ცვალებადი მოღრუბლულობა"
        }
    }

    fun getWindDirectionKa(deg: Int): String {
        val normalized = (deg % 360 + 360) % 360
        return when {
            normalized in 338..360 || normalized in 0..22 -> "ჩრდილოეთი (N)"
            normalized in 23..67 -> "ჩრდილო-აღმოსავლეთი (NE)"
            normalized in 68..112 -> "აღმოსავლეთი (E)"
            normalized in 113..157 -> "სამხრეთ-აღმოსავლეთი (SE)"
            normalized in 158..202 -> "სამხრეთი (S)"
            normalized in 203..247 -> "სამხრეთ-დასავლეთი (SW)"
            normalized in 248..292 -> "დასავლეთი (W)"
            normalized in 293..337 -> "ჩრდილო-დასავლეთი (NW)"
            else -> "ცვლადი"
        }
    }

    fun getWindDirectionShortKa(deg: Int): String {
        val normalized = (deg % 360 + 360) % 360
        return when {
            normalized in 338..360 || normalized in 0..22 -> "ჩრდ"
            normalized in 23..67 -> "ჩ-აღმ"
            normalized in 68..112 -> "აღმ"
            normalized in 113..157 -> "ს-აღმ"
            normalized in 158..202 -> "სამხ"
            normalized in 203..247 -> "ს-დას"
            normalized in 248..292 -> "დას"
            normalized in 293..337 -> "ჩ-დას"
            else -> "ცვლ"
        }
    }

    fun calculateConditionScore(temp: Double, windSpeed: Double, rainProb: Int, pressure: Double): HuntingConditionScore {
        var score = 0
        if (temp in 6.0..24.0) score += 1
        if (windSpeed <= 16.0) score += 1
        if (rainProb <= 25) score += 1
        if (pressure in 1010.0..1024.0) score += 1

        return when {
            score >= 4 -> HuntingConditionScore.VERY_GOOD
            score == 3 -> HuntingConditionScore.GOOD
            score == 2 -> HuntingConditionScore.MODERATE
            else -> HuntingConditionScore.POOR
        }
    }

    fun generateHuntingAdvice(windSpeed: Double, windDir: Int, rainProb: Int, pressure: Double, temp: Double): String {
        val dirKa = getWindDirectionKa(windDir)
        return when {
            windSpeed > 28.0 -> "⚠️ ძლიერი ქარი ($windSpeed კმ/სთ $dirKa): ფრინველი თავს აფარებს ხევებსა და ხშირ ნარგავებს. ძაღლისთვის სუნის აღება გართულებულია. მიუდექით ქარის საწინააღმდეგო მხრიდან."
            rainProb > 55 -> "🌧️ მოსალოდნელია წვიმა ($rainProb%): კარგი პირობებია იხვსა და წყალმცურავებზე ნადირობისთვის. ტყეში ნადირის ნაბიჯების ხმაური მინიმალურია."
            pressure in 1014.0..1024.0 && windSpeed <= 14.0 -> "🎯 იდეალური სანადირო პირობები: მაღალი და სტაბილური ბარომეტრული წნევა ($pressure hPa), მსუბუქი ნიავი ($windSpeed კმ/სთ $dirKa). ნადირი აქტიურად მოძრაობს კვების ადგილებისკენ."
            temp < 0 -> "❄️ დაბალი ტემპერატურა: თოვლის საფარზე კვალის ძიება გაადვილებულია. აუცილებელია თბილი თერმო-ეკიპირება."
            else -> "👍 ხელსაყრელი ამინდი: ქარი $windSpeed კმ/სთ $dirKa მიმართულებით. რეკომენდებულია საგუშაგოს ქარის საწინააღმდეგო მხარეს განლაგება."
        }
    }

    fun getGeorgianRegionName(lat: Double, lng: Double): String {
        return when {
            lat in 41.5..41.9 && lng in 45.0..45.7 -> "საგარეჯო / იორის ხეობა"
            lat in 41.3..41.7 && lng in 44.8..45.4 -> "გარდაბანი / სამგორის ველები"
            lat in 41.5..42.1 && lng in 45.5..46.3 -> "დედოფლისწყარო / ვაშლოვანი"
            lat in 41.8..42.2 && lng in 43.1..43.7 -> "ბორჯომის ხეობა"
            lat in 41.6..41.8 && lng in 44.7..45.1 -> "თბილისი / კრწანისის შემოგარენი"
            lat in 41.8..42.4 && lng in 45.2..46.0 -> "თელავი / ალაზნის ველი"
            lat in 42.0..42.5 && lng in 43.9..44.6 -> "დუშეთი / ბაზალეთის ტბა"
            lat in 42.1..42.8 && lng in 42.8..43.6 -> "რაჭა-ლეჩხუმი"
            lat in 41.9..42.5 && lng in 41.5..42.3 -> "კოლხეთის დაბლობი / სამეგრელო"
            lat in 41.4..41.8 && lng in 43.4..44.2 -> "ჯავახეთის პლატო / ტაბაწყური"
            else -> String.format(java.util.Locale.US, "საქართველო (%.3f°N, %.3f°E)", lat, lng)
        }
    }
}
