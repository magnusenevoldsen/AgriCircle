package com.magnusenevoldsen.agricircle.ui.workspace

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.magnusenevoldsen.agricircle.AgriCircleBackend
import com.magnusenevoldsen.agricircle.LocalBackend
import com.magnusenevoldsen.agricircle.R
import com.magnusenevoldsen.agricircle.model.DummyField
import com.magnusenevoldsen.agricircle.model.Field
import com.magnusenevoldsen.agricircle.ui.map.DrivingActivity
import com.squareup.picasso.Picasso


class WorkspaceAdapter(val fields : ArrayList<DummyField>) : RecyclerView.Adapter<WorkspaceAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindField(fields[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.workspace_list_item, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return fields.count()
    }


    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {


        val cropImageView = itemView?.findViewById<ImageView>(R.id.workspaceImageView)
        val fieldName = itemView?.findViewById<TextView>(R.id.workspaceFieldTextView)
        val fieldActivity = itemView?.findViewById<TextView>(R.id.workspaceActivityTextView)
        val playButton = itemView?.findViewById<ImageButton>(R.id.workspaceImageButton)



        fun bindField(field: DummyField) {

            var imageUrl : String = field.field.activeCropImageUrl

            cropImageView!!.setImageResource(R.drawable.stock_crop_image)
            if (!imageUrl.equals("null")){
                try {
                    Picasso.get().load(imageUrl).into(cropImageView)
                } catch (e : IllegalArgumentException) {
                    Log.d("", e.toString())
                }
            }
            fieldName?.text = field.field.name
            fieldActivity?.text = field.activity

            playButton?.setOnClickListener {
                var fieldid : Int? = null
                for (i in 0 until LocalBackend.allFields.size)
                    if (LocalBackend.allFields[i].name.equals(field.field.name))
                        fieldid = i
                val intent = Intent (itemView.context, DrivingActivity::class.java)
                intent.putExtra(itemView.context.resources.getString(R.string.intent_extra_field_id), fieldid)
                intent.putExtra(itemView.context.resources.getString(R.string.intent_extra_field_activity), field.activity)
                itemView.context!!.startActivity(intent)
            }

        }
    }
}