package com.sample.mix.demo.utils

import com.google.gson.Gson
import org.json.JSONObject
import java.lang.reflect.Type

object JsonUtils {

    val gson = Gson()

    @JvmStatic
    fun <T> fromJson(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)

    @JvmStatic
    fun <T> toJson(obj: T): String = gson.toJson(obj)

    @JvmStatic
    fun <T> fromJson(jsonObj:JSONObject,clazz:Class<T>):T = gson.fromJson(jsonObj.toString(),clazz)

    @JvmStatic
    inline fun <reified T> fromJson(json: String): T = gson.fromJson(json, T::class.java)

    @JvmStatic
    inline fun <reified T> fromJson(jsonObj:JSONObject):T = gson.fromJson(jsonObj.toString(),T::class.java)

    @JvmStatic
    fun  <T> fromJson(json: String,typeOfT:Type):T = gson.fromJson(json,typeOfT)
}