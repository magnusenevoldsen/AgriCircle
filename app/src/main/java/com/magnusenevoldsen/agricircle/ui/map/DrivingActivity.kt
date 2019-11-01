package com.magnusenevoldsen.agricircle.ui.map

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.magnusenevoldsen.agricircle.AgriCircleBackend
import com.magnusenevoldsen.agricircle.LocalBackend
import com.magnusenevoldsen.agricircle.R
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class DrivingActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap : GoogleMap
    private val zoom : Float = 18.0f
    private var locationRequest : LocationRequest? = null
    private var fusedLocationClient : FusedLocationProviderClient? = null
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
    var yourSpeedTextView : TextView? = null
    var yourSpeedNumberTextView : TextView? = null
    var suggestedSpeedTextView : TextView? = null
    var yourTractorImageView : ImageView? = null
    var suggestedSpeedNumberTextView : TextView? = null
    var suggestedTractorImageView : ImageView? = null
    var finishFAB : ExtendedFloatingActionButton? = null
    var pauseFAB : ExtendedFloatingActionButton? = null

    //Track
    var trackArray : ArrayList<LatLng> = ArrayList()

    //Time
    private var playOrPause : Boolean = false
    var pauseOffset : Long = 0
    var timeTextView : Chronometer? = null

    //Calculations
    var oldLocationSave : LatLng? = null
    var newLocationSave : LatLng? = null
    var oldTimeSave : Long? = null
    var newTimeSave : Long? = null


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
        yourSpeedTextView = findViewById(R.id.drivingCurrentSpeedTextView)
        yourSpeedNumberTextView = findViewById(R.id.drivingNumberCurrentSpeedTextView)
        suggestedSpeedTextView = findViewById(R.id.drivingSuggestedSpeedTextView)
        suggestedSpeedNumberTextView = findViewById(R.id.drivingNumberSuggestedSpeedTextView)

        //Time
        timeTextView = findViewById(R.id.drivingTimeTextView)
//        timeTextView!!.format = "Time: %s"

//        timeTextView!!.setOnChronometerTickListener {
//            timeTextView!!.base = SystemClock.elapsedRealtime()
//        }

        //Tractors
        yourTractorImageView = findViewById(R.id.drivingCurrentTractorImageView)
        suggestedTractorImageView = findViewById(R.id.drivingSuggestedTractorImageView)

        //Change Tractor Color
        val colorOrange = ResourcesCompat.getColor(resources, R.color.colorOrange, null)
        yourTractorImageView!!.setColorFilter(colorOrange)
        val colorGreen = ResourcesCompat.getColor(resources, R.color.colorGreen, null)
        suggestedTractorImageView!!.setColorFilter(colorGreen)

        //Buttons
        finishFAB = findViewById(R.id.finishFloatingActionButton)
        pauseFAB = findViewById(R.id.pauseFloatingActionButton)


        //Click Listeners
        finishFAB!!.setOnClickListener {
            finishSession()
        }

        pauseFAB!!.setOnClickListener {
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

                    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    println("Location information:")
                    println(""+location.toString())

                    println("Accuracy : "+location.accuracy)
                    println("Time : "+location.time)
                    println("Altitude : "+location.altitude)
                    println("Speed : "+location.speed)
                    println("Elapsed Realtime Nanos : "+location.elapsedRealtimeNanos)




                    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")



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
                    if (location != null && playOrPause) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoom))   //Brug animateCamera eller moveCamera
                        mMap.addMarker(MarkerOptions().position(currentLocation).title("You are here"))
                        println("Lat = "+currentLocation.latitude+", Lng = "+currentLocation.longitude+"")


// -----------------------------------------------------------------------------------------------------------------------------------------
                        // Draw line
                        drawTrack(currentLocation)

// -----------------------------------------------------------------------------------------------------------------------------------------

                        val kmh = (location.speed * 3.6)
                        val kmhString = kmh.toString().substring(0, 3)

//                        println("SPEED mps : "+location.speed)
//                        println("SPEED kmh : "+(location.speed * 3.6))
//                        println("Accu = "+location.accuracy)

                        if (kmh > 40 && kmh < 60)
                            yourTractorImageView!!.setColorFilter(Color.GREEN)
                        else
                            yourTractorImageView!!.setColorFilter(Color.RED)




                        yourSpeedNumberTextView!!.text = kmhString + " km/h"




                        //New test

                        //Set time
//                        if (newTimeSave != null) {
//                            oldTimeSave = newTimeSave
//                        }
//                        newTimeSave = location.elapsedRealtimeNanos
//
//                        //Set location
//                        if (newLocationSave != null) {
//                            oldLocationSave = newLocationSave
//                        }
//                        newLocationSave = LatLng(location.latitude, location.longitude)
//
//                        if (oldTimeSave != null) {
//                            println("----------------------------------")
//                            println("TIME")
//                            var time = calculateTime(oldTimeSave!!, newTimeSave!!)
//                            println(time)
//                            println("In seconds, that is : "+ (time / 1000000000)+ " seconds")
//                        }
//
//                        //Set location
//                        if (oldLocationSave != null) {
//                            println("----------------------------------")
//                            println("Distance")
//                            var distance = calculateDistance(oldLocationSave!!, newLocationSave!!)
//                            println(distance)
//
//
//
//                            var time = calculateTime(oldTimeSave!!, newTimeSave!!)
//
//                            println("----------------------------------")
//                            println("Speed")
//                            var speed = calculateSpeed(distance, time)
//                            println(speed)
//
//                            yourSpeedNumberTextView!!.text = ""+speed
//
//
//                        }
//
//
//
//                        println("----------------------------------")
//                        println("stats")
//                        println("old time : "+oldTimeSave)
//                        println("new time : "+newTimeSave)
//                        println("old location : "+oldLocationSave)
//                        println("new location : "+newLocationSave)

















