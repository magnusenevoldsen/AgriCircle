package com.magnusenevoldsen.agricircle.ui.map

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.magnusenevoldsen.agricircle.R

class drivingActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap : GoogleMap
    private val zoom : Float = 18.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driving)




        //Layout setup

        //Map setup
        val mapFragment = supportFragmentManager.findFragmentById(R.id.drivingMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Field info
        val fieldPictureImageView : ImageView = findViewById(R.id.drivingIconImageView)
        val fieldNameTextView : TextView = findViewById(R.id.drivingFieldTextView)
        val fieldWorkTextView : TextView = findViewById(R.id.drivingWorkTextView)

        //Field progress
        val timeTextView : TextView = findViewById(R.id.drivingTimeTextView)
        val yourSpeedTextView : TextView = findViewById(R.id.drivingCurrentSpeedTextView)
        val yourSpeedNumberTextView : TextView = findViewById(R.id.drivingNumberCurrentSpeedTextView)
        val suggestedSpeedTextView : TextView = findViewById(R.id.drivingSuggestedSpeedTextView)
        val suggestedSpeedNumberTextView : TextView = findViewById(R.id.drivingNumberSuggestedSpeedTextView)

        //Tractors
        val yourTractorImageView : ImageView = findViewById(R.id.drivingCurrentTractorImageView)
        val suggestedTractorImageView : ImageView = findViewById(R.id.drivingSuggestedTractorImageView)

        //Change Tractor Color
        val colorOrange = ResourcesCompat.getColor(resources, R.color.colorOrange, null)
        yourTractorImageView.setColorFilter(colorOrange)
        val colorGreen = ResourcesCompat.getColor(resources, R.color.colorGreen, null)
        suggestedTractorImageView.setColorFilter(colorGreen)

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







    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val campusLyngby = LatLng(55.785558, 12.521564)
        mMap.addMarker(MarkerOptions().position(campusLyngby).title("Campus Lyngby"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusLyngby, zoom))
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




}
