package com.leox.self.myloves.UI

interface IOFuncDefiner{
    fun requestData(params:List<Any>){}
    fun onResultBack(resultData:Any){}
    fun onFailed(e:Exception?){}
}
