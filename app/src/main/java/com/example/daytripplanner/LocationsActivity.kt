package com.example.daytripplanner

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import kotlin.collections.List

class LocationsActivity : AppCompatActivity() {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(R.string.locationsTitle)
        setContentView(R.layout.activity_locations)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@LocationsActivity)


        //get latlng from last activity
        val latlng: LatLng = intent.getParcelableExtra("latlng")
        Log.d("Locations", "LatLng is $latlng")

        lateinit var adapter: LocationAdapter
        try {
            doAsync {
                val locations = getLocations(latlng)
                runOnUiThread {
                    adapter = LocationAdapter(locations)
                    recyclerView.adapter = adapter
                }
            }
        }  catch (exception: Exception) {
            // Switch back to the UI Thread (required to update the UI)
            runOnUiThread {
                exception.printStackTrace()
                Toast.makeText(
                    this@LocationsActivity,
                    "Failed to retrieve businesses",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun getLocations(latLng: LatLng): List<Location> {
        var searchTerm = getSharedPreferences("day-trip-planner", 0).getString("attractionWord", "")
        val lat = latLng.latitude
        val lon = latLng.longitude
        Log.d("LocationsActivity", "Search term is $searchTerm")
        val locations = mutableListOf<Location>()
        // Create the Request object
        val request = Request.Builder()
            .url("https://api.yelp.com/v3/businesses/search?term=$searchTerm&latitude=$lat&longitude=$lon&sort_by=distance")
            .header("Authorization", "Bearer "+getString(R.string.yelp_api_key))
            .build()
        val response = okHttpClient.newCall(request).execute()
        // Get the JSON string body, if there was one
        val responseString = response.body?.string()
        Log.d("LocationsActivity", "$responseString")
        if (response.isSuccessful && !responseString.isNullOrEmpty()) {
            // Parse our JSON string
            val json = JSONObject(responseString)
            val businesses = json.getJSONArray("businesses")
            var l = businesses.length()
            Log.d("LocationsActivity", "Length of return array = $l")
            var size = getSharedPreferences("day-trip-planner", 0).getInt("seekBar_attractions", 10)
            for (i in 0 until size) {
                val curr = businesses.getJSONObject(i)
                Log.d("LocationsActivity", "curr = $curr")
                val name = curr.getString("name")
                var pricePt: String?
                try {
                    pricePt = curr.getString("price")
                } catch (exception: Exception){
                    pricePt = null
                }
                val rating = curr.getDouble("rating")
                val address = ((curr.getJSONObject("location").getString("address1")))
                val webpage = curr.getString("url")
                val phone = curr.getString("phone")
                var lat = ((curr.getJSONObject("coordinates").getDouble("latitude")))
                var long = (curr.getJSONObject("coordinates").getDouble("longitude"))
                val ll: LatLng= LatLng(lat, long)
                locations.add(
                    Location(
                        name = name,
                        pricePt = pricePt,
                        rating = rating,
                        address = address,
                        webpage = webpage,
                        phone = phone,
                        coords=ll
                    )
                )
            }

        }
        for (location in locations) {
            var currName = location.name
            Log.d("Locations Activity:", "$currName")
        }
        return locations
    }
}