package com.magnusenevoldsen.agricircle.ui.sampling

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.magnusenevoldsen.agricircle.R

class SamplingFragment : Fragment(), OnMapReadyCallback {


    private lateinit var samplingViewModel: SamplingViewModel
    private lateinit var mMap : GoogleMap
    private var root : View? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        samplingViewModel =ViewModelProviders.of(this).get(SamplingViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_sampling, container, false)


        val mapFragment = childFragmentManager.findFragmentById(R.id.samplingMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Buttons
        val positionFAB : FloatingActionButton = root!!.findViewById(R.id.samplingPositionFloatingActionButton)
        positionFAB.setColorFilter(Color.WHITE)
        positionFAB.setOnClickListener {
            sendMessageToUser(root!!, "You clicked on Position")
        }

        val fieldFAB : FloatingActionButton = root!!.findViewById(R.id.samplingFieldFloatingActionButton)
        fieldFAB.setColorFilter(Color.WHITE)
        fieldFAB.setOnClickListener {
            sendMessageToUser(root!!, "You clicked on Field")
        }

        val samplingFAB : FloatingActionButton = root!!.findViewById(R.id.samplingFloatingActionButton)
        samplingFAB.setColorFilter(Color.WHITE)
        samplingFAB.setOnClickListener {
            val intent = Intent (activity, SamplingParametersActivity::class.java)
            activity!!.startActivity(intent)


        }








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

    fun sendMessageToUser(view : View,  message: String) {
        val mySnackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }
}