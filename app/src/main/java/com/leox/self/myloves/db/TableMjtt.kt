package com.leox.self.myloves.db

import android.util.Log
import com.google.gson.Gson
import com.leox.self.myloves.MyApp
import java.lang.Exception
import java.nio.charset.Charset

object TableMjtt {
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
    fun getShowList(limit: Int, startIndex: Int): ArrayList<ShowBean> {
        val arrayList = ArrayList<ShowBean>()
        try {
            val readableDatabase = LocalDatabase(MyApp.instance).readableDatabase
            val rawQuery = readableDatabase.rawQuery("select * from ${NAME} order by m_id ASC limit ?,?", arrayOf(Integer.toString(startIndex), Integer.toString(limit)))
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
                        rawQuery.getString(rawQuery.getColumnIndex(SHOW_DESC))
                ))
            }
            rawQuery.close()
        } catch (e: Exception) {
            Log.e("DB_ERROR", "", e)
        }
        return arrayList
    }

    data class ShowBean(override val type: String, override val name: String, override val country: String, override val actors: String, val ed2kUrls: List<String>?, val magnetUrls: List<String>?, override val duration: String, override val director: String, override val placard: String, override val desc: String) : DataBase(
            type, name, country, actors, duration, director, placard, desc
    )
}