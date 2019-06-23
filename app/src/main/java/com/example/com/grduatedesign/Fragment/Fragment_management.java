package com.example.com.grduatedesign.Fragment;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.example.com.grduatedesign.BroadcastReceiver.MyReceiver;
import com.example.com.grduatedesign.Dialog.New_templet_dialog;
import com.example.com.grduatedesign.Entity.Statics;
import com.example.com.grduatedesign.Model.MapTable;
import com.example.com.grduatedesign.Model.WpsModel;
import com.example.com.grduatedesign.R;
import com.example.com.grduatedesign.Utils.Convert;
import com.example.com.grduatedesign.Utils.L;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


import cn.lognteng.editspinner.lteditspinner.LTEditSpinner;

public class Fragment_management extends Fragment implements View.OnClickListener {
    private WebView webView;
    private String templetUrl =null,htmlfile;
    private Button edit,delete;
    private New_templet_dialog dialog;
    private MyReceiver receiver;
    private IntentFilter filter;
    private  List<String>dirname;
    private List<File>list;
    private  String currentTemplet;
    private LocalBroadcastManager manager;
    private LTEditSpinner<String> spinner;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_management,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        webView = view.findViewById(R.id.man_WebView);
        spinner=view.findViewById(R.id.man_spinner);
        currentTemplet=null;
        initSpinner();
        view.findViewById(R.id.load_templet).setOnClickListener(this);
        (edit=view.findViewById(R.id.edit_templet)).setOnClickListener(this);
        edit.setVisibility(View.GONE);
        view.findViewById(R.id.new_templet).setOnClickListener(this);
        (delete= view.findViewById(R.id.delete_templet)).setOnClickListener(this);
        delete.setVisibility(View.GONE);
        view.findViewById(R.id.refresh_templet).setOnClickListener(this);
        //注册本地广播
        receiver=new MyReceiver();
        filter=new IntentFilter();
        filter.addAction("com.example.graduatedesign.NEW_TEMPLET");
        manager=LocalBroadcastManager.getInstance(getActivity());
        manager.registerReceiver(receiver,filter);
        checkPermission();
    }

    private void initSpinner() {
        spinner.setEditHint("请输入模板名称");
        String pathname =Statics.PATH_TEMPLET ;
        File file = new File(pathname);
        File[] files = file.listFiles();

        if (files == null) {
            list = new ArrayList<>();
        } else {
            list = Arrays.asList(files);
        }
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        dirname = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int start = list.get(i).getPath().lastIndexOf("/");
            dirname.add(list.get(i).getPath().substring(start + 1));
        }
        spinner.initData(dirname, new LTEditSpinner.OnESItemClickListener() {
            @Override
            public void onItemClick(Object o) {
            }
        });

    }

    private void checkPermission() {
        if (!Settings.System.canWrite(getActivity())) {
            Uri selfPackageUri = Uri.parse("package:" + getActivity().getPackageName());
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    selfPackageUri);
            startActivity(intent);
        }
    }
    public void opendocFile(Context context, String path) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("SendSaveBroad",true);
            intent.setAction(Intent.ACTION_VIEW);
            File file=new File(path);
            Uri uri= FileProvider.getUriForFile(context, "com.example.com.grduatedesign.provider",file);
         //   L.d(getActivity().getApplicationContext().getPackageName());
            intent.setDataAndType(uri, MapTable.getMIMEType(path));
            context.startActivity(intent);
            Intent.createChooser(intent, "请选择对应的软件打开该附件！");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "sorry附件不能打开，请下载相关软件！", Toast.LENGTH_SHORT).show();
        }
    }


    private String getFileType(String paramString) {
        String str = "";

        if (TextUtils.isEmpty(paramString)) {
            L.i( "paramString---->null");
            return str;
        }
        L.i("paramString:" + paramString);
        int i = paramString.lastIndexOf('.');
        if (i <= -1) {
            L.i( "i <= -1");
            return str;
        }

        str = paramString.substring(i + 1);
        L.i( "paramString.substring(i + 1)------>" + str);
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


    }

    @Override
    public void onPause() {
        super.onPause();
        L.d("pause");
    }

    @Override
    public void onResume() {
        super.onResume();
        L.d("resume");
        initSpinner();
    }

    @Override
    public void onDestroy() {
        manager.unregisterReceiver(receiver);
        super.onDestroy();
    }


boolean loaded=false;
    @Override
    public void onClick(View v) {
        if (dirname.size()==0){
            Toast.makeText(getActivity(), "当前模板数量为0，请新建模板", Toast.LENGTH_SHORT).show();
        }
                switch (v.getId()) {
                    case R.id.load_templet:
                    String templet_name = spinner.getValue();
                    if (!TextUtils.isEmpty(templet_name)) {
                        templetUrl = Statics.PATH_TEMPLET + templet_name;
                        File file=new File(templetUrl);
                        if (!file.exists()) {
                            Toast.makeText(getActivity(), "文件不存在", Toast.LENGTH_SHORT).show();
                        } else {
                            String s = templet_name.substring(0, templet_name.indexOf("."));
                            htmlfile = Statics.PATH_HTML + s + ".doc";
                            try {
                                Convert.convert2Html(templetUrl, templet_name, htmlfile);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                           // initDoc();
                            currentTemplet = templet_name;
                           edit.setVisibility(View.VISIBLE);
                           delete.setVisibility(View.VISIBLE);

                        }
                    } else {
                        Toast.makeText(getActivity(), "请输入模板名称", Toast.LENGTH_SHORT).show();
                    }
                        break;

                    case R.id.edit_templet:
                        templetUrl = Statics.PATH_TEMPLET + currentTemplet;
                        opendocFile(getActivity(), templetUrl);
                        break;

                    case R.id.delete_templet:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("提示")
                                .setMessage("是否删除选中模版?")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteTemplet(currentTemplet);
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .create()
                                .show();
                        break;
                    case  R.id.new_templet:
                        //弹出提示框
                        dialog = new New_templet_dialog(getActivity(), R.style.New_templet_dialog);
                        dialog.show();
//                if (openFile()){
//                    L.d("新建成功");
//                }
                    case R.id.refresh_templet:
                        break;
                }






    }

    private void deleteTemplet(String currentTemplet) {
        File docfile=new File(Statics.PATH_TEMPLET+currentTemplet);
        String s = currentTemplet.substring(0, currentTemplet.indexOf("."));
       String htmlpath = Statics.PATH_HTML + s + ".html";
        File htmlfile=new File(htmlpath);
        if (docfile.exists()){
            docfile.delete();
        }
        if (htmlfile.exists()){
            htmlfile.delete();
        }
        initSpinner();
        webView.destroy();

    }

    private void initDoc() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webView.loadUrl("file://"+htmlfile);

    }






}
