package com.leox.self.myloves.db

import com.google.gson.Gson
import java.io.Serializable

/**
 * 基础数据
 */
class BaseDao {
    companion object {
        val TABLE_NAME = "table_base"
        val ID = "_id"
        val LOVES = "_loves"
        val VERSION = "version"
    }

    @Synchronized
    fun init() {
        val writableDatabase = LocalDatabase.writableDatabase
        val resultCursor = writableDatabase.rawQuery("Select * from sqlite_master where type = \"table\" and name= $TABLE_NAME;", null)
        if (resultCursor?.moveToNext() != true) {
            resultCursor.close()
            writableDatabase.execSQL(
                    "create table $TABLE_NAME($ID INTEGER primary key,$LOVES varchar(200),$VERSION INTEGER )"
            )
        }else{
            resultCursor?.close()
        }
    }

    @Synchronized
    fun getInfos(): DefaultBean? {
        val result = LocalDatabase.readableDatabase.rawQuery("select * from $TABLE_NAME", null)
        return if (result?.moveToNext() != false) {
            DefaultBean(
                    Gson().fromJson(result.getString(result.getColumnIndex(LOVES)), ArrayList::class.java) as ArrayList<String>,
                    result.getInt(result.getColumnIndex(VERSION))
            )
        } else {
            null
        }
    }

    @Synchronized
    fun updateInfos(data: DefaultBean, oldVersion: Int) {
        val writableDatabase = LocalDatabase.writableDatabase
        writableDatabase.execSQL("update $TABLE_NAME set $LOVES = ${Gson().toJson(data.loves)} $VERSION = ${data.version} where $VERSION = $oldVersion")
    }

    data class DefaultBean(val loves: ArrayList<String>, val version: Int) : Serializable
}