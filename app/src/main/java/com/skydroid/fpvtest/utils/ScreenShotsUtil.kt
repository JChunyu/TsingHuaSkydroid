package com.skydroid.fpvtest.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object ScreenShotsUtil {
    fun captureWindow(activity: Activity, fileName: String, isShowStatusBar: Boolean): Boolean {
        // 获取当前窗体的View对象
        val view = activity.window.decorView
        view.isDrawingCacheEnabled = true
        // 生成缓存
        view.buildDrawingCache()
        val bitmap = if (isShowStatusBar) {
            // 绘制整个窗体，包括状态栏
            Bitmap.createBitmap(view.drawingCache, 0, 0, view.measuredWidth, view.measuredHeight)
        } else {
            // 获取状态栏高度
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val display = activity.windowManager.defaultDisplay

            // 减去状态栏高度
            Bitmap.createBitmap(view.drawingCache, 0,
                rect.top, display.width, display.height - rect.top)
        }
        view.isDrawingCacheEnabled = false
        view.destroyDrawingCache()
        bitmap?.let {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            if (!path.exists()) {
                path.mkdirs()
            }
            val filePath: String = path.path
            val file = File(filePath, fileName)
            Log.i("ScreenShotUtils", "$file")
            if (file.exists()) {
                file.delete()
            }
            try {
                val out = FileOutputStream(file)
                // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                    out.flush()
                    out.close()
                    MediaStore.Images.Media.insertImage(
                        activity.contentResolver,
                        file.absolutePath,
                        fileName,
                        null
                    )
//                    val values = ContentValues()
//                    values.put(MediaStore.Images.Media.DATA, file.absolutePath)
//                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//                    val uri = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//                    // 发送广播，通知刷新图库的显示
//                    activity.sendBroadcast(
//                        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
//                    )

                    val localUri = Uri.fromFile(file)
                    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri)
                    activity.sendBroadcast(intent)
                }
                bitmap.recycle()
                return true
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                activity.window.decorView.post {
                    Toast.makeText(activity, "FileNotFoundException: $fileName", Toast.LENGTH_LONG).show()
                }
                bitmap.recycle()
                return false
            } catch (e: IOException) {
                e.printStackTrace()
                activity.window.decorView.post {
                    Toast.makeText(activity, "IOException", Toast.LENGTH_LONG).show()
                }
                bitmap.recycle()
                return false
            }
        } ?: kotlin.run {
            activity.window.decorView.post {
                Toast.makeText(activity, "bitmap = null", Toast.LENGTH_LONG).show()
            }
            Log.d("saveBitmap", "bitmap = null")
            return false
        }
    }
}