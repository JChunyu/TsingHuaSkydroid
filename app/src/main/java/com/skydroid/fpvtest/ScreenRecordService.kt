package com.skydroid.fpvtest

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import com.skydroid.fpvtest.utils.ScreenUtils
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class ScreenRecordService : Service() {
    private val TAG = "ScreenRecordService"

    /**
     * 是否为标清视频
     */
    private var isVideoSd = false

    private var mScreenWidth = 1080
    private var mScreenHeight = 1920
    private var mScreenDensity = 1

    private var mMediaProjection: MediaProjection? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mVirtualDisplay: VirtualDisplay? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    private fun startForeground() {
        val activityIntent = Intent(this, H12VideoSamplesActivity::class.java)
        activityIntent.action = "stop"
        val contentIntent =
            PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "001"
            val channelName = "myChannel"
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (manager != null) {
                manager.createNotificationChannel(channel)
                val notification: Notification = Notification.Builder(
                    applicationContext, channelId
                )
                    .setOngoing(true)
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentTitle("录屏通知")
                    .setContentIntent(contentIntent)
                    .build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(10, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
                } else {
                    startForeground(10, notification)
                }
            }
        } else {
            startForeground(10, Notification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", 1)
        val resultData: Intent? = intent?.getParcelableExtra("data")
//        getScreenBaseInfo()
        mMediaProjection = createMediaProjection(resultData!!, resultCode!!)
        mMediaRecorder = createMediaRecorder()
        mVirtualDisplay = createVirtualDisplay() // 必须在mediaRecorder.prepare() 之后调用，否则报错"fail to get surface"
        mMediaRecorder?.start()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        mVirtualDisplay?.release()
        mVirtualDisplay = null
        mMediaRecorder?.setOnErrorListener(null)
        mMediaRecorder?.reset()
        mMediaProjection?.stop()
        mMediaProjection = null
    }

    private fun getScreenBaseInfo() {
        mScreenWidth = ScreenUtils.getScreenWidth(this)
        mScreenHeight = ScreenUtils.getScreenHeight(this)
        mScreenDensity = ScreenUtils.getDensity(this).toInt()
    }

    private fun createMediaProjection(intent: Intent, code: Int): MediaProjection? {
        Log.i(TAG, "Create MediaProjection")
        return (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).getMediaProjection(code, intent)
    }

    private fun createMediaRecorder(): MediaRecorder {
        val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        val curDate = Date(System.currentTimeMillis())
        val curTime: String = formatter.format(curDate).replace(" ", "")
        var videoQuality = "HD"
        if (isVideoSd) videoQuality = "SD"
        Log.i(TAG, "Create MediaRecorder")
        val mediaRecorder = MediaRecorder()
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setOutputFile(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                .toString() + "/" + videoQuality + curTime + ".mp4"
        )
        mediaRecorder.setVideoSize(
            mScreenWidth,
            mScreenHeight
        )
        //after setVideoSource(), setOutFormat()
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264) //after setOutputFormat()
        //        if(isAudio) mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);  //after setOutputFormat()
        if (isVideoSd) {
            mediaRecorder.setVideoEncodingBitRate(mScreenWidth * mScreenHeight)
            mediaRecorder.setVideoFrameRate(30)
            mScreenWidth * mScreenHeight / 1000
        } else {
            mediaRecorder.setVideoEncodingBitRate(5 * mScreenWidth * mScreenHeight)
            mediaRecorder.setVideoFrameRate(60) //after setVideoSource(), setOutFormat()
            5 * mScreenWidth * mScreenHeight / 1000
        }
        try {
            mediaRecorder.prepare()
        } catch (e: IllegalStateException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return mediaRecorder
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        Log.i(TAG, "Create VirtualDisplay")
        return mMediaProjection?.createVirtualDisplay(
            TAG, mScreenWidth, mScreenHeight, mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder?.surface, null, null
        )
    }

}