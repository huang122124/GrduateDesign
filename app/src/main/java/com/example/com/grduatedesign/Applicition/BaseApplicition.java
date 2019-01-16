package com.example.com.grduatedesign.Applicition;

import android.app.Application;
import android.content.Context;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class BaseApplicition extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(this,"appid="+"5c316803,"+SpeechConstant.FORCE_LOGIN+"=true");
    }
}
