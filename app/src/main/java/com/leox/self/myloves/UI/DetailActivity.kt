package com.leox.self.myloves.UI

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.leox.self.myloves.Downloader
import com.leox.self.myloves.R
import com.leox.self.myloves.db.DataBase
import com.leox.self.myloves.db.DyttDao
import com.leox.self.myloves.db.MjttDao
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.item_website.view.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.Serializable

class DetailActivity : BaseActivity() {
    private lateinit var data: DataBase
    private val urlList = ArrayList<ItemBean>()
    private var isShow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val stringExtra = intent.getStringExtra("data")
        if (TextUtils.isEmpty(stringExtra)) {
            Toast.makeText(this, "error happen", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        toolbar_back.setOnClickListener {
            finish()
        }
        isShow = intent.getBooleanExtra("isShow", false)
        if (isShow) {
            data = Gson().fromJson(stringExtra, MjttDao.ShowBean::class.java)
        } else {
            data = Gson().fromJson(stringExtra, DyttDao.MovieBean::class.java)
        }
        toolbar_title.text = data.name
        initView()
    }

    private fun initView() {
        Glide.with(this).load(data.placard).into(image)
        desc.text = data.desc
        name.text = data.name
        name_translated.text = data.transName
        district.text = data.country
        time.text = data.decade
        director.text = data.director
        actors.text = data.actors
        type.text = data.type
        if (data is DyttDao.MovieBean) {
            urlList.clear()
            urlList.add(ItemBean("movie", (data as DyttDao.MovieBean).ftpUrl, data.placard, data.name))
        } else if (data is MjttDao.ShowBean) {
            urlList.clear()
            val ed2kUrls = (data as MjttDao.ShowBean).ed2kUrls
            if (ed2kUrls != null && ed2kUrls.isNotEmpty()) {
                val size = ed2kUrls.size
                ed2kUrls.forEachIndexed { index, s ->
                    urlList.add(ItemBean("第" + (size - index) + "集", s, data.placard, data.name))
                }
            }
        }
        gv.adapter = DetailGVAdaper(urlList)
    }
}

class DetailGVAdaper(private val collections: ArrayList<ItemBean>) : BaseAdapter() {
    init {
        val iterator = collections.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (TextUtils.isEmpty(next.url)) {
                collections.remove(next)
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ItemHolder = if (convertView == null) {
            ItemHolder(parent!!)
        } else {
            convertView.tag as ItemHolder
        }
        holder.injectView(getItem(position))
        return holder.contentView
    }

    fun refrash(data: ArrayList<ItemBean>) {
        collections.clear()
        collections.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): ItemBean {
        return collections[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return collections.size
    }

    inner class ItemHolder(val parent: ViewGroup) {
        var contentView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_index, parent, false)

        init {
            contentView.tag = this
        }

        fun injectView(itemData: ItemBean) {
            contentView.title.text = itemData.name
            contentView.setOnClickListener {
                goPlay(itemData)
            }
        }

        private fun goPlay(itemData: ItemBean) {
            if (TextUtils.isEmpty(itemData.url)) {
                Toast.makeText(contentView.context, "节目还没有更新", Toast.LENGTH_SHORT).show()
                return
            }
            Downloader.addTask(itemData.url)
            val playIntent = Intent(contentView.context, DownloadManageActivity::class.java)
            playIntent.putExtra("url", itemData.url)
            playIntent.putExtra("name", itemData.showName + ":" + itemData.name)
            playIntent.putExtra("image", itemData.placard)
            contentView.context.startActivity(playIntent)

        }
    }
}

data class ItemBean(val name: String, val url: String, val placard: String, val showName: String) : Serializable
