package com.magnusenevoldsen.agricircle

import com.google.android.gms.maps.model.LatLng
import com.magnusenevoldsen.agricircle.model.Company
import com.magnusenevoldsen.agricircle.model.Field
import com.magnusenevoldsen.agricircle.model.User
import okhttp3.*
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException




object AgriCircleBackend {

    private val url : String = "https://graphql.agricircle.com/graphql"
    private var cookie : JSONArray ?= null
    private var cookieString : String ?=null
    private val successfulResponse : Int = 200
    var user : User? = null
    var companies : ArrayList<Company> = ArrayList()
    var fields : ArrayList<Field> = ArrayList()
    var selectedField : Int = 0
    var userWasLoadedCorrectly : Boolean = false
    var companyWasLoadedCorrectly : Boolean = false
    private val client = OkHttpClient()

    fun login (email : String, password : String) : Boolean {
        var succes = false
        var loginThread : Thread = Thread {

            //Request body string for login
            val requestBodyString =
                "[\n    {\n        \"operationName\": \"login\",\n        \"query\": \"mutation login(\$options: LoginInput!) {\\n  login(options: \$options) {\\n    token\\n    cookie\\n    __typename\\n  }\\n}\\n\",\n        \"variables\": {\n            \"options\": {\n                \"email\": \"$email\",\n                \"password\": \"$password\"\n            }\n        }\n    }\n]"

            //Create the request body
            val body = requestBodyString.toRequestBody("application/json".toMediaTypeOrNull())

            //Create the request
            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "*/*")
                .build()

            //Executing the request
            val response = client.newCall(request).execute()

            //Testing purposes
            println("Getting the response code, should be 200 for a successful login : ")
            val responseCode = response.code
            println("CODE : $responseCode")

            if (responseCode == successfulResponse) { //Hvis denne rammer er request gået igennem - login er enten rigtig eller forkert
                println("Getting the response : ")
                val bodyString = response.body!!.string()
                println("Response : $bodyString")

                println("Removing \"[]\" : ")
                var str = bodyString.substring(1, bodyString.length - 1) //Fjerner [] så det ligner array
                println("So it becomes : $str")

                println("Attempting to get the cookie")
                try {
                    cookie = JSONObject(str).getJSONObject("data").getJSONObject("login").getJSONArray("cookie")
                    println("We got a cookie : $cookie")

                    //Get the real cookie for further requests
                    for (i in 0 until cookie!!.length()) {
                        for (item in cookie!!.getString(i).split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                            if (item.startsWith("_AgriCircle_subdomain_session")) {
                                cookieString = item
                            }
                        }
                    }
                    userWasLoadedCorrectly = true
                    succes = true
                } catch (e: JSONException) { //If the jsonobject cant be found eg. bad login -> go here
                    println("We didn't get a cookie - login is wrong")
                    succes = false
                }
            }
            else {
                println("We didn't get a cookie - responce code is wrong")
                succes = false
            }
        }
        println("-----------------------------------")
        loginThread.start()
        loginThread.join() //Apparently used to wait on a thread - see if a better way can be found - I think this affects the main thread
        println("-----------------------------------")
        return succes
    }


    fun loadUser () : Boolean {
        var succes = false
        var loadUserThread : Thread = Thread {
            val client = OkHttpClient()

            //Request body string for user
            val requestBodyString = "{\n    \"operationName\": \"user\",\n    \"variables\": {},\n    \"query\": \"query user {\\n  user {\\n    name\\n    language\\n    avatarUrl\\n planningYears\\n    decimalSeparator\\n    dateFormat\\n    slug\\n    features\\n    avatarUrl\\n    __typename\\n  }\\n}\\n\"\n}"

            //Create the request body
            val body = requestBodyString.toRequestBody("application/json".toMediaTypeOrNull())

            //Create the request
            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "*/*").addHeader(
                    "x-cookie",
                    cookieString!!
                )
                .build()

            //Executing the request
            val response = client.newCall(request).execute()

            if (response.code == successfulResponse) { //Hvis denne rammer er request gået igennem - login er enten rigtig eller forkert
                println("Getting the response : ")
                val bodyString = response.body!!.string()
                println("Response : $bodyString")
                val bodyUserPath : JSONObject = JSONObject(bodyString).getJSONObject("data").getJSONObject("user")

                println("Attempting to get the user")
                try {
                    val nameFromJSON : String = bodyUserPath.getString("name")
                    val languageFromJSON : String = bodyUserPath.getString("language")
                    val avatarUrlFromJSON : String = bodyUserPath.getString("avatarUrl")
                    //Get JSONarray and make it an ArrayList
                    val planningYearsFromJSON : JSONArray = bodyUserPath.getJSONArray("planningYears")
                    var planningYearsArrayList : ArrayList<Int> = ArrayList()
                    for (i in 0 until planningYearsFromJSON.length()) { planningYearsArrayList.add(planningYearsFromJSON.getInt(i)) }
                    val decimalSeparatorFromJSON : String = bodyUserPath.getString("decimalSeparator")
                    val dateFormatFromJSON : String = bodyUserPath.getString("dateFormat")
                    val slugFromJSON : String = bodyUserPath.getString("slug")
                    //Get JSONarray and make it an ArrayList
                    val featuresFromJSON : JSONArray = bodyUserPath.getJSONArray("features")
                    var featuresArrayList : ArrayList<String> = ArrayList()
                    for (i in 0 until featuresFromJSON.length()) { featuresArrayList.add(featuresFromJSON.getString(i)) }
                    val typenameFromJSON : String = bodyUserPath.getString("__typename")

                    //Load user into user
                    user = User(name = nameFromJSON,
                        language = languageFromJSON,
                        avatarUrl = avatarUrlFromJSON,
                        planningYears = planningYearsArrayList,   //Int array
                        decimalSeparator = decimalSeparatorFromJSON,
                        dateFormat = dateFormatFromJSON,
                        slug = slugFromJSON,
                        features = featuresArrayList,        //String array
                        typename = typenameFromJSON
                        )
                    succes = true
                } catch (e: JSONException) { //If the jsonobject cant be found eg. bad login -> go here
                    println("We didn't get a user")
                    succes = false
                }
            }
            else {
                println("Error loading user")
            }
        }
        println("-----------------------------------")
        if (userWasLoadedCorrectly) {
            loadUserThread.start()
            loadUserThread.join() //Apparently used to wait on a thread - see if a better way can be found - I think this affects the main thread
        }
        println("-----------------------------------")
        return succes

    }


    fun loadFields () : Boolean {
        var succes = false

        var companyIdList : ArrayList<Int> = ArrayList()
        for (i in 0 until companies.size)
            companyIdList.add(companies[i].id)
        var year : Int = 2019

        for (i in 0 until companyIdList.size) {
            var loadUserThread : Thread = Thread {
                val client = OkHttpClient()

                val companyId : Int = companyIdList[i]

                //Request body string for fields
                val requestBodyString = "{\n    \"operationName\": \"fields\",\n    \"variables\": {\n    \t\"options\":\n    \t{\n        \t\"companyId\": $companyId,\n        \t\"year\": $year\n    \t}\n    },\n    \"query\": \"query fields(\$options: LayersInput!) {\\n  fields(options: \$options) {\\n   surface\\n   id\\n   active_crop_name\\n   active_crop_image_url\\n   center_point\\n   company_id\\n   layer_type\\n   name\\n   shape\\n    __typename\\n  }\\n}\\n\"\n}"

                //Create the request body
                val body = requestBodyString.toRequestBody("application/json".toMediaTypeOrNull())

                //Create the request
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "*/*").addHeader(
                        "x-cookie",
                        cookieString!!
                    )
                    .build()

                //Executing the request
                val response = client.newCall(request).execute()

                if (response.code == successfulResponse) { //Hvis denne rammer er request gået igennem - login er enten rigtig eller forkert
                    println("Getting the response : ")
                    val bodyString = response.body!!.string()
                    println("Response : $bodyString")
//                val bodyUserPath : JSONObject = JSONObject(bodyString).getJSONObject("data").getJSONObject("user")

                    println("Attempting to get the fields")
                    try {
                        val bodyUserPath : JSONArray = JSONObject(bodyString).getJSONObject("data").getJSONArray("fields")

                        for (i in 0 until bodyUserPath!!.length()) {
                            var idFromJSON : Int = bodyUserPath.getJSONObject(i).getInt("id")
                            var companyIdFromJSON : Int = bodyUserPath.getJSONObject(i).getInt("company_id")
                            var layerTypeFromJSON : String = bodyUserPath.getJSONObject(i).getString("layer_type")
                            var nameFromJSON : String = bodyUserPath.getJSONObject(i).getString("name")
                            var shapeTypeFromJSON : String = bodyUserPath.getJSONObject(i).getJSONObject("shape").getString("type")

                            //Hent coordinater til array
                            var shapeCoordinatesFromJSON : ArrayList<LatLng> = ArrayList()
                            var shapeCoordinatesPath = bodyUserPath.getJSONObject(i).getJSONObject("shape").getJSONArray("coordinates").getJSONArray(0)

                            for (j in 0 until shapeCoordinatesPath.length()) {  //Lat og Lng ligger som Lng -> Lat i DB, så de er byttet om her.
                                val lat = shapeCoordinatesPath.getJSONArray(j).getDouble(1)
                                val lng = shapeCoordinatesPath.getJSONArray(j).getDouble(0)
                                val coordinate : LatLng = LatLng(lat, lng)
                                shapeCoordinatesFromJSON.add(coordinate)
                            }


                            //Nye tilføjelser
                            var surfaceFromJSON = bodyUserPath.getJSONObject(i).getDouble("surface")
                            var activeCropNameFromJSON = bodyUserPath.getJSONObject(i).getString("active_crop_name")
                            var activeCropImageUrlFromJSON = bodyUserPath.getJSONObject(i).getString("active_crop_image_url")
                            var centerPointFromPath = bodyUserPath.getJSONObject(i).getJSONObject("center_point").getJSONArray("coordinates")
                            var centerPointFromJSON = LatLng(centerPointFromPath.getDouble(1), centerPointFromPath.getDouble(0))

                            //Load into fields
                            val field = Field(
                                id = idFromJSON,
                                companyId = companyIdFromJSON,
                                layerType = layerTypeFromJSON,
                                name = nameFromJSON,
                                surface = surfaceFromJSON,
                                activeCropName = activeCropNameFromJSON,
                                activeCropImageUrl = activeCropImageUrlFromJSON,
                                centerPoint = centerPointFromJSON,
                                shapeType = shapeTypeFromJSON,
                                shapeCoordinates = shapeCoordinatesFromJSON
                            )
                            fields.add(field)
                        }
                        succes = true
                    } catch (e: JSONException) { //If the jsonobject cant be found eg. bad login -> go here
                        println("We didn't get any fields")
                        succes = false
                    }
                }
                else {
                    println("Error loading fields")
                }
            }
            println("-----------------------------------")
            if (companyWasLoadedCorrectly) {
                loadUserThread.start()
                loadUserThread.join() //Apparently used to wait on a thread - see if a better way can be found - I think this affects the main thread
            }
        }


        println("-----------------------------------")
        return succes

    }

    fun loadCompanies () : Boolean {
        var succes = false
        var loadUserThread : Thread = Thread {
            val client = OkHttpClient()

            //Request body string for companies
            val requestBodyString = "[\n    {\n        \"operationName\": \"companies\",\n        \"variables\": {},\n        \"query\": \"query companies {\\n  companies {\\n    id\\n    name\\n    owner_photo_url\\n    access\\n    default\\n    location {\\n      type\\n      coordinates\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"\n    }\n]"

            //Create the request body
            val body = requestBodyString.toRequestBody("application/json".toMediaTypeOrNull())

            //Create the request
            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "*/*").addHeader(
                    "x-cookie",
                    cookieString!!
                )
                .build()

            //Executing the request
            val response = client.newCall(request).execute()

            if (response.code == successfulResponse) { //Hvis denne rammer er request gået igennem
                println("Getting the response : ")
                val bodyString = response.body!!.string()
                println("Response : $bodyString")

                println("Removing \"[]\" : ")
                var str = bodyString.substring(1, bodyString.length - 1) //Fjerner [] så det ligner array
                println("So it becomes : $str")


                println("Attempting to get the companies")
                try {
                    val bodyUserPath : JSONArray = JSONObject(str).getJSONObject("data").getJSONArray("companies")

                    for (i in 0 until bodyUserPath!!.length()) {
                        val idFromJSON : Int = bodyUserPath.getJSONObject(i).getInt("id")
                        val nameFromJSON : String = bodyUserPath.getJSONObject(i).getString("name")
                        val locationTypeFromJSON : String = bodyUserPath.getJSONObject(i).getJSONObject("location").getString("type")
                        //Get coordinates
                        val locationCoordinatesFromJSON = bodyUserPath.getJSONObject(i).getJSONObject("location").getJSONArray("coordinates")
                        val lat = locationCoordinatesFromJSON.getDouble(0)
                        val lng = locationCoordinatesFromJSON.getDouble(1)
                        val finishedLocationCoordinatesFromJSON : LatLng = LatLng(lat, lng)
                        val defaultFromJSON : Boolean = bodyUserPath.getJSONObject(i).getBoolean("default")
                        val ownerPhotoUrlFromJSON : String = bodyUserPath.getJSONObject(i).getString("owner_photo_url")
                        val accessFromJSON : String = bodyUserPath.getJSONObject(i).getString("access")
                        //Load into companies
                        var company = Company(
                            id = idFromJSON,
                            name = nameFromJSON,
                            locationType = locationTypeFromJSON,
                            locationCoordinates = finishedLocationCoordinatesFromJSON,
                            default = defaultFromJSON,
                            ownerPhotoUrl = ownerPhotoUrlFromJSON,
                            access = accessFromJSON
                            )
                        companies.add(company)

                    }
                    companyWasLoadedCorrectly = true
                    succes = true
                } catch (e: JSONException) { //If the jsonobject cant be found eg. bad login -> go here
                    println("We didn't get any company")
                    succes = false
                }
            }
            else {
                println("Error loading company")
            }
        }

        println("-----------------------------------")
        if (userWasLoadedCorrectly) {
            loadUserThread.start()
            loadUserThread.join() //Apparently used to wait on a thread - see if a better way can be found - I think this affects the main thread
        }
        println("-----------------------------------")
        return succes

    }




}

























