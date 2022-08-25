package com.alltrails.lunch.core

import com.alltrails.lunch.backend.NearbySearchResponse

data class NearbyPlaces(
    val places: List<NearbySearchResponse.Place>,
    val location: LatLng,
)
