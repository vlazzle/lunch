package com.alltrails.lunch.list

import android.Manifest
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.alltrails.lunch.app.LocationRepository
import com.alltrails.lunch.backend.NearbySearchResponse
import com.alltrails.lunch.core.LatLng
import com.alltrails.lunch.core.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val placesRepo: PlacesRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val hasLocationPermission: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val locations: Observable<Location> = hasLocationPermission.flatMap {
        if (it) {
            //noinspection MissingPermission
            locationRepository.locations()
        } else {
            Observable.empty()
        }
    }

    // TODO: define new Place model instead of reusing the one from the response
    fun nearbySearch(): Observable<Lce<List<NearbySearchResponse.Place>>> {
        return locations.map { LatLng.fromLocation(it) }
            .distinctUntilChanged()
            .flatMap { placesRepo.nearbySearch(it) }
            .startWithItem(Lce.loading())
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun onLocationPermissionGranted() {
        hasLocationPermission.onNext(true)
    }
}