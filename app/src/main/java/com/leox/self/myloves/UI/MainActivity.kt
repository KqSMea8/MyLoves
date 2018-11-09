package com.leox.self.myloves.UI

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.chaquo.python.Python
import com.google.gson.Gson
import com.leox.self.myloves.R
import com.leox.self.myloves.data.CollectionItem
import com.leox.self.myloves.data.CollectionList
import com.leox.self.myloves.db.DataBase
import com.leox.self.myloves.db.TableDytt
import com.leox.self.myloves.db.TableMjtt
import com.leox.self.myloves.utils.SpUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_rv_movie.view.*
import kotlinx.android.synthetic.main.item_website.view.*


class MainActivity : BaseActivity() {

    companion object {
        val COLLECTION_NAME = "collection_name"
        val TAG = "MainActivity"
    }

    private lateinit var collections: CollectionList
    private val listDatas = ArrayList<DataBase>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        srl.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                refreshData()
            }

        })
    }


    override fun onStart() {
        super.onStart()
        request4Permissions()
        val collectionList = CollectionList(ArrayList(mutableListOf(
                CollectionItem(1, "http://www.dytt8.net", R.mipmap.ic_launcher, "电影天堂"),
                CollectionItem(2, "http://www.baidu.com", R.mipmap.ic_launcher, "美剧天堂"),
                CollectionItem(3, "http://www.baidu.com", R.mipmap.ic_launcher, "天天美剧"),
                CollectionItem(4, "http://www.baidu.com", R.mipmap.ic_launcher, "BiliBili"))))
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

    private fun request4Permissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                        || PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            requestPermissions(arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 2012)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2012) {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder(this)
                            .setTitle("权限不具备")
                            .setMessage("需要存储权限和电话权限才能正常运行，请去权限中心赋权")
                            .setCancelable(false)
                            .setPositiveButton("马上设置") { _, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", getPackageName(), null)
                                intent.data = uri
                                startActivityForResult(intent, 2018)
                            }
                    break
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 2018) {
            request4Permissions()
        }
    }

    private fun initList() {
        gv.adapter = CollectionAdapter(collections)
        gv.setOnItemClickListener { _, _, position, _ ->
            requestData(collections.list[position])
        }
    }

    override fun requestData(vararg params: Any) {
        if (params.isNotEmpty() && params[0] is CollectionItem) {
            when ((params[0] as CollectionItem).id) {
                1 -> {
                    onResultBack(1)
                }
                2 -> {
                    onResultBack(2)
                }
                else -> {
                    Toast.makeText(this@MainActivity, "还没有实现呢", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private var currentIndex: Int = 1
    override fun onResultBack(resultData: Any) {
        if (resultData is Int) {
            when (resultData) {
                1 -> {
                    val movieList = TableDytt.getMovieList(10, 0)
                    currentIndex = 1
                    updateList(movieList)
                }
                2 -> {
                    val movieList = TableMjtt.getShowList(10, 0)
                    currentIndex = 2
                    updateList(movieList)
                }
                else -> {
                    Toast.makeText(this@MainActivity, "还没有实现呢", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun refreshData() {
        Thread {
            when (currentIndex) {
                1 -> {
                    val module = Python.getInstance().getModule("com.leox.python.dytt8.main")
                    module.callAttr("startSpider")
                    Log.i(TAG, "requestData:Done")
                    onResultBack(1)
                    srl.isRefreshing = false
                }
                2 -> {
                    val module = Python.getInstance().getModule("com.leox.python.mjtt.main")
                    module.callAttr("startSpider")
                    Log.i(TAG, "requestData:Done")
                    onResultBack(2)
                    srl.isRefreshing = false
                }
                else -> {
                }
            }
        }.start()
    }

    private fun updateList(movieList: ArrayList<out DataBase>) {
        runOnUiThread {
            listDatas.clear()
            listDatas.addAll(movieList)
            rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            rv.swapAdapter(RecyclerListAdapter(this, listDatas), true)
        }
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

class RecyclerListAdapter(val ctx: Context, val datas: ArrayList<DataBase>) : RecyclerView.Adapter<MovieHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        return MovieHolder(LayoutInflater.from(ctx).inflate(R.layout.item_rv_movie, parent, false))
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        holder.injectData(datas[position])
    }
}

class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var data: DataBase? = null

    init {
        itemView.setOnClickListener {
            if (data != null)
                goPlayMovie(data!!)
        }
    }

    fun injectData(movieBean: DataBase) {
        itemView.tv_title.text = movieBean.name
        Glide.with(itemView.context).load(movieBean.placard).into(itemView.iv)
        itemView.tv_desc.text = movieBean.desc
        data = movieBean
    }

    private fun goPlayMovie(movieList: DataBase) {
        val playIntent = Intent(itemView.context, PlayActivity::class.java)
        val ftpUrl: String = if (movieList is TableMjtt.ShowBean) {
            movieList.ed2kUrls!![0]
        } else if (movieList is TableDytt.MovieBean) {
            movieList.ftpUrl
        } else {
            ""
        }
        Log.i("MainActivity", "ftpUrl:${ftpUrl}")
        playIntent.putExtra("url", ftpUrl)
        playIntent.putExtra("name", movieList.name)
        playIntent.putExtra("image", movieList.placard)
        itemView.context.startActivity(playIntent)
    }
}
