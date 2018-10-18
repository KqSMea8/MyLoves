package com.leox.self.myloves.UI

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
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
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_play)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) {
            requestPermissions(Array<String>(1, init = {
                Manifest.permission.READ_PHONE_STATE
            }), 2012)
        }
        val url = "ed2k://|file|%E4%B8%AD%E9%97%B4%E4%BA%BA%E5%85%88%E7%94%9F.Mr.Inbetween.S01E04.720p.Classic%E5%AD%97%E5%B9%95%E7%BB%84.mp4|335391783|48f7d147dafdb4d7e3c4eba481556ef3|h=hvc6eic66benmqkkved6rdyd5tqaqxxk|/"
        taskId = Downloader.addTask(url)
        Downloader.addTask("ed2k://|file|%E4%B8%AD%E9%97%B4%E4%BA%BA%E5%85%88%E7%94%9F.mr.inbetween.s01e06.END.720p.Classic%E5%AD%97%E5%B9%95%E7%BB%84.mp4|231854269|51834406dadbbc709cec27ea178f65df|h=ey33thng43chunohvgvhz2tthojz4jyp|/")
        TaskStatusObserverService.addListener(object : ProgressListener {
            override fun onStart(taskId: Long, downloaded: Long, sum: Long) {

            }

            override fun onCompleted(taskId: Long) {
            }

            override fun onFailed(taskId: Long) {
            }

            override fun onProgress(taskId: Long, downloaded: Long, sum: Long) {
                if (taskId == this@PlayActivity.taskId) {
                    runOnUiThread {
                        tvDownloadStatus.text = tvDownloadStatus.text.subSequence(0, tvDownloadStatus.text.indexOf(":", 0, true) + 1).toString() + DataConvertUtils.convertFileSize(downloaded)
                        tvFileSize.text = tvFileSize.text.subSequence(0, tvFileSize.text.indexOf(":") + 1).toString() + DataConvertUtils.convertFileSize(sum)
                    }
                }
            }
        })
        playVideo(Downloader.obtainPlayUrl(url))
    }
    private fun playVideo(obtainPlayUrl: String) {
        val jzDataSource = JZDataSource(obtainPlayUrl)
        video_std.setUp(jzDataSource, JzvdStd.SCREEN_WINDOW_NORMAL)
        Glide.with(this).load("http://p.qpic.cn/videoyun/0/2449_43b6f696980311e59ed467f22794e792_1/640").into(video_std.thumbImageView)
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