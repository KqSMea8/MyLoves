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
import com.leox.self.myloves.db.DyttDao
import com.leox.self.myloves.db.MjttDao
import com.leox.self.myloves.utils.SpUtils
import com.scwang.smartrefresh.header.DropboxHeader
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_rv_movie.view.*
import kotlinx.android.synthetic.main.item_website.view.*
import kotlinx.android.synthetic.main.toolbar.*


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
        srl.setRefreshHeader(DropboxHeader(this))
        srl.setRefreshFooter(BallPulseFooter(this))
        srl.setOnRefreshListener { refreshData() }
        srl.setOnLoadMoreListener {
            onResultBack(currentIndex)
        }
        toolbar_back.setOnClickListener {
            finish()
        }

        initView()
    }

    private fun initView() {
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
        if (currentIndex == -1) {
            requestData(collections.list[0])
        }
    }


    override fun onStart() {
        super.onStart()
        request4Permissions()
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
                            }.show()
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

    private var currentIndex: Int = -1
    override fun onResultBack(vararg resultData: Any) {
        if (resultData.isNotEmpty() && resultData[0] is Int) {
            val isLoadMore = currentIndex == resultData[0]
            val resultSize = if (isLoadMore) {
                10 + listDatas.size
            } else {
                10
            }
            val startIndex = if (isLoadMore) {
                listDatas.size
            } else {
                0
            }
            when (resultData[0]) {
                1 -> {
                    val movieList = DyttDao.getMovieList(resultSize, startIndex)
                    currentIndex = 1
                    updateList(movieList, isLoadMore)
                }
                2 -> {
                    val movieList = MjttDao.getShowList(resultSize, startIndex)
                    currentIndex = 2
                    updateList(movieList, isLoadMore)
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
                    srl.finishRefresh()
                }
                2 -> {
                    val module = Python.getInstance().getModule("com.leox.python.mjtt.main")
                    module.callAttr("startSpider")
                    Log.i(TAG, "requestData:Done")
                    onResultBack(2)
                    srl.finishRefresh()
                }
                else -> {
                }
            }
        }.start()
    }

    private fun updateList(movieList: ArrayList<out DataBase>, loadMore: Boolean) {
        //TODO fix scroll to start problem
        runOnUiThread {
            if (!loadMore)
                listDatas.clear()
            listDatas.addAll(movieList)
            rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            rv.swapAdapter(RecyclerListAdapter(this, listDatas), true)
        }
        srl.finishLoadMore()
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
            if (data != null) {
//                goPlayMovie(data!!)
                showDetaile(data!!)
            }
        }
    }

    private fun showDetaile(data: DataBase) {
        val detailIntent = Intent(itemView.context, DetailActivity::class.java)
        detailIntent.putExtra("data", Gson().toJson(data))
        detailIntent.putExtra("isShow", data is MjttDao.ShowBean)
        itemView.context.startActivity(detailIntent)
    }

    fun injectData(movieBean: DataBase) {
        itemView.tv_title.text = movieBean.name
        Glide.with(itemView.context).load(movieBean.placard).into(itemView.iv)
        itemView.tv_desc.text = movieBean.desc
        data = movieBean
    }
}
