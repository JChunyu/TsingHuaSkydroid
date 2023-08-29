package com.skydroid.fpvtest.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object RecorderUtil {
    fun checkPermission(activity: AppCompatActivity) {
        val checkPermission = (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                    + ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_PHONE_STATE
            )
                    + ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    + ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ))
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            //动态申请
            ActivityCompat.requestPermissions(
                activity, arrayOf<String>(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 123
            )
        }
    }



}