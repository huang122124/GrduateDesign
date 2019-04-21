package com.example.com.grduatedesign.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.com.grduatedesign.Utils.L;
import com.example.com.grduatedesign.Utils.TextSearchFile;

import com.example.com.grduatedesign.Entity.Statics;
import com.example.com.grduatedesign.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lognteng.editspinner.lteditspinner.LTEditSpinner;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class Fragment_print extends Fragment implements View.OnClickListener {
    private LTEditSpinner<String> spinner;
    private List<File> list;
    private File file;
    private Toast mToast;
    private String docUrl;
    private WebView webView;
    private List<String>dirname;
    private Configuration configuration;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_print,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    private void initView(View view) {
        spinner =view.findViewById(R.id.print_spinner);
        String pathname = Statics.PATH_INTERVIEW;
        mToast=Toast.makeText(getActivity(),"",Toast.LENGTH_SHORT);
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

        webView=view.findViewById(R.id.webView);
        view.findViewById(R.id.print_start).setOnClickListener(this);
        view.findViewById(R.id.print_save).setOnClickListener(this);
        view.findViewById(R.id.btn_print).setOnClickListener(this);
    }
    private void showTip(String s){
        mToast.setText(s);
        mToast.show();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.print_start:
                if(TextUtils.isEmpty(spinner.getValue())){
                    showTip("请选择一个模板");
                    return;
            }else {
                    String doc_name =spinner.getValue();
                    if (isExist(doc_name)) {
                        chooseTemplet(doc_name);
                    }else {
                        showTip("该访谈名称不存在");
                    }
                }


                break;
            case R.id.print_save:

                break;
            case R.id.btn_print:

                break;
        }
    }
    int  currentItem=0;
    private void chooseTemplet(final String doc_name) {
        final List<File>templetFileList;
        List<String>templetNameList;
        String pathname =Statics.PATH_TEMPLET ;
        final File file = new File(pathname);
        File[] files = file.listFiles();

        if (files == null) {
            templetFileList = new ArrayList<>();
        } else {
            templetFileList= Arrays.asList(files);
        }
        Collections.sort(templetFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        templetNameList=new ArrayList<>();
        for (int i=0;i<templetFileList.size();i++){
            templetNameList.add(templetFileList.get(i).getName());
        }
        final String[]templets=new String[templetFileList.size()];
        templetNameList.toArray(templets);
        final AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setSingleChoiceItems(templets, currentItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentItem=i;
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String path=Statics.PATH_INTERVIEW+doc_name;
                String itvname_txt=TextSearchFile.searchFiles(new File(path),".txt").get(0).getName();
                String itvname=itvname_txt.substring(0,itvname_txt.indexOf("."));
                //要选择的模板
                String templetpath=Statics.PATH_TEMPLET+templets[currentItem];
                String outputFile=Statics.PATH_CONVERT+itvname+".html";
                L.d("itvname:"+itvname+"\n"+"templetfile: "+templetpath);
                Message message=handler.obtainMessage();
                message.what=1;
                Bundle bundle=new Bundle();
                bundle.putString("itvname",itvname);
                bundle.putString("templetpath",templetpath);
                bundle.putString("outputFile",outputFile);
                message.setData(bundle);
                handler.sendMessage(message);

            }
        });
        builder.setTitle("请选择模板");
        AlertDialog dialog=builder.create();
        dialog.show();

    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1) {
                String itvname=msg.getData().getString("itvname");
                String templetpath=msg.getData().getString("templetpath");
                String outputFile=msg.getData().getString("outputFile");
                try {
                    Merge(itvname, templetpath, outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void Merge(String itvname, String templetfilepath,String outputFile) throws IOException {
//       makeFilePath(Statics.PATH_CONVERT,itvname+".doc");
//       File file=new File(Statics.PATH_INTERVIEW+itvname);
//       List<File>fileList=TextSearchFile.searchFiles(file,".txt");
//        String txtPath;
//       if (fileList!=null){
//           txtPath=fileList.get(0).getPath();
//           List<String> txtList=LoadTxtFile.txtList(new File(txtPath));
//       }
        // 要填入模本的数据文件
        configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        Map dataMap = new HashMap();
        getData(dataMap);
        // 设置模本装置方法和路径,FreeMarker支持多种模板装载方法。可以重servlet，classpath，数据库装载，
        // 这里我们的模板是放在com.havenliu.document.template包下面
        configuration.setDirectoryForTemplateLoading(new File(Statics.PATH_TEMPLET));
        Template t=null;
        // test.ftl为要装载的模板
        try {
             t= configuration.getTemplate("test.ftl");
        }catch (Exception e1) {
            e1.printStackTrace();
        }
        // 生成文件
        String outFIle = Statics.PATH_CONVERT+itvname+".doc";
        File docFile = new File(outFIle);
        FileOutputStream fos = new FileOutputStream(docFile);
        Writer out = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"), 10240);
        try {
            t.process(dataMap, out);
        } catch (TemplateException e) {
            e.printStackTrace();
        }



//        int start=itvdocpath.lastIndexOf("/");
//        Convert.convert2Html(itvdocpath,itvdocpath.substring(start+1),outputFile);
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setSupportZoom(true);
//        webSettings.setBuiltInZoomControls(true);
//        webView.loadUrl("file://"+outputFile);
    }

    private void getData(Map dataMap) {
        dataMap.put("department", "计算机学院");
        dataMap.put("major", "计科");
        dataMap.put("code", "3115000820");
        dataMap.put("name", "黄家燊");

    }

    // 生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            L.i("error:" + e);
        }
    }
    private boolean isExist(String doc_name) {
        for (int i = 0; i < dirname.size(); i++) {
            if (doc_name.equals(dirname.get(i))) {
                return true;
            }
        }
        return false;
    }



}
