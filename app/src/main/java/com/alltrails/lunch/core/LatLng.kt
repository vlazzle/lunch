package com.alltrails.lunch.core

import android.location.Location
import com.google.android.gms.maps.model.LatLng as GLatLng

data class LatLng(val lat: Double, val lng: Double) {
    companion object {
        fun fromLocation(location: Location) = LatLng(location.latitude, location.longitude)
    }

    fun toGoogleMapsLatLng(): GLatLng {
        return GLatLng(lat, lng)
    }
}