package com.alltrails.lunch.list

import android.Manifest
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.alltrails.lunch.backend.NearbySearchResponse
import com.alltrails.lunch.core.LatLng
import com.alltrails.lunch.core.Lce
import com.mapbox.android.core.location.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    //noinspection StaticFieldLeak
    @ApplicationContext private val context: Context,
    private val placesRepo: PlacesRepository
) : ViewModel() {

    private val locations: BehaviorSubject<Location> = BehaviorSubject.create()

    private var locationEngine: LocationEngine? = null

    // TODO: define new Place model instead of reusing the one from the response
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun nearbySearch(): Observable<Lce<List<NearbySearchResponse.Place>>> {
        return locations.map { LatLng.fromLocation(it) }
            .distinctUntilChanged()
            .flatMap { placesRepo.nearbySearch(it) }
            .startWithItem(Lce.loading())
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun onLocationPermissionGranted() {
        // TODO: inject LocationEngine using dagger
        locationEngine = LocationEngineProvider.getBestLocationEngine(context)
            .apply {
                val request = LocationEngineRequest.Builder(1000L)
                    .setMaxWaitTime(3000L)
                    .build()
                requestLocationUpdates(request, locationCallback, null)
            }
    }

    override fun onCleared() {
        locationEngine?.removeLocationUpdates(locationCallback)
        super.onCleared()
    }

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
}