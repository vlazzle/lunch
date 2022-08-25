package com.alltrails.lunch.core

import android.location.Location

data class LatLng(val lat: Double, val lng: Double) {
    companion object {
        val nullIsland = LatLng(0.0, 0.0)

        fun fromLocation(location: Location) = LatLng(location.latitude, location.longitude)
    }
}