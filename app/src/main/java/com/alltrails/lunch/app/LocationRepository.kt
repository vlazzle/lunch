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
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val locationEngineLazy: Lazy<LocationEngine>,
) {

    private val locations: BehaviorSubject<Location> = BehaviorSubject.create()

    private val locationCallback = object : LocationEngineCallback<LocationEngineResult?> {
        override fun onSuccess(result: LocationEngineResult?) {
            result?.lastLocation?.let {
                locations.onNext(it)
            }
        }

        override fun onFailure(exception: Exception) {
            locations.onError(exception)
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun locations(): Observable<Location> {
        // TODO: Observable.create()
        val request = LocationEngineRequest.Builder(1000L)
            .setMaxWaitTime(3000L)
            .build()
        locationEngineLazy.get().requestLocationUpdates(request, locationCallback, null)
        return locations
    }

    // TODO: locationEngine?.removeLocationUpdates(locationCallback)
}