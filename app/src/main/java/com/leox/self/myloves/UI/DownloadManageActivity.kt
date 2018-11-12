package com.leox.self.myloves.UI

import android.os.Bundle
import com.leox.self.myloves.R
import kotlinx.android.synthetic.main.toolbar.*

class DownloadManageActivity:BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_manage)
        toolbar_back.setOnClickListener {
            finish()
        }
        toolbar_title.text = "下载管理"
    }
}