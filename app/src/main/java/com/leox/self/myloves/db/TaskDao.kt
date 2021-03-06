package com.leox.self.myloves.db

import android.content.ContentValues
import java.io.Serializable
import java.sql.SQLException

object TaskDao {
    val NAME = "tasks_info"
    val TASK_ID = "taskId"
    val TASK_URL = "url"
    val TASK_FILENAME = "fileName"
    val TASK_ISCOMPLETED = "isCompleted"
    val TASK_ISPLAYED = "isPlayed"
    val TASK_CREATETIME = "createTime"
    val CREATE_SQL = "create table if not exists $NAME($TASK_ID  integer not null, $TASK_URL  text  unique primary key,$TASK_ISCOMPLETED boolean not null,$TASK_ISPLAYED boolean not null,$TASK_FILENAME text not null,$TASK_CREATETIME long not null)"

    fun updateDBTaskStatus(taskId: Long, isCompleted: FieldStatus, isPlayed: FieldStatus) {
        val writableDatabase = LocalDatabase.writableDatabase
        val rawQuery = writableDatabase.rawQuery("select * from $NAME where $TASK_ID = ? order by $TASK_CREATETIME desc ", arrayOf("" + taskId))
        if (rawQuery != null) {
            if (rawQuery.moveToNext()) {
                val taskInfo = TaskInfo(rawQuery.getString(rawQuery.getColumnIndex(TASK_FILENAME)), rawQuery.getString(rawQuery.getColumnIndex(TASK_URL)),
                        rawQuery.getLong(rawQuery.getColumnIndex(TASK_ID)), rawQuery.getInt(rawQuery.getColumnIndex(TASK_ISCOMPLETED)) != 0, rawQuery.getInt(rawQuery.getColumnIndex(TASK_ISPLAYED)) != 0,
                        rawQuery.getLong(rawQuery.getColumnIndex(TASK_CREATETIME)))
//                rawQuery.close()
                when (isCompleted) {
                    FieldStatus.TRUE -> {
                        taskInfo.isCompleted = true
                    }
                    FieldStatus.FALSE -> {
                        taskInfo.isCompleted = false
                    }
                }
                when (isPlayed) {
                    FieldStatus.TRUE -> {
                        taskInfo.isPlayed = true
                    }
                    FieldStatus.FALSE -> {
                        taskInfo.isPlayed = false
                    }
                }
                val update = writableDatabase.update(NAME, taskInfo.convertBeanToValuesOfTask(), "$TASK_URL = ?", arrayOf(taskInfo.url))
                if (update <= 0) {
                    throw SQLException("update sql failed,affected raw = $update")
                }
            } else {
                rawQuery.close()
                throw SQLException("raw not found taskId = $taskId")
            }
        } else {
            throw SQLException("raw not found taskId = $taskId")
        }
    }

    fun addTask(taskInfo: TaskInfo) {
        val writableDatabase = LocalDatabase.writableDatabase
        val insert = writableDatabase.insert(TaskDao.NAME, null, taskInfo.convertBeanToValuesOfTask())
        if (insert == -1L) {
            throw SQLException("insert task data error")
        }
    }

    fun isTaskCompleted(url: String): Boolean {
        var result = false
        val readableDatabase = LocalDatabase.readableDatabase
        val rawQuery = readableDatabase.rawQuery("select * from $NAME where $TASK_URL = ?", arrayOf(url))
        if (rawQuery != null) {
            if (rawQuery.moveToNext()) {
                val int = rawQuery.getInt(rawQuery.getColumnIndex(TASK_ISCOMPLETED))
                result = int != 0
            }
            rawQuery.close()
        }
        return result
    }

    fun isTaskAdded(url: String): Long {
        var result = -1L
        val readableDatabase = LocalDatabase.readableDatabase
        val rawQuery = readableDatabase.rawQuery("select * from $NAME where $TASK_URL = ?", arrayOf(url))
        if (rawQuery != null) {
            if (rawQuery.moveToNext()) {
                val int = rawQuery.getLong(rawQuery.getColumnIndex(TASK_ID))
                result = int
            }
            rawQuery.close()
        }
        return result
    }

    fun getTaskInfos(): Pair<ArrayList<TaskInfo>, ArrayList<TaskInfo>> {
        val completedList = arrayListOf<TaskInfo>()
        val unCompletedList = arrayListOf<TaskInfo>()
        val rawQuery = LocalDatabase.readableDatabase.rawQuery("select * from $NAME", null)
        while (rawQuery?.moveToNext() == true) {
            val temp = TaskInfo(rawQuery.getString(rawQuery.getColumnIndex(TASK_FILENAME)),
                    rawQuery.getString(rawQuery.getColumnIndex(TASK_URL)),
                    rawQuery.getLong(rawQuery.getColumnIndex(TASK_ID)),
                    rawQuery.getInt(rawQuery.getColumnIndex(TASK_ISCOMPLETED)) != 0,
                    rawQuery.getInt(rawQuery.getColumnIndex(TASK_ISPLAYED)) != 0,
                    rawQuery.getLong(rawQuery.getColumnIndex(TASK_CREATETIME))
            )
            if (temp.isCompleted) {
                completedList.add(temp)
            } else {
                unCompletedList.add(temp)
            }
        }
        rawQuery.close()
        return Pair(unCompletedList, completedList)
    }

    fun deleteTask(taskId: Long):Int {
        return LocalDatabase.writableDatabase.delete(NAME, "$TASK_ID = ?", arrayOf("" + taskId))
    }

    fun removeUnCompletedTask(first: ArrayList<TaskInfo>) {
        val writableDatabase = LocalDatabase.writableDatabase
        first.forEach {
            writableDatabase.delete(NAME,"$TASK_URL = ?", arrayOf(it.url))
        }
    }


    data class TaskInfo(val fileName: String, val url: String, val taskId: Long, var isCompleted: Boolean, var isPlayed: Boolean, val createTime: Long) : Serializable {
        constructor(fileName: String, url: String, taskId: Long) : this(fileName, url, taskId, false, false, System.currentTimeMillis())

        fun convertBeanToValuesOfTask(): ContentValues {
            val values = ContentValues()
            values.put(TaskDao.TASK_ID, taskId)
            values.put(TaskDao.TASK_URL, url)
            values.put(TaskDao.TASK_ISCOMPLETED, isCompleted)
            values.put(TaskDao.TASK_ISPLAYED, isPlayed)
            values.put(TaskDao.TASK_FILENAME, fileName)
            values.put(TaskDao.TASK_CREATETIME, createTime)
            return values
        }

    }
}