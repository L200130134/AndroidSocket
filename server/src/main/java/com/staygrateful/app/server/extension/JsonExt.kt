package com.staygrateful.app.server.extension

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import org.json.JSONObject
import java.io.StringReader

fun <T> MutableList<T>?.toJson(): String? {
    if (this == null) {
        return null
    }
    val gson = Gson()
    val type = object : TypeToken<MutableList<T>?>() {}.type
    return gson.toJson(this, type)
}

inline fun <reified T> fromJson(json: String?): T? {
    try {
        val reader = JsonReader(StringReader(json))
        reader.isLenient = true
        return Gson().fromJson(reader, object: TypeToken<T>(){}.type)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

/*fun <T> String?.fromJson(): MutableList<T> {
    if (this == null) {
        return mutableListOf()
    }
    val type = object : TypeToken<MutableList<T>>() {}.type
    return Gson().fromJson(this, type)
}*/

fun <T> String?.fromJson(classOfT: Class<T>): T? {
    try {
        if (this != null) {
            return Gson().fromJson(this, classOfT)
        }
    } catch (e : Exception) {
        println("ConnectThread:: message : ${e.message}")
        e.printStackTrace()
    }
    return null
}

fun Any?.toJson(): String? {
    try {
        if (this != null) {
            return Gson().toJson(this)
        }
    } catch (e : Exception) {
        println("ConnectThread:: toJson : ${this!!::class.java.simpleName}, message : ${e.localizedMessage}")
        e.printStackTrace()
    }
    return null
}

fun Any?.toJsonTree(): JsonElement? {
    try {
        if (this != null) {
            return Gson().toJsonTree(this, this.javaClass)
        }
    } catch (e : Exception) {
        println("ConnectThread:: toJsonTree : ${e.localizedMessage}")
        e.printStackTrace()
    }
    return null
}

fun <T> Any?.toJsonObject(classOfT: Class<T>): T? {
    try {
        if (this != null) {
            val jsonObject = toJsonTree()?.asJsonObject
            println("toJsonObject: jsonObject -> $jsonObject")
            if (jsonObject != null) {
                return jsonObject.toString().fromJson(classOfT)
            }
        }
    } catch (e : Exception) {
        println("toJsonObject : ${e.localizedMessage}")
        e.printStackTrace()
    }
    return null
}

fun createJsonObject(): JSONObject {
    return JSONObject()
}

fun JSONObject.putObject(name: String, jsonObject: JSONObject): JSONObject {
    this.put(name, jsonObject)
    return this
}

fun JSONObject.putData(name: String, any: Any): JSONObject {
    when (any) {
        is String -> {
            this.put(name, any)
        }
        is Float -> {
            this.put(name, any)
        }
        is Double -> {
            this.put(name, any)
        }
        is Int -> {
            this.put(name, any)
        }
        is Long -> {
            this.put(name, any)
        }
        is Boolean -> {
            this.put(name, any)
        }
    }
    return this
}