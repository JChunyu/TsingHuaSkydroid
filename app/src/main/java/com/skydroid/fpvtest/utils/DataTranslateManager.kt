package com.skydroid.fpvtest.utils

import android.util.Log
import com.skydroid.fpvlibrary.serial.SerialPortConnection
import java.io.IOException
import java.util.Arrays

object DataTranslateManager {
    private var mServiceConnection: SerialPortConnection? = null

    var listener: ((String) -> Unit)? = null

    fun init() {
        //硬件串口实例 readSize设置200的时候可以接受25个字节
        //subs 格式标准是25个字节 16进制的数字  分别代表16个通道每个通道占11位
        //解析详见  https://blog.csdn.net/peach_orange/article/details/52958385
        //f0 58 d66081255cf705cb376e399717696df9f8f8f8f8f8f800
        mServiceConnection = SerialPortConnection.newBuilder("/dev/ttyHS1", 921600)
            .flags(1 shl 13)
            .stopBits(1)
            .parity(0)
            .dataBits(8)
            .readSize(4096)
            .build()
        Log.e("received", "1")
        mServiceConnection?.setDelegate(object : SerialPortConnection.Delegate {
            override fun received(bytes: ByteArray, size: Int) {
                Log.e("数传", " 开始")
                // byte[] string打印
                Log.e("Byte array: size", size.toString())
                Log.e("Byte array:", Arrays.toString(bytes))
                val sb = StringBuilder()
                for (i in bytes.indices) {
                    val hex = Integer.toHexString(bytes[i].toInt() and 0xFF)
                    if (hex.length < 2) {
                        sb.append(0)
                    }
                    sb.append(hex)
                }
                Log.e("Byte hex 3", sb.toString())
                listener?.invoke(sb.toString())
                Log.e("数传", "结束")
            }

            override fun connect() {
                Log.e("connect", "5")
            }
        })
        Log.e("received", "2")
        try {
            //打开串口
            mServiceConnection?.openConnection()
            Log.e("received", "3")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //发送数传数据
        mServiceConnection?.sendData("".toByteArray())
        Log.e("connect", "6")
    }

    fun addDataListener(block: (String) -> Unit) {
        this.listener = block
    }

    fun removeDataListener() {
        this.listener = null
    }

    fun release() {
        if (mServiceConnection != null) {
            try {
                mServiceConnection?.closeConnection()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        removeDataListener()
        mServiceConnection = null
    }
}