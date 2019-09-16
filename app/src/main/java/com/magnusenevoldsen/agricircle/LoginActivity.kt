package com.magnusenevoldsen.agricircle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class LoginActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<TextInputLayout>(R.id.usernameEditText)
        val passwordEditText = findViewById<TextInputLayout>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)



        loginButton.setOnClickListener {
            val goToMainActivity = Intent(this, MainActivity::class.java)//.apply { putExtra(EXTRA_MESSAGE, message) }
            startActivity(goToMainActivity)
        }

        signUpButton.setOnClickListener {
            sendMessageToUser(getString(R.string.not_implemented_yet))
        }





    }


    fun sendMessageToUser(message: String) {
        val mySnackbar = Snackbar.make(findViewById(R.id.activityLogin), message, Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }
}
