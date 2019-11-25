package com.magnusenevoldsen.agricircle

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.magnusenevoldsen.agricircle.model.Field
import java.math.BigDecimal
import java.math.RoundingMode

object LocalBackend {

    var localFields : ArrayList<Field> = ArrayList()
    var allFields : ArrayList<Field> = ArrayList()

    fun loadLocalFields () {
        //TODO load fields from local db and put into localFields array
    }

    fun uploadFieldToLocalArray(field : Field) {
        //TODO Use local variable field and upload it

        //Push into localFields Array
        localFields.add(field)

    }

    fun prepareFieldForLocalUpload (context : Context, arrayOfLatLng : ArrayList<LatLng>, fieldName : String, fieldId : Int) {
        var companyId : Int = 999999
        if (!AgriCircleBackend.companies.isEmpty())
            companyId = AgriCircleBackend.companies[0].id
        var layerType = context.getString(R.string.localbackend_upload_field_preset_layertype)

        var size : Double = calculateSize(arrayOfLatLng)       //Surface
        var activeCrop = context.getString(R.string.localbackend_upload_field_preset_activecrop)
        var activeCropUrl = context.getString(R.string.localbackend_upload_field_preset_activecrop_url)
        var centerPoint : LatLng = calculateCenterpoint(arrayOfLatLng)

        var shapeType = context.getString(R.string.localbackend_upload_field_preset_shapetype)

        var field = Field(
            id = fieldId,
            name = fieldName,
            companyId = companyId,
            layerType = layerType,

            surface = size,
            activeCropName = activeCrop,
            activeCropImageUrl = activeCropUrl,
            centerPoint = centerPoint,

            shapeType = shapeType,
            shapeCoordinates = arrayOfLatLng
            )
        uploadFieldToLocalArray(field)
    }

    fun calculateSize (array : ArrayList<LatLng>) : Double {
        var size : Double = SphericalUtil.computeArea(array)
        size = (size / 10000)
        size = BigDecimal(size).setScale(2, RoundingMode.HALF_EVEN).toDouble()
        return size
    }

    fun calculateCenterpoint (array : ArrayList<LatLng>) : LatLng {
        var builder = LatLngBounds.builder()
        for (i in 0 until array.size)
            builder.include(array[i])
        val bounds = builder.build()
        var center = bounds.center
        return center
    }

}

