package com.magnusenevoldsen.agricircle.ui.sampling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.magnusenevoldsen.agricircle.R

class SamplingFragment : Fragment() {

    private lateinit var samplingViewModel: SamplingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        samplingViewModel =
            ViewModelProviders.of(this).get(SamplingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        samplingViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}