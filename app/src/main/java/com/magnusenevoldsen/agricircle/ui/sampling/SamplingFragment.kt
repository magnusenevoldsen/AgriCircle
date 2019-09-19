package com.magnusenevoldsen.agricircle.ui.sampling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.magnusenevoldsen.agricircle.R

class SamplingFragment : Fragment(), OnMapReadyCallback {


    private lateinit var samplingViewModel: SamplingViewModel
    private lateinit var mMap : GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        samplingViewModel =ViewModelProviders.of(this).get(SamplingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_sampling, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.samplingMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)







        return root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Delete later ---------
        val campusBallerup = LatLng(55.730975, 12.396752)
        mMap.addMarker(MarkerOptions().position(campusBallerup).title("Campus Ballerup"))
        var zoom = 15.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusBallerup, zoom))
    }
}