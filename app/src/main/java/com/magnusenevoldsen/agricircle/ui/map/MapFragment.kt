package com.magnusenevoldsen.agricircle.ui.map

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.magnusenevoldsen.agricircle.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.location.*
import com.magnusenevoldsen.agricircle.MainActivity

class MapFragment : Fragment(), OnMapReadyCallback{




    private lateinit var mapViewModel: MapViewModel
    private lateinit var mMap : GoogleMap






    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Buttons

        val positionFAB : FloatingActionButton = root.findViewById(R.id.positionFloatingActionButton)
        positionFAB.setOnClickListener {

        }

        val fieldFAB : FloatingActionButton = root.findViewById(R.id.fieldFloatingActionButton)
        fieldFAB.setOnClickListener {

        }





        return root
    }





    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true




        //Delete later ---------
        val campusLyngby = LatLng(55.785558, 12.521564)
        mMap.addMarker(MarkerOptions().position(campusLyngby).title("Campus Lyngby"))
        var zoom = 15.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusLyngby, zoom))
    }











}




//        val textView: TextView = root.findViewById(R.id.text_home)
//        mapViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
