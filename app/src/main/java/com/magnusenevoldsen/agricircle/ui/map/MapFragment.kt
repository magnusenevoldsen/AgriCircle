package com.magnusenevoldsen.agricircle.ui.map

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.magnusenevoldsen.agricircle.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.magnusenevoldsen.agricircle.AgriCircleBackend
import com.magnusenevoldsen.agricircle.LocalBackend
import com.magnusenevoldsen.agricircle.model.Field
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_map.*
import java.security.Permission

class MapFragment : Fragment(), OnMapReadyCallback{




    private lateinit var mMap : GoogleMap
    private var root : View? = null
    private var constLayout : View? = null
    private var creatingLayout : View? = null

    //Location
    private var fusedLocationClient : FusedLocationProviderClient? = null
    private val MY_PERMISSION_FINE_LOCATION = 101
    private var locationRequest : LocationRequest? = null
    private var updatesOn = false
    private var locationCallback : LocationCallback? = null
    private val zoom : Float = 17.0f
    private var counter : Int = 0
    private var presetInterval : Long = 2000
    private var presetFastestInterval : Long = 1000


    //Shared prefs
    var myPref : SharedPreferences? = null

    //Test purposes
    private var constToggle : Boolean = false           //Not used??????

    //Standard Action Buttons
//    private var positionFAB : FloatingActionButton? = null
//    private var fieldFAB : FloatingActionButton? = null
    private var fabSpeedDial : SpeedDialView? = null


    //Add fields
    private var editingFields : Boolean = false
    private var doneEditingFields : Boolean = true
    private var newFieldLocations : ArrayList<LatLng> = ArrayList()
    private var groundOverlayArray : ArrayList<GroundOverlay> = ArrayList()
    private var polylineArray : ArrayList<Polyline> = ArrayList()
    private var mapCrosshair : ImageView? = null
    private var addPointFloatingActionButton : ExtendedFloatingActionButton? = null
    private var finishFloatingActionButton : ExtendedFloatingActionButton? = null

    //Top Layout Images
    private var topTopImageView : ImageView? = null
    private var topBottomImageView : ImageView? = null

