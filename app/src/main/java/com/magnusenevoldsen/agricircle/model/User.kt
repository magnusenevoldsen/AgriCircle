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