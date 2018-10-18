package com.leox.self.myloves.data

import java.io.Serializable

data class CollectionItem(val id:Int,val url:String,val icon:Int,val title:String):Serializable

data class CollectionList(val list:ArrayList<CollectionItem>):Serializable