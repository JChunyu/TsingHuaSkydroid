package com.skydroid.fpvtest.frequency

import android.content.Context
import android.util.Log
import com.skydroid.fpvtest.R
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

class FrequencyManager(val context: Context) {

    val TAG = "HomeActivity"

    private var pipeline: Pipeline? = null

    private var keySignalQualityListener = KeyListener<Int> { oldValue, newValue -> Log.e(TAG,"信号强度:${oldValue},${newValue}") }

    fun start() {
        RCSDKManager.initSDK(context, object : SDKManagerCallBack {
            override fun onRcConnected() {
                KeyManager.set(RemoteControllerKey.KeyControlMode, ControlMode.JP) { e ->
                    if (e == null){
                        log("设置摇杆模式成功")
                    }else{
                        log("设置摇杆模式失败：${e}")
                    }
                }

                KeyManager.get(RemoteControllerKey.KeyControlMode, object : CompletionCallbackWith<ControlMode> {
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
                buildPipe()
            }

            override fun onRcConnectFail(e: SkyException?) {
                log("RC SDK 连接失败$e")
            }

            override fun onRcDisconnect() {
                log("RC SDK 重新连接")
                buildPipe()
            }
        })
        RCSDKManager.connectToRC()
    }

    fun release() {
        KeyManager.cancelListen(keySignalQualityListener)

        pipeline?.let {
            PipelineManager.disconnectPipeline(it)
        }
    }

    private fun buildPipe() {
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

    private fun log(any: Any?){
        any ?: return
        Log.e(TAG, any.toString())
    }
}