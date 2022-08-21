package com.alltrails.lunch.list

import androidx.lifecycle.ViewModel
import com.alltrails.lunch.backend.NearbySearchResponse
import com.alltrails.lunch.core.LatLng
import com.alltrails.lunch.core.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val placesRepo: PlacesRepository
) : ViewModel() {

    // TODO: define new Place model instead of reusing the one from the response

    fun nearbySearch(): Observable<Lce<List<NearbySearchResponse.Place>>> {
        // TODO: use actual location
        val location = LatLng(32.807974, -117.261911)
        return placesRepo.nearbySearch(location)
    }
}