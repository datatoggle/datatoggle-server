package com.datatoggle.server.tools

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class DbUtils {

    companion object {

        fun jsonToMap(dbJson: io.r2dbc.postgresql.codec.Json): Map<String, Any> {
            val gson = Gson()
            val empMapType: Type = object : TypeToken<Map<String, Any?>?>() {}.getType()
            val config = gson.fromJson<Map<String, Any>>(dbJson.asString(), empMapType)
            return config
        }

        fun mapToJson(map: Map<String, Any>): io.r2dbc.postgresql.codec.Json {
            val gson = Gson()
            val jsonStr = gson.toJson(map)
            val jsonDb = io.r2dbc.postgresql.codec.Json.of(jsonStr)
            return jsonDb
        }
    }
}
