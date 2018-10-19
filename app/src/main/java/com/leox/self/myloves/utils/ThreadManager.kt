package com.leox.self.myloves.utils

import java.util.concurrent.*

object ThreadManager{
    val threadPools:ThreadPoolExecutor by lazy {
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        ThreadPoolExecutor(2,availableProcessors + 1,30,TimeUnit.SECONDS,LinkedBlockingDeque(100),
                Executors.defaultThreadFactory(),ThreadPoolExecutor.AbortPolicy())
    }

    fun exec(block:()->Unit){
        threadPools.execute(block)
    }
}