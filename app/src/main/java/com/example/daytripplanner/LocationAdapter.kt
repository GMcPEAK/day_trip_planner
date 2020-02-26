package com.example.daytripplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.List

class LocationAdapter(val locations: List<Location>) : RecyclerView.Adapter<LocationAdapter.ViewHolder>(){
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.name)

        val pricePt: TextView = itemView.findViewById(R.id.pricePt)

        val rating: RatingBar = itemView.findViewById(R.id.rating)

        val address: TextView = itemView.findViewById(R.id.address)

        val webpage: TextView = itemView.findViewById(R.id.webpage)

        val phone: TextView = itemView.findViewById(R.id.phoneNumber)
    }

    // The adapter needs to render a new row and needs to know what XML file to use
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Layout inflation: read, parse XML file and return a reference to the root layout
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.row_location, parent, false)
        return ViewHolder(view)
    }

    //The adapter has a row that's ready to be rendered and needs the content filled in
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentLocation = locations[position]
        holder.name.text = currentLocation.name
        holder.address.text = currentLocation.address
        if (currentLocation.pricePt != null) {
            holder.pricePt.text = "Price: " + currentLocation.pricePt
        }
        holder.webpage.text = currentLocation.webpage
        if (currentLocation.phone != null) {
            holder.phone.text = currentLocation.phone
        }
        holder.rating.rating = (currentLocation.rating).toFloat()
    }

    //Return the total number of rows you expect your list to have
    override fun getItemCount(): Int {
        return locations.size
    }
}