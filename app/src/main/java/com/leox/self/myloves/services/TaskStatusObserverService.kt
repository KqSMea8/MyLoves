package com.leox.self.myloves.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import com.leox.self.myloves.Downloader
import com.leox.self.myloves.MyApp
import com.leox.self.myloves.db.FieldStatus
import com.leox.self.myloves.db.TaskDao
import com.xunlei.downloadlib.parameter.XLTaskInfo

class TaskStatusObserverService : Service() {
    private val binder: InnerBinder = InnerBinder()

    companion object {
        val TASK_UPDATE = 0x2011
        val TASK_START = 0x1001
        val TASK_ID = "TASK_ID"
        val ACTION_REMOVE_MESSAGE = "com.leox.self.myloves.action_remove_message"
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

        fun removeMessage(taskId: Long) {
            val intent = Intent(ACTION_REMOVE_MESSAGE)
            intent.putExtra(TASK_ID, taskId)
            MyApp.instance.sendBroadcast(intent)
        }
    }

    private val workHandler: Handler by lazy {
        val thread = HandlerThread("workHandlerThread")
        thread.start()
        object : Handler(thread.looper) {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                when (msg?.what) {
                    TASK_UPDATE -> {
                        Log.i("TaskInfoUpdate","update")
                        val taskId = msg.obj as Long
                        val taskInfo = Downloader.getTaskInfo(taskId)
                        if (taskInfo.mDownloadSize == taskInfo.mFileSize && taskInfo.mFileSize > 0) {
                            listeners.forEach {
                                if (taskInfo.mFileName?.endsWith(".bt") == true) {
                                    it.onCompleted(taskId, true)
                                } else {
                                    it.onCompleted(taskId, false)
                                }
                                TaskDao.updateDBTaskStatus(taskId, FieldStatus.TRUE, FieldStatus.IGNORE)
                            }
                        } else {
                            if (msg.arg1 == TASK_START) {
                                listeners.forEach {
                                    it.onStart(taskId, taskInfo)
                                }
                            } else {
                                listeners.forEach {
                                    it.onProgress(taskId, taskInfo)
                                }
                            }
                            val obtainMessage = obtainMessage()
                            obtainMessage.what = TASK_UPDATE
                            obtainMessage.obj = taskId
                            sendMessageDelayed(obtainMessage, 1000)
                        }
                    }
                    else -> {
                        Log.i("TaskInfoUpdate","update else")
                    }
                }
            }
        }
    }

    private val receiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_REMOVE_MESSAGE -> {
                        val longExtra = intent.getLongExtra(TASK_ID, -1)
                        workHandler.removeMessages(TASK_UPDATE, longExtra)
                    }
                    else -> {
                    }
                }
            }

        }
    }

    private val filters: IntentFilter by lazy {
        val result = IntentFilter()
        result.addAction(ACTION_REMOVE_MESSAGE)
        result
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
        registerReceiver(
                receiver,
                filters
        )
        return Service.START_STICKY_COMPATIBILITY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class InnerBinder : Binder() {
        //
    }

}

interface ProgressListener {
    fun onProgress(taskId: Long, downloaded: XLTaskInfo)
    fun onStart(taskId: Long, downloaded: XLTaskInfo)
    fun onCompleted(taskId: Long, isBt: Boolean)
    fun onFailed(taskId: Long)
}