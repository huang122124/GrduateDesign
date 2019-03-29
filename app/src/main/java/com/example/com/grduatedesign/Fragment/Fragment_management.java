package com.example.com.grduatedesign.Fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.com.grduatedesign.Entity.Statics;
import com.example.com.grduatedesign.Model.MapTable;
import com.example.com.grduatedesign.Model.WpsModel;
import com.example.com.grduatedesign.R;
import com.example.com.grduatedesign.Utils.L;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsReaderView;
import com.tencent.smtt.sdk.ValueCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;


import cn.lognteng.editspinner.lteditspinner.LTEditSpinner;
import okhttp3.Call;

public class Fragment_management extends Fragment implements TbsReaderView.ReaderCallback {
    private TbsReaderView mTbsReaderView;
    private RelativeLayout mRelativeLayout;
    private String docUrl;
    private String tbsReaderTemp =Statics.PATH_NAME + "TbsReaderTemp";
    private Button open_wps;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_management,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mTbsReaderView = new TbsReaderView(getActivity(), this);
        mRelativeLayout = view.findViewById(R.id.tbsView);
        mRelativeLayout.addView(mTbsReaderView,new RelativeLayout.LayoutParams(-1,-1));
        docUrl= Statics.PATH_NAME+"大学生职业生涯规划书.doc";
        initDoc();
        view.findViewById(R.id.open_wps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                boolean flag = openFile(docUrl);
//                if (flag == true) {
//                    Toast.makeText(getActivity()," 打开文件成功", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(getActivity(), "打开文件失败", Toast.LENGTH_SHORT).show();
//                }

                opendocFile(getActivity(),docUrl);
            }
        });
    }
    public void opendocFile(Context context, String path) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setAction(Intent.ACTION_VIEW);
            File file=new File(path);
            Uri uri= FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName()
                    + ".provider",file);
            intent.setDataAndType(uri, MapTable.getMIMEType(path));
            context.startActivity(intent);
            Intent.createChooser(intent, "请选择对应的软件打开该附件！");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "sorry附件不能打开，请下载相关软件！", Toast.LENGTH_SHORT).show();
        }
    }
    private void initDoc() {

        int i = docUrl.lastIndexOf("/");
        String docName = docUrl.substring(i);
        L.d("---substring---" + docName);

        //判断是否在本地/[下载/直接打开]
        File docFile = new File(docUrl);
        if (docFile.exists()) {
            //存在本地;
            L.d( "本地存在");
            displayFile(docUrl,  docName);
            //openFileReader(getActivity(),docUrl);
        } else {
            //本地不存在,则下载;使用的OkGo2.x;
        }
    }
    private void displayFile(String filePath, String fileName) {

        //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
        String bsReaderTemp = tbsReaderTemp;
        File bsReaderTempFile =new File(bsReaderTemp);
        if (!bsReaderTempFile.exists()) {
            L.d("准备创建/TbsReaderTemp！！");
            boolean mkdir = bsReaderTempFile.mkdir();
            if(!mkdir){
                L.d("创建/TbsReaderTemp失败！！！！！");
            }
        }
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        bundle.putString("tempPath", tbsReaderTemp);
        boolean result = mTbsReaderView.preOpen(getFileType(fileName), false);
        L.d("查看文档---"+result);
        if (result) {
            mTbsReaderView.openFile(bundle);
        }else{

        }
    }

    private String getFileType(String paramString) {
        String str = "";

        if (TextUtils.isEmpty(paramString)) {
            L.d( "paramString---->null");
            return str;
        }
        L.d("paramString:" + paramString);
        int i = paramString.lastIndexOf('.');
        if (i <= -1) {
            L.d( "i <= -1");
            return str;
        }

        str = paramString.substring(i + 1);
        L.d( "paramString.substring(i + 1)------>" + str);
        return str;
    }
    boolean openFile(String path) {

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(WpsModel.OPEN_MODE, WpsModel.OpenMode.READ_MODE);
        //打开模式
        bundle.putBoolean(WpsModel.SEND_SAVE_BROAD, true);
        //关闭时是否发送广播
        bundle.putString(WpsModel.THIRD_PACKAGE, getActivity().getApplication().getPackageName());
        //第三方应用的包名，用于对改应用合法性的验证
        bundle.putBoolean(WpsModel.CLEAR_TRACE, true);
        //清除打开记录
        //bundle.putBoolean(CLEAR_FILE, true);
        //关闭后删除打开文件
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setClassName(WpsModel.PackageName.NORMAL, WpsModel.ClassName.NORMAL);
        File file = new File(path);
        if (!file.exists()){
            return false;
        }
        Uri uri =  FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName()
                + ".provider",file);
        intent.setData(uri);
        intent.putExtras(bundle);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            L.e("打开wps异常："+e.toString());
            return false;
        }
        return true;
    }
    public void openFileReader(Context context, String pathName)
    {

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("local", "true");
        JSONObject Object = new JSONObject();
        try
        {
            Object.put("pkgName",context.getApplicationContext().getPackageName());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        params.put("menuData",Object.toString());
        QbSdk.getMiniQBVersion(context);
        int ret = QbSdk.openFileReader(context, pathName, params, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {

            }
        });

    }
    @Override
    public void onDestroy() {
        mTbsReaderView.onStop();
        super.onDestroy();
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }
}