    //Selected field
    private var currentlySelectedField : Int? = null







    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_map, container, false)
        locationRequest = LocationRequest()
        locationRequest!!.interval = presetInterval // Find ud af hvor ofte der bør opdateres. pt 1 sek for test formål
        locationRequest!!.fastestInterval = presetFastestInterval //1 sec
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //Overvej at bruge HIGH ACCURACY istedet. / BALANCED

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Add new fields settings
        mapCrosshair = root!!.findViewById(R.id.mapCrosshair)
        mapCrosshair!!.setColorFilter(R.color.colorOrange)
        toggleCrosshair(false)
        addPointFloatingActionButton = root!!.findViewById(R.id.addPointFloatingActionButton)
        finishFloatingActionButton = root!!.findViewById(R.id.finishPointFloatingActionButton)
        addPointFloatingActionButton!!.setOnClickListener {
            addPointButtonClicked()
        }
        finishFloatingActionButton!!.setOnClickListener {
            finishedAddingPointsButtonClicked()
            toggleCreatingFieldTopView(false)
        }


        //Top layout
        constLayout = root!!.findViewById<ConstraintLayout>(R.id.mapConstraintLayout)
        toggleTopView(false)

        //Creating top layout
        creatingLayout = root!!.findViewById<ConstraintLayout>(R.id.creatingFieldConstraintLayout)
        toggleCreatingFieldTopView(false)

        topTopImageView = root!!.findViewById(R.id.drivingIconImageView)
        topBottomImageView = root!!.findViewById(R.id.actionTwoImageView)



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


        fabSpeedDial = root!!.findViewById<SpeedDialView>(R.id.fabSpeedDial)

        //Find field
        fabSpeedDial!!.addActionItem(SpeedDialActionItem.Builder(R.id.findFieldFloatingActionButton, R.drawable.ic_field_dashboard_white_24dp)
            .setFabBackgroundColor(resources.getColor(R.color.colorOrange))
            .setFabImageTintColor(resources.getColor(R.color.colorWhite))
            .setLabel(getString(R.string.menu_item_find_field))
            .setLabelColor(Color.GRAY)
            .setLabelBackgroundColor(resources.getColor(R.color.colorWhite))
            .setLabelClickable(false)
            .create())

        //Create field
        fabSpeedDial!!.addActionItem(SpeedDialActionItem.Builder(R.id.createFieldFloatingActionButton, R.drawable.ic_add_black_24dp)
            .setFabBackgroundColor(resources.getColor(R.color.colorOrange))
            .setFabImageTintColor(resources.getColor(R.color.colorWhite))
            .setLabel(getString(R.string.menu_item_create_field))
            .setLabelColor(Color.GRAY)
            .setLabelBackgroundColor(resources.getColor(R.color.colorWhite))
            .setLabelClickable(false)
            .create())

        //Go to position
        fabSpeedDial!!.addActionItem(SpeedDialActionItem.Builder(R.id.positionFloatingActionButton, R.drawable.ic_gps_fixed_white_24dp)
            .setFabBackgroundColor(resources.getColor(R.color.colorOrange))
            .setFabImageTintColor(resources.getColor(R.color.colorWhite))
            .setLabel(getString(R.string.menu_item_position))
            .setLabelColor(Color.GRAY)
            .setLabelBackgroundColor(resources.getColor(R.color.colorWhite))
            .setLabelClickable(false)
            .create())






        fabSpeedDial!!.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.positionFloatingActionButton -> {
                    goToCurrentPosition()
                    fabSpeedDial!!.close() // To close the Speed Dial with animation
                    return@OnActionSelectedListener true // false will close it without animation
                }
                R.id.createFieldFloatingActionButton -> {
                    newFieldButton()
                    fabSpeedDial!!.close() // To close the Speed Dial with animation
                    return@OnActionSelectedListener true // false will close it without animation
                }
                R.id.findFieldFloatingActionButton -> {
                    findFieldButton()
//                    fabSpeedDial.close() // To close the Speed Dial with animation
                    return@OnActionSelectedListener true // false will close it without animation
                }
            }
            false
        })








        //Location -> Hent den 1 gang når view åbner
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(root!!.context) // ????????????
        if (ActivityCompat.checkSelfPermission(root!!.context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient!!.lastLocation.addOnSuccessListener {location ->
                if (location != null) {
                    //Update UI             --- Go to field 0 instead????
//                    val currentLocation = LatLng(location.latitude, location.longitude)
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoom))   //Brug animateCamera eller moveCamera
//                    mMap.addMarker(MarkerOptions().position(currentLocation).title("You are here"))
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
//                        mMap.addMarker(MarkerOptions().position(currentLocation).title("You are here"))
                        updatesOn = false
                        stopLocationUpdates()


//                        sendMessageToUser(root!!, ""+counter+" : Lat = "+currentLocation.latitude+", Lng = "+currentLocation.longitude)
//                        println("Lat = "+currentLocation.latitude+", Lng = "+currentLocation.longitude+"")
//                        counter++
                    }
                }
            }
        }







        return root
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            MY_PERMISSION_FINE_LOCATION -> {
//
//                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                }
//
//            }
//        }
//    }﻿

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_FINE_LOCATION ->{
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    mMap.isMyLocationEnabled = true
                }
                else{
                    sendMessageToUser(root!!, getString(R.string.user_declined_location_permission))
                }
                return
            }
        }
    }


    private fun newFieldButton() {
        drawNewField()
        toggleCreatingFieldTopView(true)
    }

    private fun goToCurrentPosition() {
        updatesOn = true
        startLocationUpdates()
    }

    private fun findFieldButton() {
        toggleCrosshair(false)
        currentlySelectedField = counter

        toggleTopView(true)

        val field0 = LocalBackend.allFields[counter].centerPoint

        fieldNameTextView.text = LocalBackend.allFields[counter].name
        fieldSizeTextView.text = LocalBackend.allFields[counter].surface.toString() + getString(R.string.map_hectare)
        topTopImageView!!.setImageResource(R.drawable.stock_crop_image)
        topBottomImageView!!.setImageResource(R.drawable.stock_crop_image)

        val imageUrl = LocalBackend.allFields[counter].activeCropImageUrl

        if (!imageUrl.equals("null")){
            try {
                Picasso.get().load(imageUrl).into(topTopImageView)
                Picasso.get().load(imageUrl).into(topBottomImageView)
            } catch (e : IllegalArgumentException) {
                Log.d("", e.toString())
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(field0, zoom))
        var countMax = LocalBackend.allFields.size - 1 //7 fields -> 0..6
        if (counter < countMax)
            counter ++
        else counter = 0
    }


    private fun makeFieldList () {
        //Takes the fields which are from the first company of the user
//        println("Making a new fields list")
        for (i in 0 until AgriCircleBackend.fields.size)
            LocalBackend.allFields.add(AgriCircleBackend.fields[i])

        for (i in 0 until LocalBackend.localFields.size)
            LocalBackend.allFields.add(LocalBackend.localFields[i])

//        println("Fields : ")
//        for (i in 0 until LocalBackend.allFields.size)
//            println("Field $i : ${LocalBackend.allFields[i]}")
    }




    private fun drawFields () {
        for (i in 0 until LocalBackend.allFields.size) {
//            println("Field : "+LocalBackend.allFields[i].id)
//            println("Field : "+LocalBackend.allFields[i])
//            println("Field latlng : "+LocalBackend.allFields[i].shapeCoordinates.toString())
            var poly : Polygon = mMap.addPolygon(
                PolygonOptions()
                    .clickable(true)
                    .addAll(LocalBackend.allFields[i].shapeCoordinates)
            )

            poly.tag = LocalBackend.allFields[i].id
            poly.strokeColor = ContextCompat.getColor(activity!!, R.color.colorPolygonBorder)
            poly.fillColor = ContextCompat.getColor(activity!!, R.color.colorPolygonFill)
        }
    }



    private fun makePolygonClickListeners (array : ArrayList<Field>) {
        mMap.setOnPolygonClickListener { polygon ->

            fabSpeedDial!!.close()

            if (doneEditingFields)
                toggleTopView(true)

            //Update UI to field ->
            val fieldId = polygon.tag.toString().toInt()
            var fieldNumber : Int = -1
            for (i in 0 until array.size)
                if (array[i].id == fieldId)
                    fieldNumber = i

            currentlySelectedField = fieldNumber
            fieldNameTextView.text = array[fieldNumber].name
            fieldSizeTextView.text = array[fieldNumber].surface.toString()+" ha"

            var imageUrl : String = array[fieldNumber].activeCropImageUrl

            topTopImageView!!.setImageResource(R.drawable.stock_crop_image)
            topBottomImageView!!.setImageResource(R.drawable.stock_crop_image)
            if (!imageUrl.equals("null")){
                try {
                    Picasso.get().load(imageUrl).into(topTopImageView)
                    Picasso.get().load(imageUrl).into(topBottomImageView)
                } catch (e : IllegalArgumentException) {
                    Log.d("", e.toString())
                }
            }

//            println("You clicked on field:")
//            println(array[fieldNumber].toString())

        }
    }

    private fun goToCompanyLocation () {
        val companyLocation = AgriCircleBackend.companies[0].locationCoordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(companyLocation, zoom))
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(root!!.context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    override fun onResume() {
        super.onResume()

//        for (i in 0 until 10)
//            println("VI PRINTER HER $i")
//
//        for (i in 0 until LocalBackend.allFields.size)
//            println("VI PRINTER HER " + LocalBackend.allFields[i].toString())
//
//        println("SIZE = "+LocalBackend.allFields.size)
//        for (i in 0 until 10)
//            println("VI PRINTER HER $i")

        toggleActionButtons(true)
        toggleCrosshair(false)
        toggleTopView(false)
//        if (updatesOn)
//            startLocationUpdates()
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
//        mMap.isMyLocationEnabled = true //This crashes the app





        toggleActionButtons(true)

        goToCompanyLocation()           //Move map to company location
//        makeFieldList()                 //Sort field list to only include from one company
//        drawFields()                    //Draw fields on the map
//        drawLocalFields()               //Draw locally stored fields on the map
//        makePolygonClickListeners(AgriCircleBackend.fields)     //Add click listeners to the fields

        redrawFields()

        mMap.setOnMapClickListener {
            fabSpeedDial!!.close()
            toggleTopView(false)
        }


//        Current location
//        Lat = 55.5756983, Lng = 11.5702983




        //Delete later ---------
//        val campusLyngby = LatLng(55.785558, 12.521564)
//        mMap.addMarker(MarkerOptions().position(campusLyngby).title("Campus Lyngby"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusLyngby, zoom))
    }


    fun sendMessageToUser(view : View,  message: String) {
        val mySnackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        mySnackbar.show()
    }

    private fun startActivityOne() {
        //Needs data with the intent start : what activity etc
        val intent = Intent (activity, DrivingActivity::class.java)
        intent.putExtra(getString(R.string.intent_extra_field_id), currentlySelectedField)
        intent.putExtra(getString(R.string.intent_extra_field_activity), getString(R.string.placeholder_sowing))
        activity!!.startActivity(intent)
    }

    private fun startActivityTwo() {
        //Needs data with the intent start : what activity etc
        val intent = Intent (activity, DrivingActivity::class.java)
        intent.putExtra(getString(R.string.intent_extra_field_id), currentlySelectedField)
        intent.putExtra(getString(R.string.intent_extra_field_activity), getString(R.string.placeholder_Fertilization))
        activity!!.startActivity(intent)
    }

    private fun toggleCrosshair (toggle : Boolean) {
        if (toggle) mapCrosshair!!.visibility = View.VISIBLE
        else mapCrosshair!!.visibility = View.GONE
    }

    private fun toggleTopView (toggle : Boolean) {
        if (toggle) constLayout!!.visibility = View.VISIBLE
        else constLayout!!.visibility = View.GONE
    }

    private fun toggleCreatingFieldTopView (toggle : Boolean) {
        if (toggle) creatingLayout!!.visibility = View.VISIBLE
        else creatingLayout!!.visibility = View.GONE
    }


    private fun toggleActionButtons (toggle : Boolean) {
        //Show standard buttons and hide new field buttons
        if (toggle) {
            fabSpeedDial!!.visibility = View.VISIBLE

            addPointFloatingActionButton!!.visibility = View.GONE
            finishFloatingActionButton!!.visibility = View.GONE


        }
        //Show new field buttons and hide standard buttons
        else {
            fabSpeedDial!!.visibility = View.GONE

            addPointFloatingActionButton!!.visibility = View.VISIBLE
            finishFloatingActionButton!!.visibility = View.VISIBLE
        }
    }

    private fun drawNewField() {

        //Draw fields
        newFieldLocations.clear()

        //Toggle done editing
        doneEditingFields = false

        //Show crosshair
        toggleCrosshair(true)

        //Hide top layout
        toggleTopView(false)

        //Toggle Floating Action Buttons
        toggleActionButtons(false)

        //Toggle editing
        editingFields = true
    }

    fun addPointButtonClicked() {


//        println("!!!!!!!!!!------- SE MIG -----!!!!!!!!!!!!!!!")

        var location : LatLng = mMap.cameraPosition.target


//        println("New location was found $location")

        //Add point (little square)



        //Test with new array ->

//
        var markerOverlay = bitmapDescriptorFromVector(root!!.context, R.drawable.ic_stop_blue_24dp)?.let {
            GroundOverlayOptions()
                .image(it)
                .clickable(true)
                .position(location, 10f, 10f)
        }
        var overlay : GroundOverlay = mMap.addGroundOverlay(markerOverlay)
        groundOverlayArray.add(overlay)


        //Push point to array
        newFieldLocations.add(location)
//        println("New location was pushed to the array $newFieldLocations")

        //Get nr. of tracks
        var amountOfTracks = newFieldLocations.size


//        println("Amount of tracks -> $amountOfTracks")

        // calc dist
        if (amountOfTracks >= 2) {
            var firstLocation = Location(LocationManager.GPS_PROVIDER)
            firstLocation.latitude = newFieldLocations[0].latitude
            firstLocation.longitude = newFieldLocations[0].longitude

            var newestLocation = Location(LocationManager.GPS_PROVIDER)
            newestLocation.latitude = newFieldLocations[amountOfTracks-1].latitude
            newestLocation.longitude = newFieldLocations[amountOfTracks-1].longitude

            if (firstLocation.distanceTo(newestLocation) < 5.0) {
                doneEditingFields = true

//                println("-> -> -> You are now done editing")
            }
        }

        //Push first LatLng to the array to make sure its all connected
        if (doneEditingFields) {
            newFieldLocations.removeAt(newFieldLocations.size - 1)
            newFieldLocations.add(newFieldLocations[0])
            enterFieldInfoDialog()
//            println("Array was fixed to make sure its connected -> $newFieldLocations")

        }

        //Draw the track
        if (amountOfTracks >= 2) {

            var poly : Polyline = mMap.addPolyline(
                PolylineOptions()
                    .clickable(false)
                    .add(newFieldLocations[amountOfTracks-2],
                        newFieldLocations[amountOfTracks-1])
            )
            poly.color = ContextCompat.getColor(this.context!!, R.color.colorPolygonDriving)
            polylineArray.add(poly)

        }

    }

    fun redrawFields() {

        LocalBackend.allFields.clear()          //Clear all fields
        makeFieldList()                         //Make a new list
        mMap.clear()                            //Clear the map
        drawFields()                            //Draw fields on map
        makePolygonClickListeners(LocalBackend.allFields)    //Set click listeners



    }


    fun finishedAddingPointsButtonClicked() {
        for (i in 0 until groundOverlayArray.size)
            groundOverlayArray[i].remove()

        for (i in 0 until polylineArray.size)
            polylineArray[i].remove()

        groundOverlayArray.clear()
        polylineArray.clear()
        doneEditingFields = true


        toggleActionButtons(true)
        toggleCrosshair(false)
        toggleTopView(false)
        toggleCreatingFieldTopView(false)
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }


    private fun enterFieldInfoDialog () {
        var builder = AlertDialog.Builder(root!!.context)
        builder.setTitle(getString(R.string.map_dialog_title))

        var layout = LinearLayout(root!!.context)
        layout.orientation = LinearLayout.VERTICAL


        var fieldNameInput = EditText(root!!.context)
        fieldNameInput.inputType = InputType.TYPE_CLASS_TEXT
        fieldNameInput.hint = getString(R.string.map_dialog_hint_name)
        layout.addView(fieldNameInput)

        var fieldIdInput = EditText(root!!.context)
        fieldIdInput.inputType = InputType.TYPE_CLASS_NUMBER
        fieldIdInput.hint = getString(R.string.map_dialog_hint_id)


        layout.addView(fieldIdInput)

        builder.setView(layout)

//        println("LOCATIONS 1 : "+newFieldLocations)

        builder.setPositiveButton(android.R.string.yes) {dialog, which ->
            //Upload
            var fieldName = fieldNameInput.text.toString()
            if (fieldName.equals(""))
                fieldName = getString(R.string.map_dialog_field_name_preset)

            var fieldId = (100000..999999).random() //Returns a random number between 100000 and 999999
            try {
                var fieldId = fieldIdInput.text.toString().toInt()
            } catch (e : NumberFormatException) {
                println("ERROR : No input in field id -> $e")
            }



            LocalBackend.prepareFieldForLocalUpload(
                arrayOfLatLng = newFieldLocations,
                fieldName = fieldName,
                fieldId = fieldId
            )

//            println("LOCATIONS 2 : "+newFieldLocations)
            redrawFields()
//            println("LOCATIONS 3 : "+newFieldLocations)

            finishedAddingPointsButtonClicked()
//            println("LOCATIONS 4 : "+newFieldLocations)
        }

        builder.setNegativeButton(android.R.string.no) {dialog, which ->
            finishedAddingPointsButtonClicked()
        }

        builder.setOnCancelListener {
            finishedAddingPointsButtonClicked()
        }

        builder.show()
    }







}





























