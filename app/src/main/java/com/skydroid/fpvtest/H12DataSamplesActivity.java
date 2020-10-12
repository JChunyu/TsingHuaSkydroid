package com.skydroid.fpvtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.skydroid.fpvlibrary.serial.SerialPortConnection;

import java.io.IOException;

public class H12DataSamplesActivity extends AppCompatActivity {
    //usb连接实例
    private SerialPortConnection mServiceConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h12data_samples);
        initView();
        init();
    }

    public static void start(Context context){
        context.startActivity(new Intent(context, H12DataSamplesActivity.class));
    }

    private void initView(){

    }

    private void init(){
        //硬件串口实例
        mServiceConnection = SerialPortConnection.newBuilder("/dev/ttyHSL1",921600)
                .flags(1 << 13)
                .build();
        mServiceConnection.setDelegate(new SerialPortConnection.Delegate() {
            @Override
            public void received(byte[] bytes, int size) {
                Log.e("received",new String(bytes));
            }

            @Override
            public void connect() {

            }
        });

        try {
            //打开串口
            mServiceConnection.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //发送数传数据
        //mServiceConnection.sendData("".getBytes());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mServiceConnection != null){
            try {
                mServiceConnection.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mServiceConnection = null;

    }
}
