package com.example.daytripplanner

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView


class LocationAdapter(val locations: List<Location>) : RecyclerView.Adapter<LocationAdapter.ViewHolder>(),
    View.OnClickListener {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.name)

        val pricePt: TextView = itemView.findViewById(R.id.pricePt)

        val rating: RatingBar = itemView.findViewById(R.id.rating)

        val address: TextView = itemView.findViewById(R.id.address)

        var webpage: ImageButton = itemView.findViewById(R.id.web_button)

        val phone: ImageButton = itemView.findViewById(R.id.phone_button)
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
        var cont = holder.webpage.getContext()
        holder.webpage.setOnClickListener {
            val intent = Intent(android.content.Intent.ACTION_VIEW)
            intent.data = Uri.parse(currentLocation.webpage)
            startActivity(cont, intent, null)
        }
        if (currentLocation.phone != null) {
            holder.phone.setOnClickListener {
                val intent = Intent(android.content.Intent.ACTION_DIAL)
                intent.setData(Uri.parse("tel:"+currentLocation.phone));
                startActivity(cont, intent, null)
            }
        } else {
            holder.phone.visibility = View.GONE
        }
        holder.rating.rating = (currentLocation.rating).toFloat()
    }

    //Return the total number of rows you expect your list to have
    override fun getItemCount(): Int {
        return locations.size
    }

    override fun onClick(v: View?) {

    }
}