package com.skydroid.fpvtest.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

object ScreenUtils {
    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics() // 创建了一张白纸
        windowManager.defaultDisplay.getMetrics(dm) // 给白纸设置宽高
        return dm.widthPixels
    }

    fun getDensity(context: Context): Float {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.density
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics() // 创建了一张白纸
        windowManager.defaultDisplay.getMetrics(dm) // 给白纸设置宽高
        return dm.heightPixels
    }

}