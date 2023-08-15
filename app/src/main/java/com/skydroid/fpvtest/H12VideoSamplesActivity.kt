package com.skydroid.fpvtest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.skydroid.fpvlibrary.enums.PTZAction
import com.skydroid.fpvlibrary.serial.SerialPortConnection
import com.skydroid.fpvlibrary.serial.SerialPortControl
import com.skydroid.fpvlibrary.video.FPVVideoClient
import com.skydroid.fpvlibrary.widget.GLHttpVideoSurface
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
}