package com.magnusenevoldsen.agricircle.ui.map

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.magnusenevoldsen.agricircle.AgriCircleBackend
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
    private var polyList : ArrayList<Polygon> = ArrayList()

    //Shared prefs
    var myPref : SharedPreferences? = null

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

        myPref = activity?.getPreferences(Context.MODE_PRIVATE)


        //Top layout
        constLayout = root!!.findViewById<ConstraintLayout>(R.id.mapConstraintLayout)
        constLayout!!.visibility = View.GONE //Hide view initially
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

        val positionFAB : FloatingActionButton = root!!.findViewById(R.id.positionFloatingActionButton)
        positionFAB.setColorFilter(Color.WHITE)
        positionFAB.setOnClickListener {
//            updatesOn = !updatesOn
//            mMap.isMyLocationEnabled = updatesOn







            val field0 = AgriCircleBackend.fields[counter].shapeCoordinates[0]
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(field0, zoom))

            sendMessageToUser(root!!, "Counter : $counter - Field : ${AgriCircleBackend.fields[counter].name}")

            var countMax = newFields.size - 1 //7 fields -> 0..6
            if (counter < countMax)
                counter ++
            else counter = 0


        }

        val fieldFAB : FloatingActionButton = root!!.findViewById(R.id.fieldFloatingActionButton)
        fieldFAB.setColorFilter(Color.WHITE)
        fieldFAB.setOnClickListener {
//            sendMessageToUser(root!!, "Not implemented")

            //Test purposes


            mMap.clear() //Ikke korrekt funktion
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

    fun makeFieldList () {
        //Takes the fields which are from the first company of the user
        println("Making a new fields list")
        for (i in 0 until AgriCircleBackend.fields.size)
            if (AgriCircleBackend.fields[i].companyId == AgriCircleBackend.companies[0].id)
                newFields.add(AgriCircleBackend.fields[i])
        println("Fields : ")
        for (i in 0 until newFields.size)
            println("Field $i : ${newFields[i]}")
    }

    fun drawFields () {
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

    fun makePolygonClickListeners () {
        mMap.setOnPolygonClickListener { polygon ->
            //            if (constToggle) constLayout!!.visibility = View.GONE
//            else
            constLayout!!.visibility = View.VISIBLE
            constToggle = !constToggle

            //Update UI to field ->
            val fieldId = polygon.tag.toString().toInt()
            var fieldNumber : Int = -1
            for (i in 0 until newFields.size)
                if (newFields[i].id == fieldId)
                    fieldNumber = i

            AgriCircleBackend.selectedField = fieldNumber
            fieldNameTextView.text = newFields[fieldNumber].name

        }
    }

    fun goToCompanyLocation () {
        val companyLocation = AgriCircleBackend.companies[0].locationCoordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(companyLocation, zoom))
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
