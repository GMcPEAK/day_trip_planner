package com.example.daytripplanner

import com.google.android.gms.maps.model.LatLng

data class Location(
    val name: String,
    val pricePt: String?,
    val rating: Double,
    val address: String,
    val webpage: String,
    val phone: String?,
    val coords: LatLng
)