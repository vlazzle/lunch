package com.alltrails.lunch.app

import android.Manifest
import android.location.Location
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.*
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationProviderClient: FusedLocationProviderClient,
) {

    private var handlerThread: HandlerThread? = null

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun location(): Observable<Location> {
        return Observable.create { emitter ->
            locationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let { emitter.onNext(it) }
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }


            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let {
                        emitter.onNext(it)
                    }
                }
            }

            val request = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }
            locationProviderClient.requestLocationUpdates(request, locationCallback, getLooper())

            emitter.setCancellable {
                locationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    private fun getLooper(): Looper {
        if (handlerThread?.looper == null) {
            handlerThread = HandlerThread("LocationEngineCallback Looper").apply { start() }
        }
        return handlerThread!!.looper!!
    }
}