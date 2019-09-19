package com.magnusenevoldsen.agricircle.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.magnusenevoldsen.agricircle.R

import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {


    private lateinit var mapViewModel: MapViewModel
    private lateinit var mMap : GoogleMap


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_map, container, false)


//        mMap.setMinZoomPreference(6.0f)
//        mMap.setMaxZoomPreference(14.0f)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)



        return root
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

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
