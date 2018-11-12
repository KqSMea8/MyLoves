package com.leox.self.myloves.services

import android.app.Service
import android.content.Intent
import android.os.*
import com.leox.self.myloves.Downloader
import com.leox.self.myloves.db.FieldStatus
import com.leox.self.myloves.db.TaskDao

class TaskStatusObserverService : Service() {
    private val binder: InnerBinder = InnerBinder()

    companion object {
        val TASK_UPDATE = 0x2011
        val TASK_START = 0x1001
        val TASK_ID = "TASK_ID"
        private val listeners: ArrayList<ProgressListener> by lazy {
            ArrayList<ProgressListener>()
        }

        fun addListener(listener: ProgressListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: ProgressListener) {
            listeners.remove(listener)
        }

        fun clearListener() {
            listeners.clear()
        }

        private val workHandler: Handler by lazy {
            val thread = HandlerThread("workHandlerThread")
            thread.start()
            object : Handler(thread.looper) {
                override fun handleMessage(msg: Message?) {
                    super.handleMessage(msg)
                    when (msg?.what) {
                        TASK_UPDATE -> {
                            val taskId = msg.obj as Long
                            val taskInfo = Downloader.getTaskInfo(taskId)
                            if (taskInfo.mDownloadSize == taskInfo.mFileSize) {
                                listeners.forEach {
                                    it.onCompleted(taskId)
                                    TaskDao.updateDBTaskStatus(taskId,FieldStatus.TRUE, FieldStatus.IGNORE)
                                }
                            } else {
                                if (msg.arg1 == TASK_START) {
                                    listeners.forEach {
                                        it.onStart(taskId, taskInfo.mDownloadSize, taskInfo.mFileSize)
                                    }
                                } else {
                                    listeners.forEach {
                                        it.onProgress(taskId, taskInfo.mDownloadSize, taskInfo.mFileSize)
                                    }
                                }
                                val obtainMessage = obtainMessage()
                                obtainMessage.what = TASK_UPDATE
                                obtainMessage.obj = taskId
                                sendMessageDelayed(obtainMessage, 1000)
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val get = intent?.getLongExtra(TASK_ID, -1L)
        if (get != -1L) {
            val obtainMessage = workHandler.obtainMessage()
            obtainMessage.what = TASK_UPDATE
            obtainMessage.obj = get
            obtainMessage.arg1 = TASK_START
            workHandler.sendMessage(obtainMessage)
        }
        return Service.START_STICKY_COMPATIBILITY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class InnerBinder : Binder() {
        //
    }

}

interface ProgressListener {
    fun onProgress(taskId: Long, downloaded: Long, sum: Long)
    fun onStart(taskId: Long, downloaded: Long, sum: Long)
    fun onCompleted(taskId: Long)
    fun onFailed(taskId: Long)
}