package com.example.chamasegura.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.chamasegura.R
import com.google.android.gms.maps.MapFragment

class fragment_map_burn_info : Fragment(R.layout.fragment_map_burn_info), OnMapReadyCallback {

    private var latitude: Float = 0.0f
    private var longitude: Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            latitude = it.getFloat("latitude")
            longitude = it.getFloat("longitude")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(latitude.toDouble(), longitude.toDouble())
        googleMap.addMarker(MarkerOptions().position(location).title(getString(R.string.burn_location)))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    companion object {
        @JvmStatic
        fun newInstance(latitude: Float, longitude: Float) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putFloat("latitude", latitude)
                    putFloat("longitude", longitude)
                }
            }
    }
}