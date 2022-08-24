package com.alltrails.lunch.app

import android.Manifest
import android.location.Location
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationEngine: LocationEngine,
) {

    private var handlerThread: HandlerThread? = null

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun location(): Observable<Location> {
        return Observable.create { emitter ->
            val request = LocationEngineRequest.Builder(500L)
                .setMaxWaitTime(1000L)
                .build()
            val locationCallback = object : LocationEngineCallback<LocationEngineResult?> {
                override fun onSuccess(result: LocationEngineResult?) {
                    result?.lastLocation?.let {
                        emitter.onNext(it)
                    }
                }

                override fun onFailure(exception: Exception) {
                    emitter.onError(exception)
                }
            }

            locationEngine.getLastLocation(locationCallback)
            locationEngine.requestLocationUpdates(request, locationCallback, getLooper())

            emitter.setCancellable {
                locationEngine.removeLocationUpdates(locationCallback)
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