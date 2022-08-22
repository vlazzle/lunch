package com.alltrails.lunch.app

import android.Manifest
import android.location.Location
import androidx.annotation.RequiresPermission
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val locationEngineLazy: Lazy<LocationEngine>,
) {

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun locations(): Observable<Location> {
        return Observable.create { emitter ->
            val request = LocationEngineRequest.Builder(500L)
                .setMaxWaitTime(2000L)
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
            val locationEngine = locationEngineLazy.get()
            locationEngine.requestLocationUpdates(request, locationCallback, null)
            emitter.setCancellable {
                locationEngine.removeLocationUpdates(locationCallback)
            }
        }
    }
}