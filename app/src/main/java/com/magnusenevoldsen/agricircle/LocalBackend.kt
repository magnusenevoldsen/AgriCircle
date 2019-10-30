package com.magnusenevoldsen.agricircle

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.magnusenevoldsen.agricircle.model.Field
import java.math.RoundingMode
import java.text.DecimalFormat


object LocalBackend {


    private var field : Field? = null

    var localFields : ArrayList<Field> = ArrayList()

    fun loadLocalFields () {
        //TODO load fields from local db and put into localFields array
    }


    fun uploadFieldToLocalArray() {
        //TODO Use local variable field and upload it

        //Push into db

        //Push into AgricircleBackend Array
        field?.let { localFields.add(it) }

    }


    fun prepareFieldForLocalUpload (array : ArrayList<LatLng>, fieldName : String, fieldId : Int) {
        var companyId : Int = 999999
        if (AgriCircleBackend.companies.isEmpty())
            companyId = AgriCircleBackend.companies[0].id
        var layerType = "field"

        var size : Double = calculateSize(array)       //Surface
        var activeCrop = ""
        var activeCropUrl = ""
        var centerPoint : LatLng = calculateCenterpoint(array)

        var shapeType = "point"

        field = Field(
            id = fieldId,
            name = fieldName,
            companyId = companyId,
            layerType = layerType,

            surface = size,
            activeCropName = activeCrop,
            activeCropImageUrl = activeCropUrl,
            centerPoint = centerPoint,

            shapeType = shapeType,
            shapeCoordinates = array
            )
        uploadFieldToLocalArray()
    }

    fun calculateSize (array : ArrayList<LatLng>) : Double {
        var size : Double = SphericalUtil.computeArea(array)
        size = (size / 10000)
        size = roundOffDecimal(size)
        return size
    }

    fun roundOffDecimal(number: Double): Double {
        val decimalFormat = DecimalFormat("#.##")
        decimalFormat.roundingMode = RoundingMode.CEILING
        return decimalFormat.format(number).toDouble()
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

























