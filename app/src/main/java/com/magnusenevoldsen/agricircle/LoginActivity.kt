package com.magnusenevoldsen.agricircle

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class LoginActivity : AppCompatActivity() {


    //Layout
    var loginButton : Button? = null
    var createNewAccountTextView : TextView? = null
    var signUpButton : Button? = null
    var view : View? = null
    var usernameEditText : TextInputLayout? = null
    var passwordEditText : TextInputLayout? = null
    var spinnerLayout : ConstraintLayout? = null
    var logoImageView : ImageView? = null



    override fun onResume() {
        super.onResume()
        resetUserDataOnlogout()
        toggleSpinnerLayout(false)
        loginButton!!.isClickable = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        view = findViewById(R.id.activityLogin)

        spinnerLayout = findViewById(R.id.spinnerLayout)
        spinnerLayout!!.visibility = View.GONE


        logoImageView = findViewById(R.id.loginLogoImageView)


        //Create account layout
        createNewAccountTextView = findViewById(R.id.createAccountTextView)
        signUpButton = findViewById(R.id.signUpButton)

        //Log in layout
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)


        //   ---   TEMP   ---
        val image = findViewById<ImageView>(R.id.loginLogoImageView)



        //Username edittext
        usernameEditText!!.editText!!.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus)
                showKeyboard()
        }

        //Password edittext
        passwordEditText!!.editText!!.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus)
                showKeyboard()
        }

        //Log in button
        loginButton!!.setOnClickListener {
            toggleSpinnerLayout(true)
            tryToLogin()
        }

        //Sign in button
        signUpButton!!.setOnClickListener {
            sendMessageToUser(getString(R.string.not_implemented_yet))
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

    fun loginAndHideKeyboard() {
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


        var email : String = usernameEditText!!.editText!!.text.toString()
        var password : String = passwordEditText!!.editText!!.text.toString()

        val chosenUser : Int = 2 //1 for Jacob, 2 for Magnus
        email  = SensitiveInfo.returnEmail(chosenUser)+usernameEditText!!.editText!!.text.toString()
        password  = SensitiveInfo.returnPassword(chosenUser)


//        var email : String = usernameEditText!!.editText!!.text.toString()
//        var password : String = passwordEditText!!.editText!!.text.toString()

//        if (email.equals("") && password.equals("")) {
//            val chosenUser : Int = 2 //1 for Jacob, 2 for Magnus
//            email = SensitiveInfo.returnEmail(chosenUser)
//            password = SensitiveInfo.returnPassword(chosenUser)
//        }

        loginButton!!.isClickable = false
        loginAndHideKeyboard()

        if (AgriCircleBackend.login(email, password)) {
            println("Login was successful!")
            loadData()
        } else {
            println("Login was not successful")
            loginButton!!.isClickable = true
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
        val mySnackbar = Snackbar.make(findViewById(R.id.activityLogin), message, Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }




}
