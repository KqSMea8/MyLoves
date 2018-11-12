package com.leox.self.myloves.db

import android.util.Log
import java.lang.Exception

object MjttDao {
    val NAME = "mjtt"
    val SHOW_ACTORS = "m_actors"
    val SHOW_DIRECTOR = "m_director"
    val SHOW_TYPE = "m_type"
    val SHOW_NAME = "m_name"
    val SHOW_COUNTRY = "m_country"
    val SHOW_DURATION = "m_duration"
    val SHOW_PLACARD = "m_placard"
    val SHOW_DESC = "m_desc"
    val SHOW_ED2K_URLS = "m_ed2k_url"
    val SHOW_MAGNET_URL = "m_magnet_url"
    val SHOW_TRANSNAME = "m_trans_name"
    val SHOW_DECADE = "m_decade"
    fun getShowList(limit: Int, startIndex: Int): ArrayList<ShowBean> {
        val arrayList = ArrayList<ShowBean>()
        try {
            val readableDatabase = LocalDatabase.readableDatabase
            val resultCursor = readableDatabase.rawQuery("Select * from sqlite_master where type = \"table\" and name= \"${NAME}\";", null)
            if (resultCursor?.moveToNext() == true) {
                resultCursor.close()
                val rawQuery = readableDatabase.rawQuery("select * from ${NAME} order by m_id DESC limit ?,?", arrayOf(Integer.toString(startIndex), Integer.toString(limit)))
                while (rawQuery.moveToNext()) {
                    arrayList.add(ShowBean(
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_TYPE)),
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_NAME)),
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_COUNTRY)),
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_ACTORS)),
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_ED2K_URLS)).split(","),
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_MAGNET_URL)).split(","),
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_DURATION)),
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_DIRECTOR)),
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_PLACARD)).split(",")[0],
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_DESC)),
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_TRANSNAME)),
                            rawQuery.getString(rawQuery.getColumnIndex(SHOW_DECADE))
                    ))
                }
                rawQuery.close()
            } else {
                resultCursor?.close()
            }
        } catch (e: Exception) {
            Log.e("DB_ERROR", "", e)
        }
        return arrayList
    }

    class ShowBean(type: String, name: String,
                   country: String, actors: String,
                   val ed2kUrls: List<String>?, val magnetUrls: List<String>?,
                   duration: String, director: String,
                   placard: String, desc: String,
                   transName: String, decade: String) : DataBase(
            type, name, country, actors, duration, director, placard, desc, transName, decade
    )
}