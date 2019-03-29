package com.example.com.grduatedesign.Applicition;

import android.app.Application;
import android.content.Context;

import com.example.com.grduatedesign.Utils.L;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.tencent.smtt.sdk.QbSdk;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

public class BaseApplicition extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(this,"appid="+"5c316803,"+SpeechConstant.FORCE_LOGIN+"=true");
        LitePal.initialize(this);
        //初始化X5内核
        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                //x5内核初始化完成回调接口，此接口回调并表示已经加载起来了x5，有可能特殊情况下x5内核加载失败，切换到系统内核。

            }

            @Override
            public void onViewInitFinished(boolean b) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                L.d("加载内核是否成功:"+b);
            }
        });
    }
}
