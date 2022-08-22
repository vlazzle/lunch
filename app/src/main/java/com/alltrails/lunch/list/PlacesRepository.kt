package com.alltrails.lunch.list

import com.alltrails.lunch.backend.NearbySearchResponse
import com.alltrails.lunch.backend.PlacesService
import com.alltrails.lunch.core.LatLng
import com.alltrails.lunch.core.Lce
import io.reactivex.rxjava3.core.Observable
import java.io.IOException
import javax.inject.Inject

class PlacesRepository @Inject constructor(
    private val placesService: PlacesService
) {
    // TODO: define new Place model instead of reusing the one from the response
    fun nearbySearch(location: LatLng): Observable<Lce<List<NearbySearchResponse.Place>>> {
        // TODO: use stringConverter for LatLng
        //  https://square.github.io/retrofit/2.x/retrofit/retrofit2/Retrofit.html#stringConverter-java.lang.reflect.Type-java.lang.annotation.Annotation:A-
        return placesService.nearbySearch("${location.lat},${location.lng}")
            .map { response: NearbySearchResponse ->
                if (response.status == NearbySearchResponse.PlacesSearchStatus.OK) {
                    Lce.Content(response.results)
                } else {
                    Lce.Error(Throwable("${response.status}: ${response.error_message}"))
                }
            }
            .onErrorReturn {
                if (it is IOException) {
                    Lce.Error(it)
                } else {
                    throw it
                }
            }
        // TODO: caching/multicasting
        //  .replay()
        //  .autoConnect()
    }
}
