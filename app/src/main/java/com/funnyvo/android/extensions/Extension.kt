package com.funnyvo.android.extensions

import com.google.gson.Gson

inline fun <reified T>fromJson(json: String): T {
    return Gson().fromJson<T>(json, T::class.java)
}