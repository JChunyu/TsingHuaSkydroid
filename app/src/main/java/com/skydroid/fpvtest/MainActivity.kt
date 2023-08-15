package com.skydroid.fpvtest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

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
        findViewById<Button>(R.id.btn_frequency).setOnClickListener {
            startActivity(Intent(this@MainActivity, FrequencyActivity::class.java))
        }
    }
}