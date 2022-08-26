package com.alltrails.lunch.backend

class PlaceDetailsResponse(
    val html_attributions: List<String>,
    val result: Place,
    val status: PlacesDetailsStatus,
    val info_messages: String?,
) {
    class Place(
        val formatted_address: String?,
        val formatted_phone_number: String?,
        val name: String?,
        val place_id: String? = null,
        val rating: Float? = null,
        val user_ratings_total: Int? = null,
        val price_level: Int? = null,
        val photos: List<PlacePhoto>? = null
    )

    // https://developers.google.com/maps/documentation/places/web-service/details#PlacesDetailsStatus
    enum class PlacesDetailsStatus {
        OK,
        ZERO_RESULTS,
        NOT_FOUND,
        INVALID_REQUEST,
        OVER_QUERY_LIMIT,
        REQUEST_DENIED,
        UNKNOWN_ERROR,
    }
}