// -----------------------------------------------------------------------------------------------------------------------------------------
                        //  Calculate speed

//                        println("----------------------------------")
//                        println("TIME")
//                        lastLocationTime = currentLocationTime
//                        currentLocationTime = System.currentTimeMillis()
//                        println("Last time : $lastLocationTime")
//                        println("Current time : $currentLocationTime")
//
//                        println("----------------------------------")
//                        println("LOCATION")
//                        speedLastLocation = speedCurrentLocation
//                        speedCurrentLocation = currentLocation
//                        println("Last location : $speedLastLocation")
//                        println("Current location : $speedCurrentLocation")
//
//                        if (speedLastLocation != null) {
//                            println("----------------------------------")
//                            println("CALCULATION")
//                            speedCurrently = Math.sqrt(
//                                Math.pow(speedCurrentLocation!!.longitude - speedLastLocation!!.longitude, 2.0)
//                                        +Math.pow(speedCurrentLocation!!.latitude - speedLastLocation!!.latitude, 2.0)
//                            )
//                            println("----------------------------------")
////                        speedCurrently = speedCurrently / (lastLocationTime - currentLocationTime.to)
//                        }

// -----------------------------------------------------------------------------------------------------------------------------------------





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
        //Update field name
        fieldNameTextView!!.text = LocalBackend.allFields[AgriCircleBackend.selectedField].name
        //Update workitem text
        //Update timer
        //Update speed
        //Update image
        var imageUrl : String = LocalBackend.allFields[AgriCircleBackend.selectedField].activeCropImageUrl

        if (imageUrl != null){
            try {
                Picasso.get().load(imageUrl).into(fieldPictureImageView)
            } catch (e : IllegalArgumentException) {
                Log.d("", e.toString())
            }
        }
    }

    fun drawTrack(location : LatLng) {
        //Add location to array
        trackArray.add(location)

        //Add a marker
        addCustomMarker(location)

        //Get nr. of tracks
        var amountOfTracks = trackArray.size

        //Draw the track
        if (amountOfTracks >= 2){
            var poly : Polyline = mMap.addPolyline(
                PolylineOptions()
                    .clickable(false)
                    .add(trackArray[amountOfTracks-2],
                        trackArray[amountOfTracks-1])
            )
            poly.color = ContextCompat.getColor(this, R.color.colorPolygonDriving)
        }

//        println("-----------------------------------------------")
//        println("Time be like : " + SystemClock.elapsedRealtime())
//        println("-----------------------------------------------")

    }

    fun addCustomMarker(location : LatLng) {
        mMap.addMarker(MarkerOptions().position(location))
    }


    fun calculateSpeed(distanceTraveled : Float, timeTraveled : Long) : Float {
        //We are given nano seconds and meters

        var time = timeTraveled
        var distance = distanceTraveled
        var kmh : Float = 0f

        //Nanoseconds to hours
        time = time / (1000000000 * 60 * 60)    //1000000000 makes it seconds -> minutes -> hours


        println("distance in meters = $distance")
        //Meters to kms
        distance = distance / 1000 //Makes it km


        var mps = (distance / (timeTraveled / 1000000000))
        println("mps = $mps")

        println("time = $time")
        println("distance = $distance")


        // km/hour
        kmh = distance / time
        println("--------")
        println("Float : $kmh")
//        println("Double : ${kmh.toDouble()}")
//        println("Big decimal : ${kmh.toBigDecimal()}")
//        println("Int : ${kmh.toInt()}")

        return kmh
    }


    fun calculateTime (oldTime : Long, newTime : Long) : Long {
        return newTime-oldTime
    }

    fun calculateDistance (oldLocation : LatLng, newLocation : LatLng) : Float {
        var fromLocation = Location(LocationManager.GPS_PROVIDER)
        fromLocation.latitude = oldLocation.latitude
        fromLocation.longitude = oldLocation.longitude

        var toLocation = Location(LocationManager.GPS_PROVIDER)
        toLocation.latitude = newLocation.latitude
        toLocation.longitude = newLocation.longitude


        var distance = fromLocation.distanceTo(toLocation)

        return distance
    }


    fun finishSession () {
        //Save progress
        //Go to workspace view with info about the session

        //Currently just goes back
        super.onBackPressed() //Remove this
    }

    fun pauseSession () {
        //Pause session
        playOrPause = !playOrPause
        switchPlayPause()
        startStopChronometer()

    }

    fun switchPlayPause() {
        if (playOrPause) {  //Started
            pauseFAB!!.setIconResource(R.drawable.ic_pause_circle_outline_black_24dp)
            pauseFAB!!.text = "Pause"
        } else { //Stopped
            pauseFAB!!.setIconResource(R.drawable.ic_play_circle_outline_black_24dp)
            pauseFAB!!.text = "Start"
        }
    }

    fun startStopChronometer() {
        if (playOrPause) {  //Started
            timeTextView!!.base = SystemClock.elapsedRealtime() - pauseOffset
            timeTextView!!.start()
        } else {    //Stopped
            timeTextView!!.stop()
            pauseOffset = SystemClock.elapsedRealtime() - timeTextView!!.base;
        }
    }

    fun resetChronometer() {
        timeTextView!!.base = SystemClock.elapsedRealtime()
        pauseOffset = 0
    }

    override fun onResume() {
        super.onResume()
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
