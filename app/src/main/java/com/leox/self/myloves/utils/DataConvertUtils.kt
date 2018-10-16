package com.leox.self.myloves.utils

object DataConvertUtils {
    fun convertFileSize(size: Long): String {
        val kb: Long = 1024
        val mb = kb * 1024
        val gb = mb * 1024

        if (size >= gb) {
            return String.format("%.1f GB", size.toFloat() / gb)
        } else if (size >= mb) {
            val f = size.toFloat() / mb
            return String.format(if (f > 100) "%.0f M" else "%.1f M", f)
        } else if (size >= kb) {
            val f = size.toFloat() / kb
            return String.format(if (f > 100) "%.0f K" else "%.1f K", f)
        } else
            return String.format("%d B", size)
    }
}