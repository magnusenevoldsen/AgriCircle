package com.magnusenevoldsen.agricircle.ui.map

import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
import com.google.android.material.snackbar.Snackbar
import com.magnusenevoldsen.agricircle.LocalBackend
import com.magnusenevoldsen.agricircle.R
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class DrivingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap : GoogleMap
    private var locationRequest : LocationRequest? = null
    private var fusedLocationClient : FusedLocationProviderClient? = null
    private var locationCallback : LocationCallback? = null
    private val MY_PERMISSION_FINE_LOCATION = 101
    private var context : Context? = null

    //Location
    private var presetInterval : Long = 2000
    private var presetFastestInterval : Long = 1000
    private var presetPrecision : Int = LocationRequest.PRIORITY_HIGH_ACCURACY

    //Views
    private var fieldPictureImageView : ImageView? = null
    private var fieldNameTextView : TextView? = null
    private var fieldWorkTextView : TextView? = null
    private var yourSpeedTextView : TextView? = null
    private var yourSpeedNumberTextView : TextView? = null
    private var suggestedSpeedTextView : TextView? = null
    private var yourTractorImageView : ImageView? = null
    private var suggestedSpeedNumberTextView : TextView? = null
    private var suggestedTractorImageView : ImageView? = null
    private var finishFAB : ExtendedFloatingActionButton? = null
    private var pauseFAB : ExtendedFloatingActionButton? = null

    //Track
    private var trackArray : ArrayList<LatLng> = ArrayList()

    //Permission
    private var locationPermissionGranted : Boolean = false

    //Time
    private var playOrPause : Boolean = false
    private var pauseOffset : Long = 0
    private var timeTextView : Chronometer? = null

    //Suggested speed
    private var suggestedSpeedNumber : Int = 0
    private var fieldID : Int? = null

    private var activityText : String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driving)
        context = this

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
        locationRequest!!.interval = presetInterval // Find ud af hvor ofte der bør opdateres. pt 1 sek for test formål
        locationRequest!!.fastestInterval = presetFastestInterval //1 sec
        locationRequest!!.priority = presetPrecision //Overvej at bruge HIGH ACCURACY istedet. / BALANCED


        //Location -> Hent den 1 gang når view åbner
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            fusedLocationClient!!.lastLocation.addOnSuccessListener {location ->
                if (location != null) {
                    //Not used currently, map is moved to field instead
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
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoom))  //Disabled to keep user zoom

                        // Draw line
                        drawTrack(currentLocation)

                        //Calculate speed
                        val kmh = (location.speed * 3.6)
                        val kmhString = kmh.toString().substringBefore(".")

                        //Update UI
                        updateTractors(kmh)
                        yourSpeedNumberTextView!!.text = kmhString + getString(R.string.kilometerhour)

                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_FINE_LOCATION ->{
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = false
                }
                else{
                    sendMessageToUser(getString(R.string.user_declined_location_permission))
                }
                return
            }
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        if (locationPermissionGranted){
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false
        }

        setupUI()

        //Draw field
        var poly : Polygon = mMap.addPolygon(
            PolygonOptions()
                .clickable(true)
                .addAll(LocalBackend.allFields[fieldID!!].shapeCoordinates)
        )

        poly.tag = LocalBackend.allFields[fieldID!!].id
        poly.strokeColor = ContextCompat.getColor(this, R.color.drivingColorPolygonBorder)
        poly.fillColor = ContextCompat.getColor(this, R.color.drivingColorPolygonFill)

        //Set bounds based on field size
        var builder = LatLngBounds.builder()
        for (i in 0 until LocalBackend.allFields[fieldID!!].shapeCoordinates.size)
            builder.include(LocalBackend.allFields[fieldID!!].shapeCoordinates[i])
        val polyBounds = builder.build()
        val padding : Int = 100
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(polyBounds, padding)

        //App will crash without this.
        mMap.setOnMapLoadedCallback {
            mMap.moveCamera(cameraUpdate)
        }





        //Move camera to center of field
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocalBackend.allFields[fieldID!!].centerPoint, zoom))
    }

    fun setupUI() {

        //Get field
        val extraDataFromIntent= intent.extras

        if(extraDataFromIntent != null) {
            activityText = extraDataFromIntent.getString(getString(R.string.intent_extra_field_activity)).toString()
            fieldWorkTextView!!.text = activityText
            fieldID = extraDataFromIntent.getInt(getString(R.string.intent_extra_field_id))
        }

        //Update UI with field info
        fieldNameTextView!!.text = LocalBackend.allFields[fieldID!!].name
        var imageUrl : String = LocalBackend.allFields[fieldID!!].activeCropImageUrl
        fieldPictureImageView!!.setImageResource(R.drawable.stock_crop_image)


        if (!imageUrl.equals("null")){
            try {
                Picasso.get().load(imageUrl).into(fieldPictureImageView)
            } catch (e : IllegalArgumentException) {
                Log.d("", e.toString())
            }
        }

        //Set suggested speed (Currently just a preset value. Should be changed to be variable in a later version)
        suggestedSpeedNumber = 11
        suggestedSpeedNumberTextView!!.text = "$suggestedSpeedNumber ${getString(R.string.kilometerhour)}"

        //Update tracker part of view
        yourTractorImageView!!.setColorFilter(Color.GREEN)
        suggestedTractorImageView!!.setColorFilter(Color.GREEN)

    }

    private fun updateTractors (currentKmh : Double) {
        var smallVariationAmount : Int = 5
        var largeVariationAmount : Int = 10
        if (suggestedSpeedNumber - currentKmh < smallVariationAmount && currentKmh - suggestedSpeedNumber < smallVariationAmount)
            yourTractorImageView!!.setColorFilter(Color.GREEN)
        else if (suggestedSpeedNumber - currentKmh < largeVariationAmount && currentKmh - suggestedSpeedNumber < largeVariationAmount)
            yourTractorImageView!!.setColorFilter(Color.YELLOW)
        else
            yourTractorImageView!!.setColorFilter(Color.RED)
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
    }

    fun addCustomMarker(location : LatLng) {
        var markerOverlay = bitmapDescriptorFromVector(context as DrivingActivity, R.drawable.ic_stop_orange_24dp)?.let {
            GroundOverlayOptions()
                .image(it)
                .clickable(true)
                .position(location, 5f, 5f)
        }
        mMap.addGroundOverlay(markerOverlay)
    }

    fun finishSession () {
        //Save progress
        //Go to workspace view with info about the session

        val alertDialog: AlertDialog? = this?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Finish "+ (activityText?.toLowerCase() ?: "activity") +"?")
                setPositiveButton("Finish",
                    DialogInterface.OnClickListener { dialog, id ->
                        //Currently just goes back
                        super.onBackPressed() //Remove this later
                    })
                setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            }
            // Create the AlertDialog
            builder.create()
        }
        alertDialog!!.show()


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
            pauseFAB!!.text = getString(R.string.driving_pause)
        } else { //Stopped
            pauseFAB!!.setIconResource(R.drawable.ic_play_circle_outline_black_24dp)
            pauseFAB!!.text = getString(R.string.driving_start)
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

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    fun sendMessageToUser(message: String) {
        val mySnackbar = Snackbar.make(findViewById(R.id.activityDriving), message, Snackbar.LENGTH_LONG)
        mySnackbar.show()
    }
}
