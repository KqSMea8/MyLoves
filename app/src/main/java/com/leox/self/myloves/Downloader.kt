package com.leox.self.myloves

import android.content.Intent
import android.util.Log
import com.leox.self.myloves.data.TaskInfo
import com.leox.self.myloves.db.LocalDatabase
import com.leox.self.myloves.db.TableTask
import com.leox.self.myloves.services.TaskStatusObserverService
import com.xunlei.downloadlib.XLDownloadManager
import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.XLTaskInfo
import java.io.File
import java.sql.SQLException

object Downloader {
    var directoryPath: String

    init {
        XLTaskHelper.init(MyApp.instance)
        val dir = File(android.os.Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID)
        if (dir.exists()) {
            if (!dir.isDirectory) {
                dir.delete()
                dir.mkdirs()
            }
        } else {
            dir.mkdirs()
        }
        directoryPath = dir.absolutePath
    }

    fun obtainPath(url: String): String {
        return File(directoryPath, getFileName(url)).absolutePath
    }

    fun obtainPlayUrl(url: String): String {
        val loclUrl = XLTaskHelper.instance(MyApp.instance).getLoclUrl(obtainPath(url))
        Log.i("DownLoader", "playUrl:$loclUrl")
        return loclUrl
    }

    fun getFileName(url: String): String {
        return XLTaskHelper.instance(MyApp.instance).getFileName(url)
    }

    fun addTask(url: String): Long {
        val taskAdded = TableTask.isTaskAdded(url)
        return when {
            taskAdded == -1L -> {
                //create task
                val taskId = if (url.startsWith("magnet:?")) {
                    XLTaskHelper.instance(MyApp.instance).addMagentTask(url, directoryPath, getFileName(url))
                } else if (url.startsWith("thunder://") || url.startsWith("ftp://") || url.startsWith("ed2k://") || url.startsWith("http")) {
                    //thunder:// ftp:// ed2k:// http:// https://
                    XLTaskHelper.instance(MyApp.instance).addThunderTask(url, directoryPath, getFileName(url))
                } else {
                    XLTaskHelper.instance(MyApp.instance).addTorrentTask(url, directoryPath, intArrayOf(0))
                }
                addTaskInfoToDB(url, taskId)
                startObserverService(taskId)
                taskId
            }
            TableTask.isTaskCompleted(url) -> taskAdded
            else -> {
                //startTask
                XLDownloadManager.getInstance().setDownloadTaskOrigin(taskAdded, "out_app/out_app_paste")
                XLDownloadManager.getInstance().setOriginUserAgent(taskAdded, "AndroidDownloadManager/4.4.4 (Linux; U; Android 4.4.4; Build/KTU84Q)")
                XLDownloadManager.getInstance().startTask(taskAdded, false)
                XLDownloadManager.getInstance().setTaskLxState(taskAdded, 0, 1)
                XLDownloadManager.getInstance().startDcdn(taskAdded, 0, "", "", "")
                taskAdded
            }
        }
    }

    private fun startObserverService(taskId: Long) {
        val intent = Intent(MyApp.instance, TaskStatusObserverService::class.java)
        intent.putExtra(TaskStatusObserverService.TASK_ID, taskId)
        MyApp.instance.startService(intent)
    }

    private fun addTaskInfoToDB(url: String, taskId: Long) {
        val taskInfo = TaskInfo(getFileName(url), url, taskId)
        TableTask.addTask(taskInfo)
    }

    fun getTaskInfo(taskId: Long): XLTaskInfo {
        return XLTaskHelper.instance(MyApp.instance).getTaskInfo(taskId)
    }


}