package com.magnusenevoldsen.agricircle.ui.workspace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.magnusenevoldsen.agricircle.R

class WorkspaceFragment : Fragment() {

    private lateinit var workspaceViewModel: WorkspaceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        workspaceViewModel =
            ViewModelProviders.of(this).get(WorkspaceViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_workspace, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)
        workspaceViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}