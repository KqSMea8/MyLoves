package com.leox.self.myloves.UI

import android.os.Bundle
import android.util.Log
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.bumptech.glide.Glide
import com.leox.self.myloves.Downloader
import com.leox.self.myloves.R
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.item_download_manage.view.*

class PlayActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        val url = intent.getStringExtra("url")
        val name = intent.getStringExtra("name")
        val image = intent.getStringExtra("image")
        Log.i(this.javaClass.simpleName, url)
        video_std.backButton.setOnClickListener {
            finish()
        }
        playVideo(Downloader.obtainPlayUrl(url), name, image)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun playVideo(obtainPlayUrl: String, name: String, image: String?) {
        val jzDataSource = JZDataSource(obtainPlayUrl, name)
        video_std.setUp(jzDataSource, JzvdStd.SCREEN_WINDOW_FULLSCREEN)
        if (image != null) {
            Glide.with(this).load(image).into(video_std.thumbImageView)
        }
        video_std.startVideo()
    }


    override fun onBackPressed() {
        Jzvd.backPress()
        finish()
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