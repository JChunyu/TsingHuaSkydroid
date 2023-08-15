package com.skydroid.fpvtest

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.skydroid.fpvlibrary.enums.PTZAction
import com.skydroid.fpvlibrary.serial.SerialPortConnection
import com.skydroid.fpvlibrary.serial.SerialPortControl
import com.skydroid.fpvlibrary.video.FPVVideoClient
import com.skydroid.fpvlibrary.widget.GLHttpVideoSurface
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


/**
 * H12视频连接
 */
class H12VideoSamplesActivity : AppCompatActivity() {

    private var mPreviewDualVideoView: GLHttpVideoSurface? = null
    //视频渲染
    private var mFPVVideoClient: FPVVideoClient? = null

    //usb连接实例
    private var mSerialPortConnection: SerialPortConnection? = null

    //FPV控制
    private var mSerialPortControl: SerialPortControl? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isSetTime = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_h12video_samples)
        initView()
        init()
    }

    private fun initView() {
        mPreviewDualVideoView = findViewById(R.id.fPVVideoView)
        mPreviewDualVideoView?.init()
        findViewById<View>(R.id.btnTest).setOnClickListener {
            mSerialPortControl?.AkeyControl(PTZAction.DOWN)
        }
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            // todo
        }
        findViewById<Button>(R.id.btnScreenShot).setOnClickListener {
            // todo
            val bitmap = captureWindow(this@H12VideoSamplesActivity, true)
            saveBitmap(bitmap, "ScreenShot" + System.currentTimeMillis() + ".jpeg")
            bitmap?.recycle()
        }
    }

    private fun init() {
        //硬件串口实例
        mSerialPortConnection = SerialPortConnection.newBuilder("/dev/ttyHS0", 4000000)
            .flags(1 shl 13)
            .build()
        mSerialPortConnection?.setDelegate(object : SerialPortConnection.Delegate {
            override fun received(bytes: ByteArray, size: Int) {
                mFPVVideoClient?.received(bytes, size)
            }

            override fun connect() {
                mFPVVideoClient?.startPlayback()
            }
        })


        //渲染视频相关
        mFPVVideoClient = FPVVideoClient()
        mFPVVideoClient!!.setDelegate(object : FPVVideoClient.Delegate {
            override fun onStopRecordListener(fileName: String) {
                //停止录像回调
            }

            override fun onSnapshotListener(fileName: String) {
                //拍照回调
            }

            //视频相关
            override fun renderI420(frame: ByteArray, width: Int, height: Int) {
                if (!isSetTime && mSerialPortControl != null) {
                    isSetTime = true
                    //设置相机时间
                    mSerialPortControl?.setTime(System.currentTimeMillis())
                }
                mPreviewDualVideoView?.renderI420(frame, width, height)
            }

            override fun setVideoSize(picWidth: Int, picHeight: Int) {
                mPreviewDualVideoView?.setVideoSize(picWidth, picHeight, mainHandler)
            }

            override fun resetView() {
                mPreviewDualVideoView?.resetView(mainHandler)
            }
        })
        try {
            //打开串口
            mSerialPortConnection?.openConnection()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mSerialPortControl = SerialPortControl(mSerialPortConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mSerialPortConnection != null) {
            try {
                mSerialPortConnection!!.closeConnection()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        mSerialPortConnection = null
        if (mFPVVideoClient != null) {
            mFPVVideoClient!!.stopPlayback()
        }
        mFPVVideoClient = null
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, H12VideoSamplesActivity::class.java))
        }
    }

    fun captureWindow(activity: Activity, isShowStatusBar: Boolean): Bitmap? {
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
        return bitmap
    }

    private fun saveBitmap(bitmap: Bitmap?, bitName: String) {
        if (bitmap == null) {
            Log.d("saveBitmap", "bitmap = null")
            return
        }
        val fileName: String = Environment.getExternalStorageDirectory().path + "/DCIM/Screenshots/" + bitName
        val file = File(fileName)
        if (file.exists()) {
            file.delete()
        }
        try {
            val out = FileOutputStream(file)
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush()
                out.close()

                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                // 发送广播，通知刷新图库的显示
                sendBroadcast(
                    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
                )
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}