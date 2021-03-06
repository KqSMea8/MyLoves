package com.leox.self.myloves.db

import android.util.Log
import java.lang.Exception

object DyttDao {
    val NAME = "lastest_moive"
    val MOVIE_ACTORS = "m_actors"
    val MOVIE_DIRECTOR = "m_director"
    val MOVIE_FTPURL = "m_ftp_url"
    val MOVIE_TYPE = "m_type"
    val MOVIE_NAME = "m_name"
    val MOVIE_COUNTRY = "m_country"
    val MOVIE_LANGUAGE = "m_language"
    val MOVIE_IMDBScore = "m_IMDB_score"
    val MOVIE_SCREENSHOT = "m_screenshot"
    val MOVIE_DYTT8URL = "m_dytt8_url"
    val MOVIE_DURATION = "m_duration"
    val MOVIE_PLACARD = "m_placard"
    val MOVIE_DESC = "m_desc"
    val MOVIE_TRANSNAME = "m_trans_name"
    val MOVIE_DECADE = "m_decade"
    /*
    Create Table lastest_moive (
            'm_id' INTEGER PRIMARY KEY,
            'm_type' varchar(100),
            'm_trans_name' varchar(200),
            'm_name' varchar(100),
            'm_decade' varchar(30),
            'm_country' varchar(30),
            'm_level' varchar(100),
            'm_language' varchar(30),
            'm_subtitles' varchar(100),
            'm_publish' varchar(30),
            'm_IMDB_score' varchar(50),
            'm_dou_ban_score' varchar(50),
            'm_format' varchar(20),
            'm_resolution' varchar(20),
            'm_size' varchar(10),
            'm_duration' varchar(10),
            'm_director' varchar(50),
            'm_actors' varchar(1000),
            'm_placard' varchar(200),
            'm_screenshot' varchar(200),
            'm_ftp_url' varchar(200),
            'm_dytt8_url' varchar(200)
        );
     */

    fun getMovieList(limit: Int, startIndex: Int): ArrayList<MovieBean> {
        val arrayList = ArrayList<MovieBean>()
        try {
            val readableDatabase = LocalDatabase.readableDatabase
            val resultCursor = readableDatabase.rawQuery("Select * from sqlite_master where type = \"table\" and name= \"$NAME\";", null)
            if (resultCursor?.moveToNext() == true) {
                resultCursor.close()
                val rawQuery = readableDatabase.rawQuery("select * from ${NAME} order by m_id DESC limit ?,?", arrayOf(Integer.toString(startIndex), Integer.toString(limit)))
                while (rawQuery.moveToNext()) {
                    arrayList.add(MovieBean(
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_TYPE)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_NAME)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_COUNTRY)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_LANGUAGE)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_IMDBScore)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_ACTORS)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_SCREENSHOT)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_FTPURL)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_DYTT8URL)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_DURATION)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_DIRECTOR)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_PLACARD)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_DESC)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_TRANSNAME)),
                            rawQuery.getString(rawQuery.getColumnIndex(MOVIE_DECADE))
                    ))
                }
                rawQuery.close()
            }else{
                resultCursor?.close()
            }
        } catch (e: Exception) {
            Log.e("DB_ERROR", "", e)
        }
        return arrayList
    }

    class MovieBean(type: String, name: String, country: String, val language: String?
                    , val IMDBScore: String?, actors: String, val screenshot: String?,
                    val ftpUrl: String, val dytt8Url: String?, duration: String,
                    director: String, placard: String,
                    desc: String, transName: String, decade: String) : DataBase(
            type, name, country, actors, duration, director, placard, desc, transName, decade
    )
}