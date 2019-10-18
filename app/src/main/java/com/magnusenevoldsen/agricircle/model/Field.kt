package com.magnusenevoldsen.agricircle.model

import com.google.android.gms.maps.model.LatLng

data class Field(var id : Int,
                 var companyId : Int,
                 var layerType : String,
                 var name : String,

                 var shapeType : String,
                 var shapeCoordinates : ArrayList<LatLng>
)


//{
//    "id":30781,
//    "company_id":2434,
//    "layer_type":"field",
//    "name":"Dreschscheune",
//
//    "shape":{
//        "type":"Polygon",
//        "coordinates":[
//            [
//                [8.481863737106323,47.47464627229019],
//                [8.480903506278992,47.475284426476485],
//                [8.481423854827879,47.47579566940937],
//                [8.48167598247528,47.47566151394381],
//                [8.481885194778442,47.47579929522799],
//                [8.481606245040894,47.47600596647691],
//                [8.481767177581787,47.47611474049196],
//                [8.482764959335327,47.47674199958548],
//                [8.4837144613266,47.477329368363925],
//                [8.483800292015076,47.477347496925475],
//                [8.484020233154297,47.477289485506525],
//                [8.484009504318237,47.47600596647691],
//                [8.481863737106323,47.47464627229019]]
//            ]
//        },
//    "__typename":"Field"
//}










//[
//    {
//        "id": 30781,
//        "name": "Dreschscheune",
//        "surface": 3.29,
//        "layer_type": "field",
//
//        "soil_data": {
//            "soil_analyses": [],
//            "soil_trials": [],
//            "soil_type": null
//        },
//
//        "center_point": {
//            "type": "Point",
//            "coordinates": [8.48268509263342, 47.4759463472923]
//        },
//
//        "shape": {
//            "type": "Polygon",
//            "coordinates": [
//                [
//                [8.481863737106323, 47.47464627229019],
//                [8.480903506278992, 47.475284426476485],
//                [8.481423854827879, 47.47579566940937],
//                [8.48167598247528, 47.47566151394381],
//                [8.481885194778442, 47.47579929522799],
//                [8.481606245040894, 47.47600596647691],
//                [8.481767177581787, 47.47611474049196],
//                [8.482764959335327, 47.47674199958548],
//                [8.4837144613266, 47.477329368363925],
//                [8.483800292015076, 47.477347496925475],
//                [8.484020233154297, 47.477289485506525],
//                [8.484009504318237, 47.47600596647691],
//                [8.481863737106323, 47.47464627229019]
//                ]
//            ]
//        }
//    },
//
//
//]