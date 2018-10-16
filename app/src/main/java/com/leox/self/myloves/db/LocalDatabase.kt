package com.leox.self.myloves.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class LocalDatabase(ctx: Context) : SQLiteOpenHelper(ctx, "ldb", null, 1, {
    if (it.isOpen) {
        it.close()
    }
    Toast.makeText(ctx,"execute sql error",Toast.LENGTH_LONG).show()
}) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(TableTask.CREATE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}