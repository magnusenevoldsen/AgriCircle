package com.magnusenevoldsen.agricircle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class LoginActivity : AppCompatActivity() {


    //Layout
    private var loginButton : Button? = null
    private var createNewAccountTextView : TextView? = null
    private var signUpButton : Button? = null
    private var view : View? = null
    private var usernameEditText : TextInputLayout? = null
    private var passwordEditText : TextInputLayout? = null
    private var spinnerLayout : ConstraintLayout? = null
    private var logoImageView : ImageView? = null
    private var blurryView : View? = null



    override fun onResume() {
        super.onResume()
        resetUserDataOnlogout()
        toggleSpinnerLayout(false)
        toggleClickablity(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        view = findViewById(R.id.activityLogin)


        spinnerLayout = findViewById(R.id.spinnerLayout)
        spinnerLayout!!.visibility = View.GONE

        //Hide keyboard initially
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)


        logoImageView = findViewById(R.id.loginLogoImageView)


        blurryView = findViewById(R.id.blurryView)
        blurryView!!.visibility = View.GONE
        blurryView!!.bringToFront()


        //Create account layout
        createNewAccountTextView = findViewById(R.id.createAccountTextView)
        signUpButton = findViewById(R.id.signUpButton)

        //Log in layout
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)


        val image = findViewById<ImageView>(R.id.loginLogoImageView)



        //Username edittext
//        usernameEditText!!.editText!!.setOnFocusChangeListener { view, hasFocus ->
//            if (hasFocus)
//                showKeyboard()
//        }
//
//        //Password edittext
//        passwordEditText!!.editText!!.setOnFocusChangeListener { view, hasFocus ->
//            if (hasFocus)
//                showKeyboard()
//        }

        //Log in button
        loginButton!!.setOnClickListener {
            toggleSpinnerLayout(true)
            tryToLogin()
        }

        //Sign in button
        signUpButton!!.setOnClickListener {
            sendMessageToUser(getString(R.string.sign_up_button_go_to_website))
        }





    }


//    fun showCreateAccountItems (option : Boolean) {
//        if (option) {
//            createNewAccountTextView!!.visibility = View.VISIBLE
//            signUpButton!!.visibility = View.VISIBLE
//        } else {
//            createNewAccountTextView!!.visibility = View.GONE
//            signUpButton!!.visibility = View.GONE
//        }
//    }

    fun hideKeyboard() {
        hideKeyboard(window.decorView.findViewById(R.id.activityLogin))
//        showCreateAccountItems(true)
    }

    fun showKeyboard() {
//        showCreateAccountItems(false)
    }

    fun toggleSpinnerLayout (toggle : Boolean) {
        if (!toggle) {
            spinnerLayout!!.visibility = View.GONE
        }
        else {
            spinnerLayout!!.visibility = View.VISIBLE
        }
    }


    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }



    fun tryToLogin () {


//        var email : String = usernameEditText!!.editText!!.text.toString()
//        var password : String = passwordEditText!!.editText!!.text.toString()
//
//        val chosenUser : Int = 2 //1 for Jacob, 2 for Magnus
//        email  = SensitiveInfo.returnEmail(chosenUser)+usernameEditText!!.editText!!.text.toString()
//        password  = SensitiveInfo.returnPassword(chosenUser)


        var email : String = usernameEditText!!.editText!!.text.toString()
        var password : String = passwordEditText!!.editText!!.text.toString()

        if (email.equals("") && password.equals("")) {
            val chosenUser : Int = 2 //1 for Jacob, 2 for Magnus
            email = SensitiveInfo.returnEmail(chosenUser)
            password = SensitiveInfo.returnPassword(chosenUser)
        }

        toggleClickablity(false)
        hideKeyboard()

        if (AgriCircleBackend.login(email, password)) {
//            println("Login was successful!")
            loadData()
        } else {
//            println("Login was not successful")
            toggleClickablity(true)
            sendMessageToUser(getString(R.string.login_bad_info))
            toggleSpinnerLayout(false)
        }

    }

    fun loadData() {
        doAsync {
            AgriCircleBackend.loadUser()
            AgriCircleBackend.loadCompanies()
            AgriCircleBackend.loadFields()

            uiThread {
                toggleSpinnerLayout(true)
                goToNextScreen()
            }
        }
    }

    fun toggleClickablity (toggle : Boolean) {
        if (toggle) {
            loginButton!!.isClickable = true
            signUpButton!!.isClickable = true
            blurryView!!.visibility = View.GONE
        }
        else {
            loginButton!!.isClickable = false
            signUpButton!!.isClickable = false
            blurryView!!.visibility = View.VISIBLE
        }
    }


    fun goToNextScreen () {
        val goToMainActivity = Intent(this, MainActivity::class.java)//.apply { putExtra(EXTRA_MESSAGE, message) }
        startActivity(goToMainActivity)
    }

    fun resetUserDataOnlogout() {
        AgriCircleBackend.fields.clear()
        AgriCircleBackend.companies.clear()
        AgriCircleBackend.userWasLoadedCorrectly = false
        AgriCircleBackend.companyWasLoadedCorrectly = false
        LocalBackend.localFields.clear()
        LocalBackend.allFields.clear()
    }

    fun sendMessageToUser(message: String) {
        val mySnackbar = Snackbar.make(findViewById(R.id.activityLogin), message, Snackbar.LENGTH_LONG)
        mySnackbar.show()
    }




}
