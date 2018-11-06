package com.leox.self.myloves.UI

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.chaquo.python.Python
import com.google.gson.Gson
import com.leox.self.myloves.R
import com.leox.self.myloves.data.CollectionItem
import com.leox.self.myloves.data.CollectionList
import com.leox.self.myloves.db.TableDytt
import com.leox.self.myloves.utils.SpUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_website.view.*


class MainActivity : BaseActivity() {

    companion object {
        val COLLECTION_NAME = "collection_name"
    }

    private lateinit var collections: CollectionList
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    override fun onStart() {
        super.onStart()
        val collectionList = CollectionList(ArrayList(mutableListOf(
                CollectionItem(1, "http://www.baidu.com", R.mipmap.ic_launcher, "Bilibili"),
                CollectionItem(2, "http://www.baidu.com", R.mipmap.ic_launcher, "test2"),
                CollectionItem(3, "http://www.baidu.com", R.mipmap.ic_launcher, "test3"),
                CollectionItem(4, "http://www.baidu.com", R.mipmap.ic_launcher, "test4"))))
        SpUtils.getInstance(this).edit().putString(COLLECTION_NAME, Gson().toJson(collectionList)).commit()
        val collection = SpUtils.getInstance(this).getString(COLLECTION_NAME, "")
        if (TextUtils.isEmpty(collection)) {
            //TODO 添加收藏页
        } else {
            collections = Gson().fromJson(collection, CollectionList::class.java)
            initList()
        }
        btn_manage.setOnClickListener {
            //TODO 添加收藏页
        }

        btn_hide.setOnClickListener {
            if (gv.visibility == View.VISIBLE)
                gv.visibility = View.GONE
            else
                gv.visibility = View.VISIBLE
        }
    }

    private fun initList() {
        gv.adapter = CollectionAdapter(collections)
        gv.setOnItemClickListener { _, _, position, _ ->
            requestData(listOf(collections.list[position]))
        }
    }

    override fun requestData(params: List<Any>) {
        val module = Python.getInstance().getModule("com.leox.python.dytt8.main")
        val callAttr = module.callAttr("startSpider")
        Log.i("requestData",callAttr.toString())
        onResultBack("dytt.db")
    }

    override fun onResultBack(resultData: Any) {
        val movieList = TableDytt.getMovieList(2, 0)
            val playIntent = Intent(this@MainActivity, PlayActivity::class.java)
            playIntent.putExtra("url", movieList[0].ftpUrl)
            this@MainActivity.startActivity(playIntent)
    }

    override fun onFailed(e: Exception?) {
    }


}

class CollectionAdapter(private val collections: CollectionList) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ItemHolder = if (convertView == null) {
            ItemHolder(parent!!)
        } else {
            convertView.tag as ItemHolder
        }
        holder.injectView(getItem(position))
        return holder.contentView
    }

    override fun getItem(position: Int): CollectionItem {
        return collections.list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return collections.list.size
    }

    inner class ItemHolder(val parent: ViewGroup) {
        var contentView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_website, parent, false)

        init {
            contentView.tag = this
        }

        fun injectView(itemData: CollectionItem) {
            contentView.icon.setImageResource(itemData.icon)
            contentView.title.text = itemData.title
        }
    }
}
