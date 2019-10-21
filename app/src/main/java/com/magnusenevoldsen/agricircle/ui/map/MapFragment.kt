package com.magnusenevoldsen.agricircle.ui.map

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.magnusenevoldsen.agricircle.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MapFragment : Fragment(), OnMapReadyCallback{




    private lateinit var mapViewModel: MapViewModel
    private lateinit var mMap : GoogleMap
    private var root : View? = null

    //Location
    private var fusedLocationClient : FusedLocationProviderClient? = null
    private val MY_PERMISSION_FINE_LOCATION = 101
    private var locationRequest : LocationRequest? = null
    private var updatesOn = false
    private var locationCallback : LocationCallback? = null
    private val zoom : Float = 18.0f
    private var counter : Int = 0


    //Test purposes
    private var constToggle : Boolean = false






    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_map, container, false)
        locationRequest = LocationRequest()
        locationRequest!!.interval = 2000 // Find ud af hvor ofte der bør opdateres. pt 1 sek for test formål
        locationRequest!!.fastestInterval = 1000 //1 sec
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //Overvej at bruge HIGH ACCURACY istedet. / BALANCED

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Top layout
        val constLayout = root!!.findViewById<ConstraintLayout>(R.id.mapConstraintLayout)
        constLayout.visibility = View.GONE //Hide view initially
        val actionOneImageButton = root!!.findViewById<ImageButton>(R.id.actionOneImageButton)
        actionOneImageButton.setColorFilter(R.color.colorAgricircle)
        val actionTwoImageButton = root!!.findViewById<ImageButton>(R.id.actionTwoImageButton)
        actionTwoImageButton.setColorFilter(R.color.colorAgricircle)

        //Click Listeners
        actionOneImageButton.setOnClickListener {
            startActivityOne()
        }
        actionTwoImageButton.setOnClickListener {
            startActivityTwo()
        }












        //Buttons

        val positionFAB : FloatingActionButton = root!!.findViewById(R.id.positionFloatingActionButton)
        positionFAB.setColorFilter(Color.WHITE)
        positionFAB.setOnClickListener {
//            updatesOn = !updatesOn
//            mMap.isMyLocationEnabled = updatesOn


            val arnakke = LatLng(55.671006, 11.770301)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(arnakke, 15.0f))

            var poly1 : Polyline = mMap.addPolyline(
                PolylineOptions().clickable(true)
                    .add(
                        LatLng(55.672343, 11.769163),
                        LatLng(55.669511, 11.766824),
                        LatLng(55.668428, 11.771030),
                        LatLng(55.669874, 11.774560),
                        LatLng(55.671653, 11.773927),
                        LatLng(55.672391, 11.771805)))


            poly1.color = Color.RED
            poly1.tag = "hej"



        }

        val fieldFAB : FloatingActionButton = root!!.findViewById(R.id.fieldFloatingActionButton)
        fieldFAB.setColorFilter(Color.WHITE)
        fieldFAB.setOnClickListener {
//            sendMessageToUser(root!!, "Not implemented")

            //Test purposes
            if (constToggle) constLayout.visibility = View.GONE
            else constLayout.visibility = View.VISIBLE
            constToggle = !constToggle

            mMap.clear() //Ikke korrekt funktion
        }



        //Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(root!!.context) // ????????????
        if (ActivityCompat.checkSelfPermission(root!!.context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient!!.lastLocation.addOnSuccessListener {location ->
                if (location != null) {
                    //Update UI
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoom))   //Brug animateCamera eller moveCamera
                    mMap.addMarker(MarkerOptions().position(currentLocation).title("You are here"))
                }
            }
        } else {
            //Request permission
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_FINE_LOCATION)
        }



        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                for (location in p0!!.locations) {
                    //Update UI
                    if (location != null && updatesOn) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoom))   //Brug animateCamera eller moveCamera
                        mMap.addMarker(MarkerOptions().position(currentLocation).title("You are here"))
                        sendMessageToUser(root!!, ""+counter+" : Lat = "+currentLocation.latitude+", Lng = "+currentLocation.longitude)
                        counter++
                    }
                }
            }
        }







        return root
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(root!!.context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    override fun onResume() {
        super.onResume()
//        if (updatesOn)
            startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID






        //Delete later ---------
//        val campusLyngby = LatLng(55.785558, 12.521564)
//        mMap.addMarker(MarkerOptions().position(campusLyngby).title("Campus Lyngby"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusLyngby, zoom))
    }


    fun sendMessageToUser(view : View,  message: String) {
        val mySnackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }

    fun startActivityOne() {
        //Needs data with the intent start : what activity etc
        val intent = Intent (activity, DrivingActivity::class.java)
        activity!!.startActivity(intent)
    }

    fun startActivityTwo() {
        //Needs data with the intent start : what activity etc
        val intent = Intent (activity, DrivingActivity::class.java)
        activity!!.startActivity(intent)
    }








}




//        val textView: TextView = root.findViewById(R.id.text_home)
//        mapViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
