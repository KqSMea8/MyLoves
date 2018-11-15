package com.leox.self.myloves.UI

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.leox.self.myloves.Downloader
import com.leox.self.myloves.R
import com.leox.self.myloves.db.TaskDao
import com.leox.self.myloves.services.ProgressListener
import com.leox.self.myloves.services.TaskStatusObserverService
import com.leox.self.myloves.utils.DataConvertUtils
import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.XLTaskInfo
import kotlinx.android.synthetic.main.activity_download_manage.*
import kotlinx.android.synthetic.main.item_download_manage.view.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

class DownloadManageActivity : BaseActivity() {
    private val downloadingDatas: ArrayList<TaskDao.TaskInfo> = arrayListOf()

    private val downloadingCompletedDatas: ArrayList<TaskDao.TaskInfo> = arrayListOf()


    private val mListener: ProgressListener by lazy {
        object : ProgressListener {
            override fun onProgress(taskId: Long, downloaded: XLTaskInfo) {
                runOnUiThread {
                    (downloading.adapter as DownloadAdapter).onDownLoading(taskId, downloaded)
                }
            }

            override fun onStart(taskId: Long, downloaded: XLTaskInfo) {
                runOnUiThread {
                    (downloading.adapter as DownloadAdapter).onDownLoading(taskId, downloaded)
                }
            }

            override fun onCompleted(taskId: Long, isBt: Boolean) {
                runOnUiThread {
                    if (isBt) {
                        showBtDownloadChooser(taskId)
                    } else {
                        collectDatas()
                    }
                }
            }

            override fun onFailed(taskId: Long) {
                runOnUiThread {
                    Toast.makeText(this@DownloadManageActivity, "下载失败", Toast.LENGTH_SHORT).show()
                    (downloading.adapter as DownloadAdapter).onFailed(taskId)
                }
            }

        }
    }

    private fun showBtDownloadChooser(taskId: Long) {
        Downloader.getBTInfo(taskId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_manage)
        toolbar_back.setOnClickListener {
            finish()
        }
        toolbar_title.text = "下载管理"
        downloading.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        downloading.swapAdapter(DownloadAdapter(false, downloadingDatas), true)
        downloadingCompleted.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        downloadingCompleted.swapAdapter(DownloadAdapter(true, downloadingCompletedDatas), true)
        TaskStatusObserverService.addListener(mListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        TaskStatusObserverService.removeListener(mListener)
    }

    override fun onStart() {
        super.onStart()
        collectDatas()
    }

    private fun collectDatas() {
        Thread {
            val taskInfos = TaskDao.getTaskInfos()
            downloadingCompletedDatas.clear()
            downloadingCompletedDatas.addAll(taskInfos.second)
            downloadingDatas.clear()
            downloadingDatas.addAll(taskInfos.first)
            runOnUiThread {
                downloadingCompleted.adapter.notifyDataSetChanged()
                downloading.adapter.notifyDataSetChanged()
            }
        }.start()
    }
}

class DownloadAdapter(val isFinished: Boolean, val datas: ArrayList<TaskDao.TaskInfo>) : RecyclerView.Adapter<DownloadHolder>() {
    val visualList: HashMap<Long, DownloadHolder> = hashMapOf()
    private val mListener: OnActionListener = object : OnActionListener {
        override fun onDelete(data: TaskDao.TaskInfo) {
            datas.remove(data)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadHolder {
        return DownloadHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_download_manage, parent, false), mListener)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: DownloadHolder, position: Int) {
        holder.inject(datas[position], isFinished)
        if (!isFinished) {
            visualList[holder.data!!.taskId] = holder
        }
    }

    override fun onViewRecycled(holder: DownloadHolder) {
        super.onViewRecycled(holder)
        if (!isFinished && holder.data != null) {
            visualList.remove(holder.data!!.taskId)
        }
    }

    override fun onViewDetachedFromWindow(holder: DownloadHolder) {
        super.onViewDetachedFromWindow(holder)
        if (!isFinished && holder.data != null) {
            visualList.remove(holder.data!!.taskId)
        }
    }

    override fun getItemId(position: Int): Long {
        return datas[position].taskId
    }

    fun onDownLoading(taskId: Long, downloaded: XLTaskInfo) {
        visualList[taskId]?.updateView(downloaded)
    }

    fun onFailed(taskId: Long) {
        visualList[taskId]?.onFail()
    }
}

interface OnActionListener {
    fun onDelete(data: TaskDao.TaskInfo)
}

class DownloadHolder(view: View, val onActionListener: OnActionListener) : RecyclerView.ViewHolder(view) {
    var data: TaskDao.TaskInfo? = null
    fun inject(taskBean: TaskDao.TaskInfo, finished: Boolean) {
        data = taskBean
        val taskInfo = XLTaskHelper.instance().getTaskInfo(data!!.taskId)
        itemView.title.text = data?.fileName
        itemView.speed.text = if (finished) "完成" else "${DataConvertUtils.convertFileSize(taskInfo.mDownloadSpeed)}/S"
        itemView.sumSize.text = if (finished) "完成" else "${DataConvertUtils.convertFileSize(taskInfo.mDownloadSize)}/ ${DataConvertUtils.convertFileSize(taskInfo.mFileSize)}"
        itemView.play.setOnClickListener {
            val playIntent = Intent(itemView.context, PlayActivity::class.java)
            playIntent.putExtra("url", data?.url)
            playIntent.putExtra("name", data?.fileName)
//            playIntent.putExtra("image", null)
            itemView.context.startActivity(playIntent)
        }
        if (finished) {
            itemView.start.visibility = View.GONE
        } else {
            itemView.start.visibility = View.VISIBLE

            itemView.start.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    if (data != null && !data!!.isCompleted) {
                        Downloader.startTask(data!!.taskId)
                        Handler(Looper.getMainLooper())
                                .postDelayed({
                                    itemView.speed.text = "开始"
                                }, 1000)

                    }
                } else {
                    if (data != null) {
                        Downloader.pauseDownload(data!!.taskId)
                        Handler(Looper.getMainLooper()).postDelayed({
                            itemView.speed.text = "暂停"
                        }, 5000)
                    }
                }
            }
        }
        itemView.delete.setOnClickListener {
            if (data != null) {
                AlertDialog.Builder(itemView.context)
                        .setMessage("删除后不可恢复，确定要删除吗？")
                        .setPositiveButton("确定") { _, _ ->
                            Downloader.deleteDownload(data!!.taskId)
                            TaskDao.deleteTask(data!!.taskId)
                            onActionListener.onDelete(data!!)
                        }
                        .setNegativeButton("取消") { dialog, _ ->
                            dialog?.dismiss()
                        }
                        .show()
            }
        }
    }

    fun updateView(downloaded: XLTaskInfo) {
        Log.i(this.javaClass.simpleName, "update view")
        itemView.speed.text = DataConvertUtils.convertFileSize(downloaded.mDownloadSpeed)
        itemView.sumSize.text = DataConvertUtils.convertFileSize(downloaded.mDownloadSize) + "/" + DataConvertUtils.convertFileSize(downloaded.mFileSize)
    }

    fun onFail() {
        itemView.speed.text = "失败"
    }

}