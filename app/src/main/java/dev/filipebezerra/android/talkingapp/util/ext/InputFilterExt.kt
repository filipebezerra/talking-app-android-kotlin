package dev.filipebezerra.android.talkingapp.util.ext

import android.text.InputFilter

fun Int.toLengthFilter() = InputFilter.LengthFilter(this)