package sc.meteo.seymeteo.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import sc.meteo.seymeteo.data.model.UserKinematics
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.*

data class GpsLocation(val latitude: Double, val longitude: Double)

class LocationService(context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val _currentLocation = MutableStateFlow<GpsLocation?>(null)
    val currentLocation: Flow<GpsLocation?> = _currentLocation.asStateFlow()

    private val _userKinematics = MutableStateFlow(
        UserKinematics(
            location = GpsLocation(-4.6191, 55.4513), // Default Victoria Port, Mahé
            speedKmh = 0.0,
            bearingDeg = 0.0,
            isMoving = false
        )
    )
    val userKinematics: Flow<UserKinematics> = _userKinematics.asStateFlow()
    val currentUserKinematics: UserKinematics get() = _userKinematics.value

    private var isTracking = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val gps = GpsLocation(location.latitude, location.longitude)
            _currentLocation.value = gps

            val speedKmh = (location.speed * 3.6).toDouble()
            val bearingDeg = location.bearing.toDouble()
            val isMoving = speedKmh >= 2.0 // > 2 km/h indicates intentional human/vehicle motion

            _userKinematics.value = UserKinematics(
                location = gps,
                speedKmh = max(0.0, speedKmh),
                bearingDeg = (bearingDeg + 360.0) % 360.0,
                isMoving = isMoving,
                accuracyMeters = location.accuracy,
                timestampMs = System.currentTimeMillis()
            )
        }
    }

    /**
     * Starts high-resolution GPS tracking when the app is active in foreground.
     * Throttled to 5 seconds / 5 meters to preserve device battery life.
     */
    @SuppressLint("MissingPermission")
    fun startRealtimeTracking() {
        if (isTracking) return
        try {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(2500L)
                .setMinUpdateDistanceMeters(3.0f)
                .setWaitForAccurateLocation(false)
                .build()

            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            isTracking = true
        } catch (_: SecurityException) {
            // Permission not yet granted
        }
    }

    /**
     * Stops GPS updates immediately when the app goes into background or pauses to prevent battery drain.
     */
    fun stopRealtimeTracking() {
        if (!isTracking) return
        try {
            fusedClient.removeLocationUpdates(locationCallback)
            isTracking = false
        } catch (_: Exception) {}
    }

    /**
     * One-shot current position fetch using PRIORITY_BALANCED_POWER_ACCURACY.
     * Caller must have ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION granted.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GpsLocation? {
        val cts = CancellationTokenSource()
        return suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { location: Location? ->
                    val gps = location?.let { GpsLocation(it.latitude, it.longitude) }
                    _currentLocation.value = gps
                    if (location != null && gps != null) {
                        val speedKmh = (location.speed * 3.6).toDouble()
                        val bearingDeg = location.bearing.toDouble()
                        val isMoving = speedKmh >= 2.0
                        _userKinematics.value = UserKinematics(
                            location = gps,
                            speedKmh = max(0.0, speedKmh),
                            bearingDeg = (bearingDeg + 360.0) % 360.0,
                            isMoving = isMoving,
                            accuracyMeters = location.accuracy
                        )
                    }
                    cont.resume(gps)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }

            cont.invokeOnCancellation { cts.cancel() }
        }
    }

    /**
     * Updates kinematics manually (e.g. for testing simulated motion in UI).
     */
    fun updateSimulatedKinematics(
        location: GpsLocation,
        speedKmh: Double,
        bearingDeg: Double,
        isMoving: Boolean
    ) {
        _userKinematics.value = UserKinematics(
            location = location,
            speedKmh = speedKmh,
            bearingDeg = bearingDeg,
            isMoving = isMoving
        )
    }
}

/** Haversine distance in kilometres between two lat/lon points. */
fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}
