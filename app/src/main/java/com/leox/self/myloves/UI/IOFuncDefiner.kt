package com.leox.self.myloves.UI

interface IOFuncDefiner{
    fun requestData(vararg params:Any){}
    fun onResultBack(resultData:Any){}
    fun onFailed(e:Exception?){}
}
