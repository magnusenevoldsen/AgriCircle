package com.magnusenevoldsen.agricircle

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_LABELED
import com.google.android.material.snackbar.Snackbar



class MainActivity : AppCompatActivity() {


    //Shared prefs
    var myPref : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.labelVisibilityMode = LABEL_VISIBILITY_LABELED //Show labels on all items in navigation view
//        supportActionBar?.hide() //Hide the action bar
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map, R.id.navigation_sampling, R.id.navigation_workspace, R.id.navigation_settings
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        //Set up initial shared prefs
//        myPref = this.getPreferences(Context.MODE_PRIVATE) ?: return

        //Map Preference -> Hybrid, Terrain, Satellite

//        myPref!!.edit().remove(getString(R.string.settings_map_type_preference)).apply()


//        val mapType = myPref!!.getString(getString(R.string.settings_map_type_preference), getString(R.string.map_type_none))
//        if (mapType.equals(getString(R.string.map_type_none))) {
//            val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
//            with (sharedPref.edit()) {
//                putString(getString(R.string.settings_map_type_preference), getString(R.string.map_type_hybrid))
//                commit()
//            }
//        }
//        else
//            sendMessageToUser("We got one!")





//        //Load user from DB
//        sendMessageToUser("Loading user : "+AgriCircleBackend.loadUser())
//
//        //Load companies from DB
//        sendMessageToUser("Loading companies : "+AgriCircleBackend.loadCompanies())
//
//        //Load fields from DB
//        sendMessageToUser("Loading fields : "+AgriCircleBackend.loadFields())
//
//        //Test all is working and print it to terminal
//        println("-----------------------------------"+
//                "\nUser : \n ${AgriCircleBackend.user.toString()} "+
//                "\nCompanies : \n ${AgriCircleBackend.companies.toString()} "+
//                "\nFields : \n ${AgriCircleBackend.fields.toString()} "+
//                "\n-----------------------------------")
//
//
//        println("Companies")
//        for (i in 0 until AgriCircleBackend.companies.size)
//            println(AgriCircleBackend.companies[i].toString())
//
//        println("Fields")
//        for (i in 0 until AgriCircleBackend.fields.size)
//            println(AgriCircleBackend.fields[i].toString())




    }


    fun sendMessageToUser(message: String) {
        val mySnackbar = Snackbar.make(findViewById(R.id.mainActivity), message, Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }

}
