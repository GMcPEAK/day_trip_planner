package com.example.daytripplanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.json.JSONObject

private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationProvider: FusedLocationProviderClient

    private lateinit var currentLocation: ImageButton

    private lateinit var goToList: Button

    private lateinit var mMap: GoogleMap

    private var currentAddress: Address? = null

    lateinit var latLng: LatLng

    private lateinit var locations: List<Location>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        latLng =intent.getParcelableExtra("latlng")
        locationProvider = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            checkPermissions()
        }

        goToList = findViewById(R.id.confirm)
        goToList.setOnClickListener {
                val intent = Intent(this, LocationsActivity::class.java)
                intent.putExtra("latlng", latLng)
                startActivity(intent)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun getLocations(latLng: LatLng, searchTerm: String?): List<Location> {
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
            for (i in 0 until businesses.length()) {
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
                val address = "temp"
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

    fun checkPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted - we can now access the GPS
            Log.d("MapsActivity", "GPS permission granted")
        } else {
            //Permission has not been granted

            //ask for the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                200
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            //This is the result of our GPS permission prompt
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MapsActivity", "GPS permission granted")
            } else {
                //either user declined permission or system denied
                Log.d("MapsActivity", "Permission denied")

                //make sure that the user is running at least Marshmallow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        //user denied, but can be re-prompted
                    } else {
                        //user denied, and doesn't want to be promtped again
                        Toast.makeText(
                            this,
                            "To use this feature, go into Settings and enable the location permission",
                            Toast.LENGTH_LONG
                        ).show()
                        val settingsIntent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:$packageName")
                        )

                        startActivityForResult(settingsIntent, 100)
                    }
                }
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("Map", "Hey you made it here")
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Current location")
                .icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        try {
            doAsync {
                var searchTerm = getSharedPreferences("day-trip-planner", 0).getString("attractionWord", "")
                locations = getLocations(latLng, searchTerm)
                runOnUiThread {
                    var size = getSharedPreferences("day-trip-planner", 0).getInt("seekBar_attractions", 10)
                    for (i in 0 until size) {
                        mMap.addMarker(
                            MarkerOptions()
                                .position(locations[i].coords)
                                .title(locations[i].name)
                                .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        )
                    }
                }
                searchTerm = getSharedPreferences("day-trip-planner", 0).getString("foodWord", "")
                locations = getLocations(latLng, searchTerm)
                runOnUiThread {
                    var size = getSharedPreferences("day-trip-planner", 0).getInt("seekBar_food", 10)
                    for (i in 0 until size) {
                        mMap.addMarker(
                            MarkerOptions()
                                .position(locations[i].coords)
                                .title(locations[i].name)
                                .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        )
                    }
                }
            }
        }  catch (exception: Exception) {
            // Switch back to the UI Thread (required to update the UI)
            runOnUiThread {
                Log.d("STACK TRACE","STACK TRACE")
                exception.printStackTrace()
                Toast.makeText(
                    this@MapsActivity,
                    "Failed to retrieve businesses",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        try {
            Log.d("STATIONS PART", "Trying to get nearest Metro Station")
            doAsync {
                locations = getStations(latLng)
                runOnUiThread {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(locations[0].coords)
                            .title(locations[0].name)
                            .icon(
                                BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                            )
                    )

                }
            }
        }  catch (exception: Exception) {
            // Switch back to the UI Thread (required to update the UI)
            runOnUiThread {
                Log.d("STACK TRACE","STACK TRACE")
                exception.printStackTrace()
                Toast.makeText(
                    this@MapsActivity,
                    "Failed to retrieve nearby Metro Stations",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun getStations(latLng: LatLng): List<Location> {
        val lat = latLng.latitude
        val lon = latLng.longitude
        val locations = mutableListOf<Location>()
        // Create the Request object
        val request = Request.Builder()
            .url("https://api.wmata.com/Rail.svc/json/jStationEntrances?Lat=$lat&Lon=$lon&Radius=500")
            .header("api_key", getString(R.string.wmata_api_key))
            .build()
        val response = okHttpClient.newCall(request).execute()
        // Get the JSON string body, if there was one
        val responseString = response.body?.string()
        Log.d("LocationsActivity", "$responseString")
        if (response.isSuccessful && !responseString.isNullOrEmpty()) {
            // Parse our JSON string
            val json = JSONObject(responseString)
            val entrances = json.getJSONArray("Entrances")
            val curr = entrances.getJSONObject(1)
            val closestStationCode = curr.getString("StationCode1")
            val request2 = Request.Builder()
                .url("https://api.wmata.com/Rail.svc/json/jStationInfo?StationCode=$closestStationCode")
                .header("api_key", getString(R.string.wmata_api_key))
                .build()
            val response2 = okHttpClient.newCall(request2).execute()
            // Get the JSON string body, if there was one
            val responseString2 = response2.body?.string()
            Log.d("LocationsActivity", "$responseString2")
            if (response.isSuccessful && !responseString.isNullOrEmpty()) {
                // Parse our JSON string
                val json2 = JSONObject(responseString2)
                locations.add(
                    Location(
                        name = json2.getString("Name"),
                        coords = LatLng(curr.getDouble("Lat"), curr.getDouble("Lon")),
                        address = "",
                        webpage = "",
                        phone = "",
                        pricePt = "",
                        rating = 0.0
                    )
                )
            }
        }
        return locations
    }
}