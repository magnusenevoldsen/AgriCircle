package com.magnusenevoldsen.agricircle

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.magnusenevoldsen.agricircle.model.Company
import com.magnusenevoldsen.agricircle.model.Field
import com.magnusenevoldsen.agricircle.model.User
import okhttp3.*
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.anko.doAsync
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
    var userWasLoadedCorrectly : Boolean = false
    var companyWasLoadedCorrectly : Boolean = false
    private val client = OkHttpClient()
    private var context : Context? = null

    fun login (contextFromLogin : Context, email : String, password : String) : Boolean {
        var success = false
        context = contextFromLogin

        val login = doAsync {

            val requestBodyString = "[\n    {\n        \"operationName\": \"login\",\n        \"query\": \"mutation login(\$options: LoginInput!) {\\n  login(options: \$options) {\\n    token\\n    cookie\\n    __typename\\n  }\\n}\\n\",\n        \"variables\": {\n            \"options\": {\n                \"email\": \"$email\",\n                \"password\": \"$password\",\n                \"locale\": \"en\"\n            }\n        }\n    }\n]"

            //Create the request body
            val body = requestBodyString.toRequestBody(context!!.getString(R.string.http_header_content_type_value_application_json).toMediaTypeOrNull())

            //Create the request
            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader(context!!.getString(R.string.http_header_content_type), context!!.getString(R.string.http_header_content_type_value_application_json))
                .addHeader(context!!.getString(R.string.http_header_accept), context!!.getString(R.string.http_header_accept_value_star))
                .build()

            //Executing the request
            val response = client.newCall(request).execute()

            val responseCode = response.code

            if (responseCode == successfulResponse) { //Hvis denne rammer er request gået igennem - login er enten rigtig eller forkert
                val bodyString = response.body!!.string()


                var str = bodyString.substring(1, bodyString.length - 1) //Fjerner [] så det ligner array

              //Attempting to get the cookie")
                try {
                    cookie = JSONObject(str).getJSONObject(context!!.getString(R.string.backend_data)).getJSONObject(context!!.getString(R.string.backend_login)).getJSONArray(context!!.getString(R.string.backend_cookie))

                    //Get the real cookie for further requests
                    for (i in 0 until cookie!!.length()) {
                        for (item in cookie!!.getString(i).split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                            if (item.startsWith(context!!.getString(R.string.backend_agricircle_subdomain_session))) {
                                cookieString = item
                            }
                        }
                    }
                    userWasLoadedCorrectly = true
                    success = true
                } catch (e: JSONException) { //If the jsonobject cant be found eg. bad login -> go here
                    success = false
                }
            }
            else {
                success = false
            }
        }

        while (!login.isDone) {
            //Block until done.
        }

        return success
    }


    fun loadUser () : Boolean {
        var success = false
        var loadUserThread : Thread = Thread {
            val client = OkHttpClient()

            //Request body string for user
            val requestBodyString = "{\n    \"operationName\": \"user\",\n    \"variables\": {},\n    \"query\": \"query user {\\n  user {\\n    name\\n    language\\n    avatarUrl\\n planningYears\\n    decimalSeparator\\n    dateFormat\\n    slug\\n    features\\n    avatarUrl\\n    __typename\\n  }\\n}\\n\"\n}"

            //Create the request body
            val body = requestBodyString.toRequestBody(context!!.getString(R.string.http_header_content_type_value_application_json).toMediaTypeOrNull())

            //Create the request
            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader(context!!.getString(R.string.http_header_content_type), context!!.getString(R.string.http_header_content_type_value_application_json))
                .addHeader(context!!.getString(R.string.http_header_accept), context!!.getString(R.string.http_header_accept_value_star)).addHeader(
                    context!!.getString(R.string.backend_header_x_cookie),
                    cookieString!!
                )
                .build()

            //Executing the request
            val response = client.newCall(request).execute()

            if (response.code == successfulResponse) { //Hvis denne rammer er request gået igennem - login er enten rigtig eller forkert

                val bodyString = response.body!!.string()

                val bodyUserPath : JSONObject = JSONObject(bodyString).getJSONObject(context!!.getString(R.string.backend_data)).getJSONObject(context!!.getString(R.string.backend_user))

                try {
                    val nameFromJSON : String = bodyUserPath.getString(context!!.getString(R.string.backend_name))
                    val languageFromJSON : String = bodyUserPath.getString(context!!.getString(R.string.backend_language))
                    val avatarUrlFromJSON : String = bodyUserPath.getString(context!!.getString(R.string.backend_avatar_url))

                    //Get JSONarray and make it an ArrayList
                    val planningYearsFromJSON : JSONArray = bodyUserPath.getJSONArray(context!!.getString(R.string.backend_planning_years))
                    var planningYearsArrayList : ArrayList<Int> = ArrayList()
                    for (i in 0 until planningYearsFromJSON.length()) { planningYearsArrayList.add(planningYearsFromJSON.getInt(i)) }
                    val decimalSeparatorFromJSON : String = bodyUserPath.getString(context!!.getString(R.string.backend_decimal_separator))
                    val dateFormatFromJSON : String = bodyUserPath.getString(context!!.getString(R.string.backend_date_format))
                    val slugFromJSON : String = bodyUserPath.getString(context!!.getString(R.string.backend_slug))

                    //Get JSONarray and make it an ArrayList
                    val featuresFromJSON : JSONArray = bodyUserPath.getJSONArray(context!!.getString(R.string.backend_features))
                    var featuresArrayList : ArrayList<String> = ArrayList()
                    for (i in 0 until featuresFromJSON.length()) { featuresArrayList.add(featuresFromJSON.getString(i)) }
                    val typenameFromJSON : String = bodyUserPath.getString(context!!.getString(R.string.backend_typename))

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
                    success = true
                } catch (e: JSONException) { //If the jsonobject cant be found eg. bad login -> go here
                    success = false
                }
            }
            else {
                //Error
            }
        }
        if (userWasLoadedCorrectly) {
            loadUserThread.start()
            loadUserThread.join() //Wait for thread to finish
        }
        return success
    }

    fun loadFields () : Boolean {
        var success = false

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
                val body = requestBodyString.toRequestBody(context!!.getString(R.string.http_header_content_type_value_application_json).toMediaTypeOrNull())

                //Create the request
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader(context!!.getString(R.string.http_header_content_type), context!!.getString(R.string.http_header_content_type_value_application_json))
                    .addHeader(context!!.getString(R.string.http_header_accept), context!!.getString(R.string.http_header_accept_value_star)).addHeader(
                        context!!.getString(R.string.backend_header_x_cookie),
                        cookieString!!
                    )
                    .build()

                //Executing the request
                val response = client.newCall(request).execute()

                if (response.code == successfulResponse) { //Hvis denne rammer er request gået igennem - login er enten rigtig eller forkert

                    val bodyString = response.body!!.string()

                    try {
                        val bodyUserPath : JSONArray = JSONObject(bodyString).getJSONObject(context!!.getString(R.string.backend_data)).getJSONArray(context!!.getString(R.string.backend_field))

                        for (i in 0 until bodyUserPath!!.length()) {
                            var idFromJSON : Int = bodyUserPath.getJSONObject(i).getInt(context!!.getString(R.string.backend_id))
                            var companyIdFromJSON : Int = bodyUserPath.getJSONObject(i).getInt(context!!.getString(R.string.backend_company_id))
                            var layerTypeFromJSON : String = bodyUserPath.getJSONObject(i).getString(context!!.getString(R.string.backend_layer_type))
                            var nameFromJSON : String = bodyUserPath.getJSONObject(i).getString(context!!.getString(R.string.backend_name))
                            var shapeTypeFromJSON : String = bodyUserPath.getJSONObject(i).getJSONObject(context!!.getString(R.string.backend_shape)).getString(context!!.getString(R.string.backend_type))

                            //Hent coordinater til array
                            var shapeCoordinatesFromJSON : ArrayList<LatLng> = ArrayList()
                            var shapeCoordinatesPath = bodyUserPath.getJSONObject(i).getJSONObject(context!!.getString(R.string.backend_shape)).getJSONArray(context!!.getString(
                                                            R.string.backend_coordinates)).getJSONArray(0)

                            for (j in 0 until shapeCoordinatesPath.length()) {  //Lat og Lng ligger som Lng -> Lat i DB, så de er byttet om her.
                                val lat = shapeCoordinatesPath.getJSONArray(j).getDouble(1)
                                val lng = shapeCoordinatesPath.getJSONArray(j).getDouble(0)
                                val coordinate : LatLng = LatLng(lat, lng)
                                shapeCoordinatesFromJSON.add(coordinate)
                            }


                            //Nye tilføjelser
                            var surfaceFromJSON = bodyUserPath.getJSONObject(i).getDouble(context!!.getString(R.string.backend_surface))
                            var activeCropNameFromJSON = bodyUserPath.getJSONObject(i).getString(context!!.getString(R.string.backend_active_crop_name))
                            var activeCropImageUrlFromJSON = bodyUserPath.getJSONObject(i).getString(context!!.getString(R.string.backend_active_crop_image_url))
                            var centerPointFromPath = bodyUserPath.getJSONObject(i).getJSONObject(context!!.getString(R.string.backend_center_point)).getJSONArray(context!!.getString(R.string.backend_coordinates))
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
                        success = true
                    } catch (e: JSONException) { //If the jsonobject cant be found eg. bad login -> go here
                        success = false
                    }
                }
                else {
                }
            }
            if (companyWasLoadedCorrectly) {
                loadUserThread.start()
                loadUserThread.join() //Wait for thread to finish
            }
        }

        return success

    }

    fun loadCompanies () : Boolean {
        var success = false
        var loadUserThread : Thread = Thread {
            val client = OkHttpClient()

            //Request body string for companies
            val requestBodyString = "[\n    {\n        \"operationName\": \"companies\",\n        \"variables\": {},\n        \"query\": \"query companies {\\n  companies {\\n    id\\n    name\\n    owner_photo_url\\n    access\\n    default\\n    location {\\n      type\\n      coordinates\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"\n    }\n]"

            //Create the request body
            val body = requestBodyString.toRequestBody(context!!.getString(R.string.http_header_content_type_value_application_json).toMediaTypeOrNull())

            //Create the request
            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader(context!!.getString(R.string.http_header_content_type), context!!.getString(R.string.http_header_content_type_value_application_json))
                .addHeader(context!!.getString(R.string.http_header_accept), context!!.getString(R.string.http_header_accept_value_star)).addHeader(
                    context!!.getString(R.string.backend_header_x_cookie),
                    cookieString!!
                )
                .build()

            //Executing the request
            val response = client.newCall(request).execute()

            if (response.code == successfulResponse) { //Hvis denne rammer er request gået igennem

                val bodyString = response.body!!.string()

                var str = bodyString.substring(1, bodyString.length - 1) //Fjerner [] så det ligner array

                try {
                    val bodyUserPath : JSONArray = JSONObject(str).getJSONObject(context!!.getString(R.string.backend_data)).getJSONArray(context!!.getString(R.string.backend_companies))

                    for (i in 0 until bodyUserPath!!.length()) {
                        val idFromJSON : Int = bodyUserPath.getJSONObject(i).getInt(context!!.getString(R.string.backend_id))
                        val nameFromJSON : String = bodyUserPath.getJSONObject(i).getString(context!!.getString(R.string.backend_name))
                        val locationTypeFromJSON : String = bodyUserPath.getJSONObject(i).getJSONObject(context!!.getString(R.string.backend_location)).getString(context!!.getString(R.string.backend_type))

                        //Get coordinates
                        val locationCoordinatesFromJSON = bodyUserPath.getJSONObject(i).getJSONObject(context!!.getString(R.string.backend_location)).getJSONArray(context!!.getString(R.string.backend_coordinates))
                        val lat = locationCoordinatesFromJSON.getDouble(0)
                        val lng = locationCoordinatesFromJSON.getDouble(1)
                        val finishedLocationCoordinatesFromJSON : LatLng = LatLng(lat, lng)
                        val defaultFromJSON : Boolean = bodyUserPath.getJSONObject(i).getBoolean(context!!.getString(R.string.backend_default))
                        val ownerPhotoUrlFromJSON : String = bodyUserPath.getJSONObject(i).getString(context!!.getString(R.string.backend_owner_photo_url))
                        val accessFromJSON : String = bodyUserPath.getJSONObject(i).getString(context!!.getString(R.string.backend_access))

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
                    success = true
                } catch (e: JSONException) { //If the jsonobject cant be found eg. bad login -> go here
                    success = false
                }
            }
            else {
            }
        }

        if (userWasLoadedCorrectly) {
            loadUserThread.start()
            loadUserThread.join() //Wait for thread to finish
        }
        return success
    }

}

























