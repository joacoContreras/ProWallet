package com.undef.prowallet.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class LocationHelper(private val context: Context) {

    suspend fun getLocation(): Pair<Double, Double>? {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) return null

        val client = LocationServices.getFusedLocationProviderClient(context)

        return suspendCancellableCoroutine { continuation ->
            val task = client.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }
}
