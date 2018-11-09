package com.leox.self.myloves.UI

import android.os.Bundle
import android.util.Log
import android.view.View
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.bumptech.glide.Glide
import com.leox.self.myloves.Downloader
import com.leox.self.myloves.R
import com.leox.self.myloves.services.ProgressListener
import com.leox.self.myloves.services.TaskStatusObserverService
import com.leox.self.myloves.utils.DataConvertUtils
import kotlinx.android.synthetic.main.activity_play.*

class PlayActivity : BaseActivity(){
    var taskId: Long = 0L
    private val progressListener = object : ProgressListener {
        override fun onStart(taskId: Long, downloaded: Long, sum: Long) {
            if (taskId == this@PlayActivity.taskId) {
                runOnUiThread {
                    tvDownloadStatus.visibility = View.VISIBLE
                    tvFileSize.visibility = View.VISIBLE
                    tvDownloadStatus.text = tvDownloadStatus.text.subSequence(0, tvDownloadStatus.text.indexOf(":", 0, true) + 1).toString() + DataConvertUtils.convertFileSize(downloaded)
                    tvFileSize.text = tvFileSize.text.subSequence(0, tvFileSize.text.indexOf(":") + 1).toString() + DataConvertUtils.convertFileSize(sum)
                }
            }
        }

        override fun onCompleted(taskId: Long) {
            if (taskId == this@PlayActivity.taskId) {
                runOnUiThread {
                    tvDownloadStatus.visibility = View.VISIBLE
                    tvFileSize.visibility = View.VISIBLE
                }
            }
        }

        override fun onFailed(taskId: Long) {
        }

        override fun onProgress(taskId: Long, downloaded: Long, sum: Long) {
            if (taskId == this@PlayActivity.taskId) {
                runOnUiThread {
                    tvDownloadStatus.visibility = View.VISIBLE
                    tvFileSize.visibility = View.VISIBLE
                    tvDownloadStatus.text = tvDownloadStatus.text.subSequence(0, tvDownloadStatus.text.indexOf(":", 0, true) + 1).toString() + DataConvertUtils.convertFileSize(downloaded)
                    tvFileSize.text = tvFileSize.text.subSequence(0, tvFileSize.text.indexOf(":") + 1).toString() + DataConvertUtils.convertFileSize(sum)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        val url = intent.getStringExtra("url")
        val name = intent.getStringExtra("name")
        val image = intent.getStringExtra("image")
        Log.i(this.javaClass.simpleName,url)
        taskId = Downloader.addTask(url)
       // Downloader.addTask(url)
        playVideo(Downloader.obtainPlayUrl(url),name,image)
    }

    override fun onStart() {
        super.onStart()
        TaskStatusObserverService.addListener(progressListener)
    }

    override fun onStop() {
        super.onStop()
        TaskStatusObserverService.removeListener(progressListener)
    }
    private fun playVideo(obtainPlayUrl: String, name: String, image: String) {
        val jzDataSource = JZDataSource(obtainPlayUrl,name)
        video_std.setUp(jzDataSource, JzvdStd.SCREEN_WINDOW_NORMAL)
        Glide.with(this).load(image).into(video_std.thumbImageView)
    }


    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}