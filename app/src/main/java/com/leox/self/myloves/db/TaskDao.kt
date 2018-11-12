package com.leox.self.myloves.db

import com.leox.self.myloves.data.TaskInfo
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
}