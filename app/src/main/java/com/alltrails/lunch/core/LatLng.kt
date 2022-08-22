package com.alltrails.lunch.core

import android.location.Location

data class LatLng(val lat: Double, val lng: Double) {
    companion object {
        fun fromLocation(location: Location)= LatLng(location.latitude, location.longitude)
    }
}