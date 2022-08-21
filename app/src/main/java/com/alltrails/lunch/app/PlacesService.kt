package com.alltrails.lunch.app

import com.alltrails.lunch.list.NearbySearchResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesService {
    @GET("nearbysearch/json")
    fun nearbySearch(@Query("location") location: String): Observable<NearbySearchResponse>
}