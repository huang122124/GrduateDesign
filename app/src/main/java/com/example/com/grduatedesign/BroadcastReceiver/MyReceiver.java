package com.example.com.grduatedesign.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.com.grduatedesign.Fragment.Fragment_management;
import com.example.com.grduatedesign.Utils.L;

public class MyReceiver extends BroadcastReceiver {
    private Fragment_management management=new Fragment_management();
    @Override
    public void onReceive(Context context, Intent intent) {
        String path=intent.getStringExtra("path");
        L.d("已收到广播");
        management.opendocFile(context.getApplicationContext(),path);
    }
}
