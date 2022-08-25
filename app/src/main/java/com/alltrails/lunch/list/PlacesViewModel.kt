package com.alltrails.lunch.list

import android.Manifest
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.alltrails.lunch.app.LocationRepository
import com.alltrails.lunch.core.LatLng
import com.alltrails.lunch.core.Lce
import com.alltrails.lunch.core.NearbyPlaces
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val placesRepo: PlacesRepository,
    locationRepository: LocationRepository,
) : ViewModel() {

    private val hasLocationPermission: BehaviorSubject<Boolean> = BehaviorSubject.create()
    private val disposables = CompositeDisposable()

    // Subscribe to location() and pass emissions downstream only after hasLocationPermission emits true
    private val location: Observable<LatLng> = hasLocationPermission.takeUntil { it }
            .flatMap { Observable.empty<Location>() }
            //noinspection MissingPermission
            .concatWith(locationRepository.location())
            .map(LatLng::fromLocation)

    // TODO: define new Place model instead of reusing the one from the response
    private val nearbySearchCache: Observable<Lce<NearbyPlaces>> by lazy {
        location.distinctUntilChanged()
            // TODO: allow for some gps slop in distinctness check
            .flatMap { latLng ->
                placesRepo.nearbySearch(latLng)
                    .startWithItem(Lce.loading())
                    .map { lce ->
                        when (lce) {
                            is Lce.Initial -> Lce.initial()
                            is Lce.Loading -> Lce.loading()
                            is Lce.Content -> Lce.Content(NearbyPlaces(lce.content, latLng))
                            is Lce.Error -> Lce.Error(lce.throwable)
                        }
                    }
            }
            .scan { prev, current ->
                if (current is Lce.Loading && prev is Lce.Content) {
                    Lce.Loading(oldContent = prev.content)
                } else {
                    current
                }
            }
            .replay(1)
            .apply { disposables.add(connect()) }
    }

    /**
     * Lce.Initial: State before receiving first location update.
     * Lce.Loading: State after receiving first location update but before nearbySearch response.
     * Lce.Content: State after nearbySearch response.
     */
    fun nearbySearch(): Observable<Lce<NearbyPlaces>> = nearbySearchCache

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun onLocationPermissionGranted() {
        hasLocationPermission.onNext(true)
    }

    fun onLocationPermissionDenied() {
        hasLocationPermission.onNext(false)
    }

    /**
     * Emits Unit immediately upon subscription iff location permission was previously denied.
     * Then emits Unit whenever location permission is denied subsequently.
     */
    fun locationPermissionDenied(): Observable<Unit> {
        return hasLocationPermission.filter(Boolean::not)
            .map { }
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }
}