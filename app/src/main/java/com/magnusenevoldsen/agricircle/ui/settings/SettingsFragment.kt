package com.magnusenevoldsen.agricircle.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.magnusenevoldsen.agricircle.AgriCircleBackend
import com.magnusenevoldsen.agricircle.R
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    var userImageView : ImageView? = null
    var userWebView : WebView? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        userImageView = root.findViewById(R.id.userImageView)
        userWebView = root.findViewById(R.id.userWebView)

        val userNameTextView : TextView = root.findViewById(R.id.userNameTextView)


        //Load data into page:
        try {
            //Find the Url and then find the extension
            var avatarUrl : String = AgriCircleBackend.user!!.avatarUrl
            var shortened : String = avatarUrl.substring(avatarUrl.length - 4, avatarUrl.length)

            //If its a .svg -> Load it into a webview instead
            if (shortened.equals(".svg")) {
                changeImageView(true) //Use WebView
                userWebView!!.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        view?.loadUrl(url)
                        return true
                    }
                }
                userWebView!!.loadUrl(avatarUrl)
            } else {
                changeImageView(false) //Use ImageView
                Picasso.get().load(avatarUrl).into(userImageView)
            }



            userNameTextView.text = AgriCircleBackend.user!!.name
        } catch (e : KotlinNullPointerException) {
            println("  --  SETTINGS SCREEN  --  // User was never created, trying now")
            AgriCircleBackend.loadUser()
        }






//        val textView: TextView = root.findViewById(R.id.text_settings)
//        settingsViewModel.text.observe(this, Observer {
//            textView.text = it
//        })

        return root
    }


    fun changeImageView(option : Boolean) {
        //True = show WebView
        //False = show ImageView
        if (option) {
            userWebView!!.visibility = View.VISIBLE
            userImageView!!.visibility = View.INVISIBLE
        }
        else if (!option) {
            userWebView!!.visibility = View.INVISIBLE
            userImageView!!.visibility = View.VISIBLE
        }

    }





}














