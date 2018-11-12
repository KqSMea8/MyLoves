package com.leox.self.myloves.db

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.leox.self.myloves.MyApp

object LocalDatabase : SQLiteOpenHelper(MyApp.instance, "ldb", null, 1, {
    if (it.isOpen) {
        it.close()
    }
    Toast.makeText(MyApp.instance,"execute sql error",Toast.LENGTH_LONG).show()
}) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(TaskDao.CREATE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}