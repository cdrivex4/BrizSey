package sc.meteo.seymeteo.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import sc.meteo.seymeteo.data.model.CapAlertGeoJson
import sc.meteo.seymeteo.data.model.HomeForecastResponse
import sc.meteo.seymeteo.data.model.IslandLocation
import java.util.concurrent.TimeUnit

interface SmaApiService {

    @GET("api/cities")
    suspend fun getCities(): List<IslandLocation>

    @GET("weather/home-weather-forecast/")
    suspend fun getHomeWeatherForecast(): HomeForecastResponse

    @GET("api/cap/alerts.geojson")
    suspend fun getCapAlerts(): CapAlertGeoJson

    companion object {
        private const val BASE_URL = "https://www.meteo.sc/"

        fun create(): SmaApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SmaApiService::class.java)
        }
    }
}
