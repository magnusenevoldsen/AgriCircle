package com.magnusenevoldsen.agricircle

import com.magnusenevoldsen.agricircle.model.User
import okhttp3.*
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import android.os.AsyncTask.execute
import okhttp3.RequestBody




object AgriCircleBackend {

    private val url : String = "https://graphql.agricircle.com/graphql"
    private var cookie : JSONArray ?= null
    private var cookieString : String ?=null
    private val successfulResponse : Int = 200
    var user : User? = null
    private var userWasLoadedCorrectly : Boolean = false
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
        loginThread.start()
        loginThread.join() //Apparently used to wait on a thread - see if a better way can be found - I think this affects the main thread

        return succes
    }


    fun loadUser () : Boolean {
        var succes = false
        var loadUserThread : Thread = Thread {
            val client = OkHttpClient()

            //Request body string for login
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
        if (userWasLoadedCorrectly) {
            loadUserThread.start()
            loadUserThread.join() //Apparently used to wait on a thread - see if a better way can be found - I think this affects the main thread
        }
        return succes

    }




}

























