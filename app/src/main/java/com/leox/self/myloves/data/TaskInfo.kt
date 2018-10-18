package com.leox.self.myloves.data

import android.content.ContentValues
import com.leox.self.myloves.db.TableTask
import java.io.Serializable

data class TaskInfo(val fileName:String,val url:String,val taskId:Long,var isCompleted:Boolean,var isPlayed:Boolean, val createTime:Long):Serializable{
    constructor(fileName: String,url: String,taskId: Long):this(fileName,url,taskId,false,false,System.currentTimeMillis())
    fun convertBeanToValuesOfTask(): ContentValues {
        val values = ContentValues()
        values.put(TableTask.TASK_ID,taskId)
        values.put(TableTask.TASK_URL,url)
        values.put(TableTask.TASK_ISCOMPLETED,isCompleted)
        values.put(TableTask.TASK_ISPLAYED,isPlayed)
        values.put(TableTask.TASK_FILENAME,fileName)
        values.put(TableTask.TASK_CREATETIME,createTime)
        return values
    }

}