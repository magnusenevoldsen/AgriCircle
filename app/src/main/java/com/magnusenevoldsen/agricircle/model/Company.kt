package com.magnusenevoldsen.agricircle.model

import com.google.android.gms.maps.model.LatLng

data class Company(var id : Int,
                   var name : String,

                   var locationType : String,
                   var locationCoordinates : LatLng,

                   var default : Boolean,
                   var ownerPhotoUrl : String,
                   var access : String
)