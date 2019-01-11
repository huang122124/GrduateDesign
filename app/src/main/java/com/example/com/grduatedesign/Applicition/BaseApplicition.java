package com.example.com.grduatedesign.Applicition;

import android.app.Application;
import android.content.Context;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class BaseApplicition extends Application {
    private Context context=getApplicationContext();
    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(context,SpeechConstant.APPID+"=5c316803");
    }
}
