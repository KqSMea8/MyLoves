package com.leox.self.myloves.UI

interface IOFuncDefiner{
    fun requestData(vararg params:Any){}
    fun onResultBack(vararg resultData:Any){}
    fun onFailed(e:Exception?){}
}
