package com.example.daytripplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.List

class LocationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)
        title = getString(R.string.locationsTitle)

        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        val locations = getFakeLocations()
        val adapter = LocationAdapter(locations)
        recyclerView.adapter = adapter
    }

    fun getFakeLocations(): List<Location> {
        return listOf(
            Location (
                name = "Location A",
                rating = 5.0,
                pricePt = null,
                address = "123 Example Ave",
                webpage = "wikipedia.org",
                phone = "+13022552020"
            ),
            Location (
                name = "Location B",
                pricePt = "$",
                rating = 4.5,
                address = "123 Example Rd",
                webpage = "google.com",
                phone = "+13012577777"
            ),
            Location (
                name = "Location C",
                pricePt = "$$$",
                rating = 4.9,
                address = "123 Example St",
                webpage = "gwu.edu",
                phone = null
            )
        )
    }
}