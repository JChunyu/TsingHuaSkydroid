package com.skydroid.fpvtest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skydroid.fpvlibrary.enums.PTZAction
import com.skydroid.fpvlibrary.serial.SerialPortConnection
import com.skydroid.fpvlibrary.serial.SerialPortControl
import com.skydroid.fpvlibrary.video.FPVVideoClient
import com.skydroid.fpvlibrary.widget.GLHttpVideoSurface
import com.skydroid.fpvtest.frequency.FrequencyManager
import com.skydroid.fpvtest.utils.DataTranslateManager
import com.skydroid.fpvtest.utils.ScreenShotsUtil
import com.skydroid.rcsdk.KeyManager
import com.skydroid.rcsdk.key.RemoteControllerKey
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

    private var stateTv: TextView? = null

    private var frequencyManager: FrequencyManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_h12video_samples)
        init()
        initView()
    }

    private fun initView() {
        mPreviewDualVideoView = findViewById(R.id.fPVVideoView)
        mPreviewDualVideoView?.init()
        stateTv = findViewById(R.id.stateTv)
        findViewById<View>(R.id.btnRecord).setOnClickListener {
            // todo
        }
        findViewById<View>(R.id.btnScreenShot).setOnClickListener {
            // 截图
            if (ScreenShotsUtil.captureWindow(this@H12VideoSamplesActivity, "ScreenShot" + System.currentTimeMillis() + ".jpeg", true)) {
                Toast.makeText(this@H12VideoSamplesActivity, "截屏已保存到相册", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@H12VideoSamplesActivity, "截屏失败，请重试", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<View>(R.id.frequencyTv).setOnClickListener{
            if (frequencyManager == null) {
                frequencyManager = FrequencyManager(this@H12VideoSamplesActivity)
                frequencyManager?.start()
            }
            KeyManager.action(RemoteControllerKey.KeyRequestPairing){ e ->
                if (e == null){
                    this@H12VideoSamplesActivity.window.decorView.post {
                        Toast.makeText(this@H12VideoSamplesActivity, "对频成功", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    this@H12VideoSamplesActivity.window.decorView.post {
                        Toast.makeText(this@H12VideoSamplesActivity, "对频失败，请确认接收机进入对频模式，绿灯闪烁", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        DataTranslateManager.addDataListener {
            this@H12VideoSamplesActivity.window.decorView.post {
                stateTv?.text = "数传结果：$it"
            }
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
        if (frequencyManager == null) {
            frequencyManager = FrequencyManager(this)
        }
        frequencyManager?.start()
        DataTranslateManager.init()
    }

    override fun onDestroy() {
        DataTranslateManager.release()
        frequencyManager?.release()
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
        super.onDestroy()
    }


    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, H12VideoSamplesActivity::class.java))
        }
    }
}