package sc.meteo.seymeteo.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import sc.meteo.seymeteo.data.model.UserKinematics
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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
                            speedKmh = speedKmh,
                            bearingDeg = bearingDeg,
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
     * Updates kinematics manually (e.g. for testing or simulated motion in UI).
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
