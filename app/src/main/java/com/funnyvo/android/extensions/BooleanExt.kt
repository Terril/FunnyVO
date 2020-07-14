package com.funnyvo.android.extensions

fun Boolean?.filterNull(defaultValue: Boolean = false): Boolean {
    return this ?: defaultValue
}