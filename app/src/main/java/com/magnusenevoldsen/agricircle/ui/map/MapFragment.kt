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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.magnusenevoldsen.agricircle.AgriCircleBackend
import com.magnusenevoldsen.agricircle.LocalBackend
import com.magnusenevoldsen.agricircle.model.Field
import kotlinx.android.synthetic.main.fragment_map.*

class MapFragment : Fragment(), OnMapReadyCallback{




    private lateinit var mapViewModel: MapViewModel
    private lateinit var mMap : GoogleMap
    private var root : View? = null
    private var constLayout : View? = null

    //Location
    private var fusedLocationClient : FusedLocationProviderClient? = null
    private val MY_PERMISSION_FINE_LOCATION = 101
    private var locationRequest : LocationRequest? = null
    private var updatesOn = false
    private var locationCallback : LocationCallback? = null
    private val zoom : Float = 17.0f
    private var counter : Int = 0

    //Polygons
    var newFields : ArrayList<Field> = ArrayList()

    //Shared prefs
    var myPref : SharedPreferences? = null

    //Test purposes
    private var constToggle : Boolean = false           //Not used??????

    //Standard Action Buttons
    private var positionFAB : FloatingActionButton? = null
    private var fieldFAB : FloatingActionButton? = null


    //Add fields
    private var editingFields : Boolean = false
    private var doneEditingFields : Boolean = false
    private var newFieldLocations : ArrayList<LatLng> = ArrayList()
    private var groundOverlayArray : ArrayList<GroundOverlay> = ArrayList()
    private var polylineArray : ArrayList<Polyline> = ArrayList()
    private var mapCrosshair : ImageView? = null
    private var addPointFloatingActionButton : ExtendedFloatingActionButton? = null
    private var finishFloatingActionButton : ExtendedFloatingActionButton? = null






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
            toggleActionButtons(true)
            toggleCrosshair(false)
            toggleTopView(false)
        }


        //Top layout
        constLayout = root!!.findViewById<ConstraintLayout>(R.id.mapConstraintLayout)
        toggleTopView(false)

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


        var counter : Int = 0

        //Buttons

        positionFAB = root!!.findViewById(R.id.positionFloatingActionButton)
        positionFAB!!.setColorFilter(Color.WHITE)
        positionFAB!!.setOnClickListener {
            drawNewField()
        }

        fieldFAB = root!!.findViewById(R.id.fieldFloatingActionButton)
        fieldFAB!!.setColorFilter(Color.WHITE)
        fieldFAB!!.setOnClickListener {

            toggleCrosshair(false)

            val field0 = AgriCircleBackend.fields[counter].centerPoint
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(field0, zoom))

            var countMax = newFields.size - 1 //7 fields -> 0..6
            if (counter < countMax)
                counter ++
            else counter = 0

        }



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
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoom))   //Brug animateCamera eller moveCamera
                        mMap.addMarker(MarkerOptions().position(currentLocation).title("You are here"))
                        sendMessageToUser(root!!, ""+counter+" : Lat = "+currentLocation.latitude+", Lng = "+currentLocation.longitude)
                        println("Lat = "+currentLocation.latitude+", Lng = "+currentLocation.longitude+"")
                        counter++
                    }
                }
            }
        }







        return root
    }


    private fun makeFieldList () {
        //Takes the fields which are from the first company of the user
        println("Making a new fields list")
        for (i in 0 until AgriCircleBackend.fields.size)
//            if (AgriCircleBackend.fields[i].companyId == AgriCircleBackend.companies[0].id)
                newFields.add(AgriCircleBackend.fields[i])
        println("Fields : ")
        for (i in 0 until newFields.size)
            println("Field $i : ${newFields[i]}")
    }

    private fun drawFields () {
        for (i in 0 until newFields.size) {
            var poly : Polygon = mMap.addPolygon(
                PolygonOptions()
                    .clickable(true)
                    .addAll(AgriCircleBackend.fields[i].shapeCoordinates)
            )

            poly.tag = AgriCircleBackend.fields[i].id
            poly.strokeColor = ContextCompat.getColor(activity!!, R.color.colorPolygonBorder)
            poly.fillColor = ContextCompat.getColor(activity!!, R.color.colorPolygonFill)
        }
    }

    private fun makePolygonClickListeners () {
        mMap.setOnPolygonClickListener { polygon ->
            //            if (constToggle) constLayout!!.visibility = View.GONE
//            else
            toggleTopView(true)
            constToggle = !constToggle

            //Update UI to field ->
            val fieldId = polygon.tag.toString().toInt()
            var fieldNumber : Int = -1
            for (i in 0 until newFields.size)
                if (newFields[i].id == fieldId)
                    fieldNumber = i

            AgriCircleBackend.selectedField = fieldNumber
            fieldNameTextView.text = newFields[fieldNumber].name
            fieldSizeTextView.text = newFields[fieldNumber].surface.toString()+" ha"

            println("You clicked on field:")
            println(newFields[fieldNumber].toString())

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


        toggleActionButtons(true)

        goToCompanyLocation()           //Move map to company location
        makeFieldList()                 //Sort field list to only include from one company
        drawFields()                    //Draw fields on the map
        makePolygonClickListeners()     //Add click listeners to the fields



//        Current location
//        Lat = 55.5756983, Lng = 11.5702983




        //Delete later ---------
//        val campusLyngby = LatLng(55.785558, 12.521564)
//        mMap.addMarker(MarkerOptions().position(campusLyngby).title("Campus Lyngby"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusLyngby, zoom))
    }


    fun sendMessageToUser(view : View,  message: String) {
        val mySnackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }

    private fun startActivityOne() {
        //Needs data with the intent start : what activity etc
        val intent = Intent (activity, DrivingActivity::class.java)
        activity!!.startActivity(intent)
    }

    private fun startActivityTwo() {
        //Needs data with the intent start : what activity etc
        val intent = Intent (activity, DrivingActivity::class.java)
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

    private fun toggleActionButtons (toggle : Boolean) {
        //Show standard buttons and hide new field buttons
        if (toggle) {
            fieldFAB!!.visibility = View.VISIBLE
            positionFAB!!.visibility = View.VISIBLE

            addPointFloatingActionButton!!.visibility = View.GONE
            finishFloatingActionButton!!.visibility = View.GONE


        }
        //Show new field buttons and hide standard buttons
        else {
            fieldFAB!!.visibility = View.GONE
            positionFAB!!.visibility = View.GONE

            addPointFloatingActionButton!!.visibility = View.VISIBLE
            finishFloatingActionButton!!.visibility = View.VISIBLE
        }
    }

    private fun drawNewField() {

        //Draw fields

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

        var location : LatLng = mMap.cameraPosition.target

        //Add point (little square)

//
        var markerOverlay = bitmapDescriptorFromVector(root!!.context, R.drawable.ic_stop_red_24dp)?.let {
            GroundOverlayOptions()
                .image(it)
                .clickable(true)
                .position(location, 10f, 10f)
        }
        var overlay : GroundOverlay = mMap.addGroundOverlay(markerOverlay)
        groundOverlayArray.add(overlay)


        //Push point to array
        newFieldLocations.add(location)

        //Get nr. of tracks
        var amountOfTracks = newFieldLocations.size

        // calc dist
        if (amountOfTracks >= 2) {
            var firstLocation = Location(LocationManager.GPS_PROVIDER)
            firstLocation.latitude = newFieldLocations[0].latitude
            firstLocation.longitude = newFieldLocations[0].longitude

            var newestLocation = Location(LocationManager.GPS_PROVIDER)
            newestLocation.latitude = newFieldLocations[amountOfTracks-1].latitude
            newestLocation.longitude = newFieldLocations[amountOfTracks-1].longitude

            if (firstLocation.distanceTo(newestLocation) < 3.0) {
                doneEditingFields = true
            }
        }

        //Push first LatLng to the array to make sure its all connected
        if (doneEditingFields) {
            newFieldLocations.removeAt(newFieldLocations.size - 1)
            newFieldLocations.add(newFieldLocations[0])
            enterFieldInfoDialog()
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

    fun goToUploadFieldView () {
        //Go to next screen

        //Go to new screen to type field info -> bring array

        //On new screen ->

        //Input stuff from logbog

        //Push to local sqlite array (implement this as well)
    }


    fun finishedAddingPointsButtonClicked() {
        for (i in 0 until groundOverlayArray.size)
            groundOverlayArray[i].remove()

        for (i in 0 until polylineArray.size)
            polylineArray[i].remove()

        newFieldLocations.clear()
        groundOverlayArray.clear()
        polylineArray.clear()
        doneEditingFields = false
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
        builder.setTitle("Field properties")

        var layout = LinearLayout(root!!.context)
        layout.orientation = LinearLayout.VERTICAL


        var fieldNameInput = EditText(root!!.context)
        fieldNameInput.inputType = InputType.TYPE_CLASS_TEXT
        fieldNameInput.hint = "Field name"
        layout.addView(fieldNameInput)

        var fieldIdInput = EditText(root!!.context)
        fieldIdInput.inputType = InputType.TYPE_CLASS_NUMBER
        fieldIdInput.hint = "Field ID"

        layout.addView(fieldIdInput)

        builder.setView(layout)

        builder.setPositiveButton(android.R.string.yes) {dialog, which ->
            //Upload
            LocalBackend.prepareFieldForLocalUpload(
                array = newFieldLocations,
                fieldName = fieldNameInput.text.toString(),
                fieldId = fieldIdInput.text.toString().toInt()
            )
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





























