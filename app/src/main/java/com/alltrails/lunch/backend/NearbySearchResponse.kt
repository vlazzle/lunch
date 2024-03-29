package com.alltrails.lunch.backend

class NearbySearchResponse(
    val html_attributions: List<String>,
    val results: List<Place>,
    val status: PlacesSearchStatus,
    val error_message: String?,
    val info_messages: String?,
    // TODO: pagination? https://developers.google.com/maps/documentation/places/web-service/search-nearby#PlaceSearchPaging
    val next_page_token: String?
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
        val geometry: Geometry? = null,
        val place_id: String? = null,
        val rating: Float? = null,
        val user_ratings_total: Int? = null,
        val price_level: Int? = null,
        val photos: List<PlacePhoto>? = null
    ) {
        class Geometry (val location: Location) {
            data class Location(val lat: Double, val lng: Double)
        }

    }

}