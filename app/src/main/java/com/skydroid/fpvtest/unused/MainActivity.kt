package com.skydroid.fpvtest.unused

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.skydroid.fpvtest.H12VideoSamplesActivity
import com.skydroid.fpvtest.R
import com.skydroid.fpvtest.utils.UsbSerialActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
        initView()
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 0
        )
    }

    private fun initView() {
        findViewById<View>(R.id.btn_h12_video).setOnClickListener {
            H12VideoSamplesActivity.start(this@MainActivity)
        }
        findViewById<View>(R.id.btn_usb_serial).setOnClickListener {
            UsbSerialActivity.start(this@MainActivity)
        }
        findViewById<View>(R.id.btn_h12_data).setOnClickListener {
            H12DataSamplesActivity.start(this@MainActivity)
        }
        findViewById<View>(R.id.btn_h30_data).setOnClickListener {
            H30DataSamplesActivity.start(this@MainActivity)
        }
    }
}