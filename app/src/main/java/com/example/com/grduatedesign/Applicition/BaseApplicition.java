package com.example.com.grduatedesign.Applicition;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.example.com.grduatedesign.Utils.L;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;


import org.litepal.LitePal;
import org.litepal.LitePalApplication;

public class BaseApplicition extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(this,"appid="+"5c316803,"+SpeechConstant.FORCE_LOGIN+"=true");
        LitePal.initialize(this);
        //x5内核初始化接口
//        QbSdk.setDownloadWithoutWifi(true);
//        QbSdk.initX5Environment(getApplicationContext(), new QbSdk.PreInitCallback() {
//            @Override
//            public void onCoreInitFinished() {
//
//            }
//
//            @Override
//            public void onViewInitFinished(boolean b) {
//                if (b) {
//                    Toast.makeText(getApplicationContext(), "初始化X5成功！", Toast.LENGTH_SHORT).show();
//                }else {
//                    Toast.makeText(getApplicationContext(), "初始化X5失败！", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }
}
