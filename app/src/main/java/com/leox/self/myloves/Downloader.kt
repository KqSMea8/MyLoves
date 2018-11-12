package com.leox.self.myloves

import android.content.Intent
import android.util.Log
import com.leox.self.myloves.data.TaskInfo
import com.leox.self.myloves.db.TaskDao
import com.leox.self.myloves.services.TaskStatusObserverService
import com.xunlei.downloadlib.XLDownloadManager
import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.XLTaskInfo
import java.io.File

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
        val loclUrl = XLTaskHelper.instance().getLoclUrl(obtainPath(url))
        Log.i("DownLoader", "playUrl:$loclUrl")
        return loclUrl
    }

    fun getFileName(url: String): String {
        return XLTaskHelper.instance().getFileName(url)
    }

    fun addTask(url: String): Long {
        val taskAdded = TaskDao.isTaskAdded(url)
        return when {
            taskAdded == -1L -> {
                //create task
                val taskId = if (url.startsWith("magnet:?")) {
                    XLTaskHelper.instance().addMagnetTask(url, directoryPath, getFileName(url))
                } else if (url.startsWith("thunder://") || url.startsWith("ftp://") || url.startsWith("ed2k://") || url.startsWith("http")) {
                    //thunder:// ftp:// ed2k:// http:// https://
                    XLTaskHelper.instance().addThunderTask(url, directoryPath, getFileName(url))
                } else {
                    XLTaskHelper.instance().addTorrentTask(url, directoryPath, listOf())
                }
                addTaskInfoToDB(url, taskId)
                startObserverService(taskId)
                taskId
            }
            TaskDao.isTaskCompleted(url) -> taskAdded
            else -> {
                //startTask
                XLDownloadManager.getInstance().setDownloadTaskOrigin(taskAdded, "out_app/out_app_paste")
                XLDownloadManager.getInstance().setOriginUserAgent(taskAdded, "AndroidDownloadManager/4.4.4 (Linux; U; Android 4.4.4; Build/KTU84Q)")
                XLDownloadManager.getInstance().startTask(taskAdded, false)
                XLDownloadManager.getInstance().setTaskLxState(taskAdded, 0, 1)
                XLDownloadManager.getInstance().startDcdn(taskAdded, 0, "", "", "")
                startObserverService(taskAdded)
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
        TaskDao.addTask(taskInfo)
    }

    fun getTaskInfo(taskId: Long): XLTaskInfo {
        return XLTaskHelper.instance().getTaskInfo(taskId)
    }


}