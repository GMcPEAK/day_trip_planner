package com.example.daytripplanner

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.doAsync


class MainActivity : AppCompatActivity() {
    private lateinit var address: EditText
    private lateinit var foodType: Spinner
    private lateinit var attractionType: Spinner
    private lateinit var num_restaurants: SeekBar
    private lateinit var num_attractions: SeekBar
    private lateinit var go: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("Main activity", "onCreate() called")

        val preferences: SharedPreferences = getSharedPreferences(
            "day-trip-planner",
            Context.MODE_PRIVATE
        )

        address = findViewById(R.id.address)
        address.setText(preferences.getString("address", ""))
        foodType = findViewById(R.id.foodSpinner)
        foodType.setSelection(preferences.getInt("foodSpinner", 0))
        attractionType = findViewById(R.id.attractionSpinner)
        attractionType.setSelection(preferences.getInt("attractionSpinner", 0))
        num_restaurants = findViewById(R.id.seekBar_food)
        num_restaurants.progress = preferences.getInt("seekBar_food", 0)
        num_attractions = findViewById(R.id.seekBar_attractions)
        num_attractions.progress = preferences.getInt("seekBar_attractions", 0)
        go = findViewById(R.id.goButton)
        go.isEnabled = false
        //in case all of the values were loaded & ready to go
        check()
        go.setOnClickListener {
            Log.d("Main activity", "onClick() called")
            var line = ""
            var lat: Double
            var long: Double
            lateinit var result: Address
            doAsync {
                val geocoder = Geocoder(this@MainActivity)
                var fullAddress = geocoder.getFromLocationName(address.text.toString(), 1)
                var result = fullAddress.first()
                lat = result.latitude
                long = result.longitude
                val latlng = LatLng(lat, long)
                line = fullAddress.toString()
                runOnUiThread {
                    val b = AlertDialog.Builder(this@MainActivity)
                    b.setTitle("Confirm address:")
                    b.setMessage(result.getAddressLine(0))
                    b.setPositiveButton("View Map") { popup, which ->
                        val intent = Intent(this@MainActivity, MapsActivity::class.java)
                        intent.putExtra("latlng", latlng)
                        startActivity(intent)

                    }
                    b.setNegativeButton("Cancel") {popup, which ->
                        val toast =
                            Toast.makeText(
                                this@MainActivity,
                                "Operation cancelled",
                                Toast.LENGTH_LONG
                            )
                        toast.show()
                    }
                    val popup = b.create()
                    popup.show()
                    preferences
                        .edit()
                        .putString("address", address.text.toString())
                        .putInt("foodSpinner", foodType.selectedItemPosition)
                        .putInt("attractionSpinner", attractionType.selectedItemPosition)
                        .putString("attractionWord", attractionType.selectedItem.toString())
                        .putInt("seekBar_food", num_restaurants.progress)
                        .putInt("seekBar_attractions", num_attractions.progress)
                        .putString("foodWord", foodType.selectedItem.toString())
                        .apply()
                }

            }
        }

            foodType.onItemSelectedListener = itemListener
            attractionType.onItemSelectedListener = itemListener
            num_restaurants.setOnSeekBarChangeListener(barListener)
            num_attractions.setOnSeekBarChangeListener(barListener)
            num_restaurants.max = 10
            num_attractions.max = 10
            address.addTextChangedListener(textWatcher)

        }

        private val barListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                check()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //do nothing
            }

        }

        private val itemListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                check()
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                check()
            }

        }

        private val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                check()
            }
        }

        fun check() {
            val enable: Boolean =
                address.text.toString().trim().isNotEmpty()
                        && foodType.selectedItem.toString() != "Select one..."
                        && attractionType.selectedItem.toString() != "Select one..."
            go.isEnabled = enable
        }
}
