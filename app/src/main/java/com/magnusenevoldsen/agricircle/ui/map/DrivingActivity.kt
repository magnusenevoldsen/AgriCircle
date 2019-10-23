package com.magnusenevoldsen.agricircle.ui.map

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.magnusenevoldsen.agricircle.AgriCircleBackend
import com.magnusenevoldsen.agricircle.MainActivity
import com.magnusenevoldsen.agricircle.R
import com.magnusenevoldsen.agricircle.ui.workspace.WorkspaceInfoFragment
import java.sql.Time
import java.time.LocalDateTime
import java.util.*

class DrivingActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap : GoogleMap
    private val zoom : Float = 18.0f
    private var locationRequest : LocationRequest? = null
    private var fusedLocationClient : FusedLocationProviderClient? = null
    private var updatesOn = true
    private var locationCallback : LocationCallback? = null
    private val MY_PERMISSION_FINE_LOCATION = 101
    private var speedCurrentLocation : LatLng? = null
    private var speedLastLocation : LatLng? = null
    private var speedCurrently : Double = 0.0
    private var lastLocationTime : Long? = null
    private var currentLocationTime : Long? = null

    //Views
    var fieldPictureImageView : ImageView? = null
    var fieldNameTextView : TextView? = null
    var fieldWorkTextView : TextView? = null
    var timeTextView : TextView? = null
    var yourSpeedTextView : TextView? = null
    var yourSpeedNumberTextView : TextView? = null
    var suggestedSpeedTextView : TextView? = null
    var yourTractorImageView : ImageView? = null
    var suggestedSpeedNumberTextView : TextView? = null
    var suggestedTractorImageView : ImageView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driving)




        //Layout setup

        //Map setup
        val mapFragment = supportFragmentManager.findFragmentById(R.id.drivingMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Field info
        fieldPictureImageView = findViewById(R.id.drivingIconImageView)
        fieldNameTextView = findViewById(R.id.drivingFieldTextView)
        fieldWorkTextView = findViewById(R.id.drivingWorkTextView)

        //Field progress
        timeTextView = findViewById(R.id.drivingTimeTextView)
        yourSpeedTextView = findViewById(R.id.drivingCurrentSpeedTextView)
        yourSpeedNumberTextView = findViewById(R.id.drivingNumberCurrentSpeedTextView)
        suggestedSpeedTextView = findViewById(R.id.drivingSuggestedSpeedTextView)
        suggestedSpeedNumberTextView = findViewById(R.id.drivingNumberSuggestedSpeedTextView)

        //Tractors
        yourTractorImageView = findViewById(R.id.drivingCurrentTractorImageView)
        suggestedTractorImageView = findViewById(R.id.drivingSuggestedTractorImageView)

        //Change Tractor Color
        val colorOrange = ResourcesCompat.getColor(resources, R.color.colorOrange, null)
        yourTractorImageView!!.setColorFilter(colorOrange)
        val colorGreen = ResourcesCompat.getColor(resources, R.color.colorGreen, null)
        suggestedTractorImageView!!.setColorFilter(colorGreen)

        //Buttons
        val finishFAB : ExtendedFloatingActionButton = findViewById(R.id.finishFloatingActionButton)
        val pauseFAB : ExtendedFloatingActionButton = findViewById(R.id.pauseFloatingActionButton)

        //Click Listeners
        finishFAB.setOnClickListener {
            finishSession()
        }

        pauseFAB.setOnClickListener {
            pauseSession()
        }

        //Get location
        locationRequest = LocationRequest()
        locationRequest!!.interval = 2000 // Find ud af hvor ofte der bør opdateres. pt 1 sek for test formål
        locationRequest!!.fastestInterval = 1000 //1 sec
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //Overvej at bruge HIGH ACCURACY istedet. / BALANCED

        //Location -> Hent den 1 gang når view åbner
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // ????????????
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient!!.lastLocation.addOnSuccessListener {location ->
                if (location != null) {
                    //Update UI             --- Go to field 0 instead????
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
            @TargetApi(Build.VERSION_CODES.O)
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                for (location in p0!!.locations) {
                    //Update UI
                    if (location != null && updatesOn) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoom))   //Brug animateCamera eller moveCamera
                        mMap.addMarker(MarkerOptions().position(currentLocation).title("You are here"))
                        println("Lat = "+currentLocation.latitude+", Lng = "+currentLocation.longitude+"")



                        println("----------------------------------")
                        println("TIME")
                        lastLocationTime = currentLocationTime
                        currentLocationTime = System.currentTimeMillis()
                        println("Last time : $lastLocationTime")
                        println("Current time : $currentLocationTime")

                        println("----------------------------------")
                        println("LOCATION")
                        speedLastLocation = speedCurrentLocation
                        speedCurrentLocation = currentLocation
                        println("Last location : $speedLastLocation")
                        println("Current location : $speedCurrentLocation")

                        if (speedLastLocation != null) {
                            println("----------------------------------")
                            println("CALCULATION")
                            speedCurrently = Math.sqrt(
                                Math.pow(speedCurrentLocation!!.longitude - speedLastLocation!!.longitude, 2.0)
                                        +Math.pow(speedCurrentLocation!!.latitude - speedLastLocation!!.latitude, 2.0)
                            )
                            println("----------------------------------")
//                        speedCurrently = speedCurrently / (lastLocationTime - currentLocationTime.to)
                        }



                    }
                }
            }
        }


        setupUI()



        





    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
//        val campusLyngby = LatLng(55.785558, 12.521564)
//        mMap.addMarker(MarkerOptions().position(campusLyngby).title("Campus Lyngby"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusLyngby, zoom))
    }

    fun setupUI() {
        fieldNameTextView!!.text = AgriCircleBackend.fields[AgriCircleBackend.selectedField].name
        //Update workitem text
        //Update timer
        //Update speed
        //Update image
    }


    fun finishSession () {
        //Save progress
        //Go to workspace view with info about the session

        //Currently just goes back
        super.onBackPressed() //Remove this
    }

    fun pauseSession () {
        //Pause session

        //Currently just goes back
        super.onBackPressed() //Remove this
    }

    override fun onResume() {
        super.onResume()
//        if (updatesOn)
        startLocationUpdates()
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }




}
