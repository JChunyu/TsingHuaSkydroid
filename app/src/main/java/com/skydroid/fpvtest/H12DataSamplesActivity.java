package com.skydroid.fpvtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.skydroid.fpvlibrary.serial.SerialPortConnection;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class H12DataSamplesActivity extends AppCompatActivity {
    //硬件串口连接实例
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
        //硬件串口实例 readSize设置200的时候可以接受25个字节
        //subs 格式标准是25个字节 16进制的数字  分别代表16个通道每个通道占11位
        //解析详见  https://blog.csdn.net/peach_orange/article/details/52958385
        //f0 58 d66081255cf705cb376e399717696df9f8f8f8f8f8f800
        mServiceConnection = SerialPortConnection.newBuilder("/dev/ttyHS1",921600)
                .flags(1 << 13)
                .stopBits(1)
                .parity(0)
                .dataBits(8)
                .readSize(4096)
                .build();
        Log.e("received","1");
        TextView textView = (TextView)findViewById(R.id.text_view_shuchuan);
        textView.setText("开始打印数传数据");
        mServiceConnection.setDelegate(new SerialPortConnection.Delegate() {
            @Override
            public void received(byte[] bytes, int size) {
                Log.e("数传"," 开始");
                // byte[] string打印
                Log.e("Byte array: size", String.valueOf(size));
                Log.e("Byte array:",  Arrays.toString(bytes));
//------------
//                String byteStr = new String(bytes, 0, bytes.length).trim();
//                System.out.println("===========start===========");
//                System.out.println(new Date() + "【读到的字符串长度】：-----" + bytes.length);
//                System.out.println(new Date() + "【读到的字符串】：-----" + byteStr);
//                StringBuilder sbf = new StringBuilder();
//                for (byte value : bytes)
//                {
//                    String hex = Integer.toHexString(value & 0xFF);
//                    if (hex.length() == 1)
//                    {
//                        hex = '0' + hex;
//                    }
//                    sbf.append(hex.toUpperCase()).append(" ");
//                }
//                System.out.println(new Date() + "【字节数组转16进制字符串】：-----" + sbf.toString().trim());
//                System.out.println("===========end===========");
//--------------------------
//                // byte[] 十六进制打印
//                String res = "";
//                for (int i = 0; i < bytes.length; i++) {
//                    String hex = Integer.toHexString(bytes[i] & 0xFF);
//                    if (hex.length() == 1) {
//                        hex = '0' + hex;
//                    }
//                    res += hex;
//                }
//                Log.e("Byte hex 2",  res);
//--------
                // byte[] 十六进制打印
                StringBuffer sb = new StringBuffer();
                for(int i = 0; i < bytes.length; i++) {
                    String hex = Integer.toHexString(bytes[i] & 0xFF);
                    if(hex.length() < 2){
                        sb.append(0);
                    }
                    sb.append(hex);
                }
                Log.e("Byte hex 3",  sb.toString());
//                textView.setText(Arrays.toString(bytes));
                Log.e("数传","结束");
            }


            @Override
            public void connect() {
                Log.e("connect","5");
            }
        });
        Log.e("received","2");
        try {
            //打开串口
            mServiceConnection.openConnection();
            Log.e("received","3");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //发送数传数据
        mServiceConnection.sendData("".getBytes());
        Log.e("connect","6");

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
