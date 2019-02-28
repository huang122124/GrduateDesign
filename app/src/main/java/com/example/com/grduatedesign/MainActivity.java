package com.example.com.grduatedesign;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.ColorSpace;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.com.grduatedesign.Fragment.Fragment_contrast;
import com.example.com.grduatedesign.Fragment.Fragment_itv_collect;
import com.example.com.grduatedesign.Fragment.Fragment_itv_setting;
import com.example.com.grduatedesign.Fragment.Fragment_management;
import com.example.com.grduatedesign.Fragment.Fragment_print;
import com.example.com.grduatedesign.Fragment.Fragment_query;
import com.example.com.grduatedesign.Fragment.Fragment_syt_setting;
import com.example.com.grduatedesign.Fragment.Fragment_add_person;
import com.example.com.grduatedesign.Utils.L;
import com.iflytek.cloud.IdentityVerifier;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private ArrayList<Fragment>fragments;
    private Fragment contrast;
    private Fragment itv_collect;
    private Fragment itv_setting;
    private Fragment management;
    private Fragment print;
    private Fragment query;
    private Fragment syt_setting;
    private Fragment add_person;
    private RelativeLayout rl_itv_setting;
    private RelativeLayout rl_itv_collect;
    private RelativeLayout rl_contrast;
    private RelativeLayout rl_query;
    private RelativeLayout rl_print;
    private RelativeLayout rl_management;
    private RelativeLayout rl_syst_setting;
    private TextView tv_itv_setting;
    private TextView tv_itv_collect;
    private TextView tv_contrast;
    private TextView tv_query;
    private TextView tv_print;
    private TextView tv_management;
    private TextView tv_syt_setting;
    private int currentTab;
    private float size_px;
    private float size;
//    private SpeakerVerifier verifier;
    private IdentityVerifier verifier;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initViews();
        SQLiteDatabase db= LitePal.getDatabase();   //LitePal提供了一个便捷的方法来获取到SQLiteDatabase的实例

    }



    private void initViews() {
        //RelativeLaout
        rl_itv_setting=findViewById(R.id.interview_setting);
        rl_itv_collect=findViewById(R.id.interview_collect);
        rl_contrast=findViewById(R.id.contrast);
        rl_query=findViewById(R.id.query);
        rl_print=findViewById(R.id.print);
        rl_management=findViewById(R.id.management);
        rl_syst_setting=findViewById(R.id.syt_setting);
        rl_itv_setting.setOnClickListener(this);
        rl_itv_collect.setOnClickListener(this);
        rl_contrast.setOnClickListener(this);
        rl_query.setOnClickListener(this);
        rl_print.setOnClickListener(this);
        rl_management.setOnClickListener(this);
        rl_syst_setting.setOnClickListener(this);
                 //TextView
        tv_itv_setting=findViewById(R.id.tv_itv_setting);
        tv_itv_collect=findViewById(R.id.tv_itv_collect);
        tv_contrast=findViewById(R.id.tv_contrast);
        tv_query=findViewById(R.id.tv_query);
        tv_print=findViewById(R.id.tv_print);
        tv_management=findViewById(R.id.tv_management);
        tv_syt_setting=findViewById(R.id.tv_syt_setting);
        size_px=tv_itv_setting.getTextSize();      //以px为单位
           //获取fragment
          fragments=new ArrayList<>();
          itv_setting=new Fragment_itv_setting();
          itv_collect=new Fragment_itv_collect();
          contrast=new Fragment_contrast();
          query=new Fragment_query();
          print=new Fragment_print();
          management=new Fragment_management();
          add_person=new Fragment_add_person();
        syt_setting=new Fragment_syt_setting();
          fragments.add(itv_setting);      // 0
          fragments.add(itv_collect);      // 1
          fragments.add(contrast);         // 2
          fragments.add(query);            // 3
          fragments.add(print);              // 4
          fragments.add(management);  // 5
          fragments.add(syt_setting);        // 6
          fragments.add(add_person);    // 7
        FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
        ft.add(R.id.frame_layout,fragments.get(0));
        currentTab=0;
        rl_itv_setting.setSelected(true);
        size=px2sp(this,size_px);
        tv_itv_setting.setTextSize(size+5);        //以sp为单位
        ft.commit();

    }
    //px转换成sp
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.interview_setting:
                changeTab(0);
                break;
            case R.id.interview_collect:
                changeTab(1);
                break;
            case R.id.contrast:
                changeTab(2);
                break;
            case R.id.query:
                changeTab(3);
                break;
            case R.id.print:
                changeTab(4);
                break;
            case R.id.management:
                changeTab(5);
                break;
            case R.id.syt_setting:
                changeTab(6);
                break;
        }
    }

    private void changeTab(int Tab) {
         if (currentTab==Tab){
             return;
         }
         Fragment fragment=fragments.get(Tab);
        FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
        if (!fragment.isAdded()){
            ft.add(R.id.frame_layout,fragment);
        }
        setStatus(currentTab,false);
        ft.replace(R.id.frame_layout,fragment);
        currentTab=Tab;
        setStatus(currentTab,true);
        ft.commit();

    }

    private void setStatus(int currentTab, boolean isSelected) {
         switch (currentTab){
             case 0:
                 rl_itv_setting.setSelected(isSelected);
                 if (isSelected==true) {
                     tv_itv_setting.setTextSize(size+5);
                 }else {
                     tv_itv_setting.setTextSize(size);
                 }
                 break;
             case 1:
                 rl_itv_collect.setSelected(isSelected);
                 if (isSelected==true) {
                     tv_itv_collect.setTextSize(size+5);
                 }else {
                     tv_itv_collect.setTextSize(size);
                 }
                 break;
             case 2:
                 rl_contrast.setSelected(isSelected);
                 if (isSelected==true) {
                     tv_contrast.setTextSize(size+5);
                 }else {
                     tv_contrast.setTextSize(size);
                 }
                 break;
             case 3:
                 rl_query.setSelected(isSelected);
                 if (isSelected==true) {
                     tv_query.setTextSize(size+5);
                 }else {
                     tv_query.setTextSize(size);
                 }
                 break;
             case 4:
                 rl_print.setSelected(isSelected);
                 if (isSelected==true) {
                     tv_print.setTextSize(size+5);
                 }else {
                     tv_print.setTextSize(size);
                 }
                 break;
             case 5:
                 rl_management.setSelected(isSelected);
                 if (isSelected==true) {
                     tv_management.setTextSize(size+5);
                 }else {
                     tv_management.setTextSize(size);
                 }
                 break;
             case 6:
                 rl_syst_setting.setSelected(isSelected);
                 if (isSelected==true) {
                     tv_syt_setting.setTextSize(size+5);
                 }else {
                     tv_syt_setting.setTextSize(size);
                 }
                 break;
         }
    }



}
