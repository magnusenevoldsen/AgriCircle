package com.magnusenevoldsen.agricircle.ui.workspace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.magnusenevoldsen.agricircle.R


class WorkspaceInfoFragment : Fragment() {


    private var root : View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_workspace_info, container, false)


        //Layout
        val backButton : ImageView = root!!.findViewById(R.id.backButtonWorkInfo)
//        backButton.setBackgroundResource(0) //Removes grey background on an imagebutton
        backButton.setColorFilter(R.color.colorAgricircle)
        backButton.setOnClickListener {
            activity!!.onBackPressed()
        }









        return root
    }

}