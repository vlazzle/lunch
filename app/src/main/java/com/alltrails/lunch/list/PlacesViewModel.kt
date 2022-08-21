package com.alltrails.lunch.list

import androidx.lifecycle.ViewModel
import com.alltrails.lunch.backend.NearbySearchResponse
import com.alltrails.lunch.core.LatLng
import com.alltrails.lunch.core.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val placesRepo: PlacesRepository
) : ViewModel() {

    // TODO: use actual location
    private val currentLocation = Observable.just(LatLng(32.807974, -117.261911))
        .delay(1000L, TimeUnit.MILLISECONDS)
        .replay()
        .autoConnect()

    // TODO: define new Place model instead of reusing the one from the response
    fun nearbySearch(): Observable<Lce<List<NearbySearchResponse.Place>>> {
        return currentLocation.flatMap { placesRepo.nearbySearch(it) }
            .startWithItem(Lce.loading())
    }

    fun onLocationPermissionGranted() {
    }
}