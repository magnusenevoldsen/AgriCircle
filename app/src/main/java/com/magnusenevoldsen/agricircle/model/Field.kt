package com.magnusenevoldsen.agricircle.model

import com.google.android.gms.maps.model.LatLng

data class Field(var id : Int,
                 var companyId : Int,
                 var layerType : String,
                 var name : String,

                 var surface : Double,
                 var activeCropName : String,
                 var activeCropImageUrl : String,
                 var centerPoint : LatLng,

                 var shapeType : String,
                 var shapeCoordinates : ArrayList<LatLng>
)