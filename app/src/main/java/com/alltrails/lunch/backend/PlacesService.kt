package com.alltrails.lunch.backend

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesService {
    @GET("nearbysearch/json")
    fun nearbySearch(@Query("location") location: String): Observable<NearbySearchResponse>

    @GET("details/json")
    fun placeDetails(@Query("place_id") placeId: String): Observable<PlaceDetailsResponse>
}