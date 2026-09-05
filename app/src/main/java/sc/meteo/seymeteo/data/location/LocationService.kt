package sc.meteo.seymeteo.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
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
                    cont.resume(gps)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }

            cont.invokeOnCancellation { cts.cancel() }
        }
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
