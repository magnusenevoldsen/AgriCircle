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



//{
//    "id": 2434,
//    "name": "DTU Diplom",
//
//    "location": {
//        "type": "Point",
//        "coordinates": [55.73152, 12.3967]
//},
//    "default": true,
//    "owner_photo_url": null,
//    "access": "edit"
//},