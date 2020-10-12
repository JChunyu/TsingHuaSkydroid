package com.skydroid.fpvtest;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class  MainActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;
        requestPermissions();
        initView();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 0
        );
    }

    private void initView(){
        findViewById(R.id.btn_h12_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                H12VideoSamplesActivity.start(context);
            }
        });

        findViewById(R.id.btn_usb_serial).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsbSerialActivity.start(context);
            }
        });
        findViewById(R.id.btn_h12_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                H12DataSamplesActivity.start(context);
            }
        });
    }
}
