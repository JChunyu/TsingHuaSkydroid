package com.skydroid.fpvtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.skydroid.rcsdk.KeyManager
import com.skydroid.rcsdk.PipelineManager
import com.skydroid.rcsdk.RCSDKManager
import com.skydroid.rcsdk.SDKManagerCallBack
import com.skydroid.rcsdk.comm.CommListener
import com.skydroid.rcsdk.common.DeviceType
import com.skydroid.rcsdk.common.callback.CompletionCallbackWith
import com.skydroid.rcsdk.common.callback.KeyListener
import com.skydroid.rcsdk.common.error.SkyException
import com.skydroid.rcsdk.common.pipeline.Pipeline
import com.skydroid.rcsdk.common.remotecontroller.ControlMode
import com.skydroid.rcsdk.key.AirLinkKey
import com.skydroid.rcsdk.key.RemoteControllerKey

/*
* 对频功能
* */
class FrequencyActivity : AppCompatActivity() {

    val TAG = "HomeActivity"

    private var keySignalQualityListener = KeyListener<Int>{
            oldValue, newValue ->
        Log.e(TAG,"信号强度:${oldValue},${newValue}")
    }

    private var pipeline: Pipeline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        RCSDKManager.initSDK(this,object : SDKManagerCallBack {
            override fun onRcConnected() {
                KeyManager.set(RemoteControllerKey.KeyControlMode, ControlMode.JP) {
                        e ->
                    if (e == null){
                        log("设置摇杆模式成功")
                    }else{
                        log("设置摇杆模式失败：${e}") }
                }


                KeyManager.get(RemoteControllerKey.KeyControlMode,object :
                    CompletionCallbackWith<ControlMode> {
                    override fun onSuccess(result: ControlMode?) {
                        log(result)
                    }

                    override fun onFailure(e: SkyException?) {
                        log(e)
                    }
                })

                when (RCSDKManager.getDeviceType()){
                    DeviceType.H12 -> {
                        KeyManager.get(AirLinkKey.KeyH12SignalQuality,object :
                            CompletionCallbackWith<Int> {
                            override fun onSuccess(result: Int?) {
                                log("H12信号强度：${result}")
                            }

                            override fun onFailure(e: SkyException?) {

                            }
                        })
                    }
                    DeviceType.H12Pro -> {
                        KeyManager.listen(AirLinkKey.KeySignalQuality,keySignalQualityListener)
                    }
                    DeviceType.H16 -> {
                        KeyManager.listen(AirLinkKey.KeyH16SignalQuality,keySignalQualityListener)
                    }
                }
                //创建通讯管道
                pipeline = PipelineManager.createPipeline()
                pipeline?.let {
                    //设置监听
                    it.onCommListener = object : CommListener {
                        override fun onConnectSuccess() {
                            log("管道连接成功")
                        }

                        override fun onConnectFail(e: SkyException?) {
                            log("管道连接失败${e}")
                        }

                        override fun onDisconnect() {
                            log("管道断开连接")
                        }

                        override fun onReadData(data: ByteArray?) {

                        }

                    }
                    //连接通讯管道
                    PipelineManager.connectPipeline(it)
                }
            }

            override fun onRcConnectFail(e: SkyException?) {

            }

            override fun onRcDisconnect() {

            }
        })
        RCSDKManager.connectToRC()
        findViewById<View>(R.id.btn_pairing).setOnClickListener{
            KeyManager.action(RemoteControllerKey.KeyRequestPairing){
                    e ->
                if (e == null){
                    log("对频成功")
                }else{
                    log("对频失败：${e}")
                }
            }
        }

    }

    private fun log(any: Any?){
        any ?: return
        Log.e(TAG, any.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        KeyManager.cancelListen(keySignalQualityListener)

        pipeline?.let {
            PipelineManager.disconnectPipeline(it)
        }
    }
}