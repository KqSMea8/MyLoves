package com.leox.self.myloves.utils

import android.content.Context
import android.content.SharedPreferences

object SpUtils{
    val FILE_NAME = "basic_config"
    fun getInstance(ctx:Context): SharedPreferences {
        return ctx.applicationContext.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE)
    }
}