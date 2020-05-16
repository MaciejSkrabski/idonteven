package com.example.gettext

import android.content.Context
import android.content.SharedPreferences


class preferences(context: Context) {
    val NightMode: String = "NightMode"
    val AppPreferences: String = "AppPreferences"
    val appSettingPrefs: SharedPreferences = context.getSharedPreferences(AppPreferences, 0)
    val sharedPrefsEdit: SharedPreferences.Editor = appSettingPrefs.edit()

    fun getMode():Boolean{
        return appSettingPrefs.getBoolean(NightMode,false)
    }
    fun setMode(value:Boolean){
        sharedPrefsEdit.putBoolean("NightMode",value)
        sharedPrefsEdit.apply()
    }
}