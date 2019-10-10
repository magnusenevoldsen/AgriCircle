package com.magnusenevoldsen.agricircle

import okhttp3.*
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException


object AgriCircleBackend {

    val url : String = "https://graphql.agricircle.com/graphql"
    var cookie : JSONArray ?= null
    val successfulResponse : Int = 200




    private val client = OkHttpClient()



    fun login (email : String, password : String) : Boolean {
        var succes = false
        var loginThread : Thread = Thread {
            val client = OkHttpClient()

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





}

