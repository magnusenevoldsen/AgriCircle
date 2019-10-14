package com.magnusenevoldsen.agricircle.model

data class User(var name : String,
                var language : String,
                var avatarUrl : String,
                var planningYears : ArrayList<Int>,
                var decimalSeparator : String,
                var dateFormat : String,
                var slug : String,
                var features : ArrayList<String>,
                var typename : String
                )







//{
//    "data": {
//      "user": {
//          "name": "Jacob Nordfalk",
//          "language": "en",
//          "avatarUrl": "https://core.agricircle.com/static/images/agricircle/profile_photo_placeholder.svg",
//          "planningYears": [
//              2014,
//              2015,
//              2016,
//              2017,
//              2018,
//              2019,
//              2020
//              ],
//          "decimalSeparator": ".",
//          "dateFormat": "dd.mm.yyyy",
//          "slug": "jacob-nordfalk-f56b7435-09c3-4077-a59c-d01b2df50bd9",
//          "features": [
//              "historical_weather",
//              "new_geotiff_import",
//              "old_geotiff_import",
//              "soil_zoning",
//              "satellite",
//              "graphiql"
//              ],
//          "__typename": "User"
//      }
//  }
//}


