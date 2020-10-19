package com.skydroid.fpvtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.skydroid.fpvlibrary.enums.PTZAction;
import com.skydroid.fpvlibrary.serial.SerialPortConnection;
import com.skydroid.fpvlibrary.serial.SerialPortControl;
import com.skydroid.fpvlibrary.video.FPVVideoClient;
import com.skydroid.fpvlibrary.widget.GLHttpVideoSurface;

import java.io.IOException;

/**
 * H12视频连接
 */
public class H12VideoSamplesActivity extends AppCompatActivity {

    private GLHttpVideoSurface mPreviewDualVideoView;

    //视频渲染
    private FPVVideoClient mFPVVideoClient;

    //usb连接实例
    private SerialPortConnection mSerialPortConnection;

    //FPV控制
    private SerialPortControl mSerialPortControl;

    private Handler mainHanlder = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h12video_samples);
        initView();
        init();
    }

    public static void start(Context context){
        context.startActivity(new Intent(context, H12VideoSamplesActivity.class));
    }

    private void initView(){
        mPreviewDualVideoView = findViewById(R.id.fPVVideoView);
        mPreviewDualVideoView.init();

        findViewById(R.id.btnTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSerialPortControl.AkeyControl(PTZAction.DOWN);
            }
        });
    }

    private void init(){
        //硬件串口实例
        mSerialPortConnection = SerialPortConnection.newBuilder("/dev/ttyHS0",921600)
                .flags(1 << 13)
                .build();
        mSerialPortConnection.setDelegate(new SerialPortConnection.Delegate() {
            @Override
            public void received(byte[] bytes, int size) {
                if(mFPVVideoClient != null){
                    mFPVVideoClient.received(bytes,size);
                }
            }

            @Override
            public void connect() {
                if(mFPVVideoClient != null){
                    mFPVVideoClient.startPlayback();
                }
            }
        });


        //渲染视频相关
        mFPVVideoClient = new FPVVideoClient();
        mFPVVideoClient.setDelegate(new FPVVideoClient.Delegate() {
            @Override
            public void onStopRecordListener(String fileName) {
                //停止录像回调
            }

            @Override
            public void onSnapshotListener(String fileName) {
                //拍照回调
            }

            //视频相关
            @Override
            public void renderI420(byte[] frame, int width, int height) {
                mPreviewDualVideoView.renderI420(frame,width,height);
            }

            @Override
            public void setVideoSize(int picWidth, int picHeight) {
                mPreviewDualVideoView.setVideoSize(picWidth,picHeight,mainHanlder);
            }

            @Override
            public void resetView() {
                mPreviewDualVideoView.resetView(mainHanlder);
            }
        });

        try {
            //打开串口
            mSerialPortConnection.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSerialPortControl = new SerialPortControl(mSerialPortConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSerialPortConnection != null){
            try {
                mSerialPortConnection.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mSerialPortConnection = null;
        if(mFPVVideoClient != null){
            mFPVVideoClient.stopPlayback();
        }
        mFPVVideoClient = null;
    }
}
