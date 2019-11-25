package com.magnusenevoldsen.agricircle.ui.map

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.magnusenevoldsen.agricircle.AgriCircleBackend
import com.magnusenevoldsen.agricircle.LoginActivity
import com.magnusenevoldsen.agricircle.R
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {

    var userImageView : ImageView? = null
    var userWebView : WebView? = null
    var logOutButton : Button? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            AgriCircleBackend.loadUser()
        }

        logOutButton = root!!.findViewById(R.id.logOutButton)
        logOutButton!!.setOnClickListener {
            val intent = Intent (activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity!!.startActivity(intent)
        }

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

