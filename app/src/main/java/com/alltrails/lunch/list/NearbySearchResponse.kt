package com.alltrails.lunch.list

class NearbySearchResponse(
    val html_attributions: List<String>,
    val results: List<Place>,
    val status: PlacesSearchStatus
) {
    enum class PlacesSearchStatus {
        OK,
        ZERO_RESULTS,
        INVALID_REQUEST,
        OVER_QUERY_LIMIT,
        REQUEST_DENIED,
        UNKNOWN_ERROR
    }

    // https://developers.google.com/maps/documentation/places/web-service/search-nearby#Place
    class Place(
        val name: String?,
        val geometry: Geometry? = null
    ) {
        class Geometry (
            val location: Location
        ){
            class Location(val lat: Double, val lng: Double)
        }
    }
}