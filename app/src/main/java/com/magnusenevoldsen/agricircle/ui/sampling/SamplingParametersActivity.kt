package com.magnusenevoldsen.agricircle.ui.sampling

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import com.magnusenevoldsen.agricircle.R

class SamplingParametersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sampling_parameters)


        //Layout
        val backButton : ImageView = findViewById(R.id.backButtonSampParam)
//        backButton.setBackgroundResource(0) //Removes grey background on an imagebutton
        backButton.setColorFilter(R.color.colorAgricircle)
        backButton.setOnClickListener {
            super.onBackPressed()
        }


        val confirmButton : Button = findViewById(R.id.confirmSampParamButton)
        confirmButton.setOnClickListener {

        }






    }
}


