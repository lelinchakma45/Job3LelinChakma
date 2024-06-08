package com.example.job3lelinchakma

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.job3lelinchakma.ViewModels.FirestoreViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.example.job3lelinchakma.databinding.ActivityMapsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var firestoreViewModel: FirestoreViewModel
    private val boundsBuilder = LatLngBounds.Builder()
    private var hasValidLocations = false
    private lateinit var bottomSheet: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)

        binding.zoomIn.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }
        binding.zoomOut.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }
        binding.autoFocus.setOnClickListener {
            if (hasValidLocations) {
                val bounds = boundsBuilder.build()
                val padding = 500
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                mMap.animateCamera(cameraUpdate)
            }
        }
        val standardBottomSheetBehavior = BottomSheetBehavior.from(binding.standardBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.layerBtn.setOnClickListener {
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.standardBottomSheet.findViewById<RadioButton>(R.id.satelliteBtn).setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
            else{
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        }


        firestoreViewModel.getAllUsers { userList ->
            for (user in userList) {
                val userLocation = user.location
                if (userLocation.isNotEmpty()) {
                    val latLng = parseLocation(userLocation)
                    val markerOptions = MarkerOptions().position(latLng).title(user.displayName)
                    mMap.addMarker(markerOptions)
                    boundsBuilder.include(latLng)
                    hasValidLocations = true
                }
            }

            if (hasValidLocations) {
                val bounds = boundsBuilder.build()
                val padding =500
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                mMap.animateCamera(cameraUpdate)
            }
        }
    }

    private fun parseLocation(location: String): LatLng {
        val latLngSplit = location.split(", ")
        val latitude = latLngSplit[0].substringAfter("Lat: ").toDouble()
        val longitude = latLngSplit[1].substringAfter("Long: ").toDouble()
        return LatLng(latitude, longitude)
    }
}
