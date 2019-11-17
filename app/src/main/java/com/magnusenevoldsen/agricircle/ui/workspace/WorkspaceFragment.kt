package com.magnusenevoldsen.agricircle.ui.workspace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.magnusenevoldsen.agricircle.AgriCircleBackend
import com.magnusenevoldsen.agricircle.R
import com.magnusenevoldsen.agricircle.model.DummyField
import com.magnusenevoldsen.agricircle.model.Field

class WorkspaceFragment : Fragment() {

    private lateinit var workspaceViewModel: WorkspaceViewModel
    private var listView : RecyclerView? = null
    private var headerTextView : TextView? = null
    private lateinit var workspaceAdapter : WorkspaceAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_workspace, container, false)

        headerTextView = root!!.findViewById(R.id.headerWorkspaceTextView)

        //Create dummydata
        var dummyArray : ArrayList<DummyField> = ArrayList()
        for (i in 0 until AgriCircleBackend.fields.size) {
            for (j in 0..1) {
                var activityString : String = ""
                if (j == 0)
                    activityString = getString(R.string.placeholder_Fertilization)
                if (j == 1)
                    activityString = getString(R.string.placeholder_sowing)
                val dummyItem = DummyField(AgriCircleBackend.fields[i], activityString)
                dummyArray.add(dummyItem)
            }
        }


        workspaceAdapter = WorkspaceAdapter(dummyArray)
        viewManager = LinearLayoutManager(this.context)

        listView = root.findViewById<RecyclerView>(R.id.workspaceRecyclerListView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = workspaceAdapter
        }

        val mDividerItemDecoration = DividerItemDecoration(
            listView!!.getContext(),
            1 //1 = Vertical, 0 = Horizontal
        )
        listView!!.addItemDecoration(mDividerItemDecoration)




        return root
    }
}
