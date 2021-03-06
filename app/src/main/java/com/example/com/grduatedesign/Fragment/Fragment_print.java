package com.example.com.grduatedesign.Fragment;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.example.com.grduatedesign.Adapter.DocListAdapter;
import com.example.com.grduatedesign.Model.MapTable;
import com.example.com.grduatedesign.Utils.L;
import com.example.com.grduatedesign.Utils.LoadTxtFile;
import com.example.com.grduatedesign.Utils.TextSearchFile;

import com.example.com.grduatedesign.Entity.Statics;
import com.example.com.grduatedesign.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lognteng.editspinner.lteditspinner.LTEditSpinner;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * @author
 */
public class Fragment_print extends Fragment implements View.OnClickListener {
    private LTEditSpinner<String> spinner;
    private List<File> list;
    private File file;
    private Toast mToast;
    private String docUrl;
    private ListView listView;
    private View headerView;
    private List<String>dirname;
    private DocListAdapter adapter;
    private List<String>docList;
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
        final File file = new File(pathname);
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
        view.findViewById(R.id.print_start).setOnClickListener(this);
        view.findViewById(R.id.print_save).setOnClickListener(this);
        view.findViewById(R.id.btn_print).setOnClickListener(this);


        headerView=View.inflate(getContext(),R.layout.doclistview_header,null);
        listView=view.findViewById(R.id.docListView);
        listView.addHeaderView(headerView);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                headerView.setVisibility(View.VISIBLE);
            }
        });
        docList=new ArrayList<>();
       updataList(docList);

    }

    private void initDocList(List<String> docList) {
        docList.clear();
        List<File>docfile=TextSearchFile.searchFiles(new File(Statics.PATH_CONVERT),".doc");
        if (docfile.size()!=0){
            for (int i=0;i<docfile.size();i++){
                docList.add(docfile.get(i).getName());
            }
        }

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

        if (files.length==0) {
            showTip("当前无模板可选择");
        } else {
            templetFileList= Arrays.asList(files);
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
                List<File>txtFileList=TextSearchFile.searchFiles(new File(path),".txt");
                if (txtFileList.size()==0){
                    showTip("找不到该访谈文本！");
                    return;
                }
                String itvname_txt=txtFileList.get(0).getName();   //包含日期，名称
                String itvname_txt_1=itvname_txt.substring(0,itvname_txt.indexOf("."));       //txt文件前缀名
                //要选择的模板
                String templetname=templets[currentItem];
                String outputFile=Statics.PATH_CONVERT+itvname_txt_1+".doc";
                L.d("itvTXTname:"+itvname_txt_1+"\n"+"templetname: "+templetname);
                Message message=handler.obtainMessage();
                message.what=1;
                Bundle bundle=new Bundle();
                bundle.putString("txtPath",path+"/"+itvname_txt);
                bundle.putString("txtName", itvname_txt_1);
                bundle.putString("templetname",templetname);
                bundle.putString("outputFile",outputFile);
                message.setData(bundle);
                handler.sendMessage(message);

            }
        });
        builder.setTitle("请选择模板");
        AlertDialog dialog=builder.create();
        dialog.show();
        }
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1) {
                String txtPath=msg.getData().getString("txtPath");
                String templetname=msg.getData().getString("templetname");
                String outputFile=msg.getData().getString("outputFile");
                String txtName=msg.getData().getString("txtName");      //日期加访谈（文）
                try {
                    Merge(txtPath, templetname, outputFile,txtName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    private void Merge(String txtPath, String templetname, final String outputFile, String doc_name) throws IOException {
//       makeFilePath(Statics.PATH_CONVERT,itvname+".doc");
        List<String> txtList;
        txtList = LoadTxtFile.txtList(new File(txtPath));
        L.d("正文："+txtList.size());
        List<Map<String, Object>> contentList = new ArrayList<>();
        for (int i = 0; i <txtList.size(); i++) {
               Map<String, Object> map = new HashMap<>();
               map.put("content", txtList.get(i));
               contentList.add(map);
           }
           // 要填入模本的数据文件
           configuration = new Configuration(Configuration.VERSION_2_3_24);
           configuration.setDefaultEncoding("UTF-8");
           configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
           Map dataMap = new HashMap();
           dataMap.put("number", "3115000820");
           dataMap.put("name", "黄家燊");
           dataMap.put("contentList", contentList);
           // 设置模本装置方法和路径,FreeMarker支持多种模板装载方法。可以重servlet，classpath，数据库装载，
           // 这里我们的模板是放在com.havenliu.document.template包下面
           configuration.setDirectoryForTemplateLoading(new File(Statics.PATH_TEMPLET));
           Template t = null;
           // test.ftl为要装载的模板
           try {
               t = configuration.getTemplate(templetname);
           } catch (Exception e1) {
               e1.printStackTrace();
           }
           // 生成文件
           File docFile = new File(outputFile);
           FileOutputStream fos = new FileOutputStream(docFile);
           Writer out = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"), 10240);
           try {
               t.process(dataMap, out);
               showTip("排版成功！");
              updataList(docList);

               AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
               builder.setTitle("提示")
                       .setMessage("排版成功！是否马上查看？")
                       .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               opendocFile(getActivity(),outputFile);   //可调用office打开，打印
                           }
                       })
                       .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {

                           }
                       })
                       .create().show();
           } catch (TemplateException e) {
               e.printStackTrace();
           }
        //   displayFile(docFile);  //显示生成的doc文档
//        try {
//            //doc-->html
//            String htmlPath=Statics.PATH_HTML+doc_name+".html";
//            Convert.convert2Html(outputFile,doc_name,htmlPath);
             //webView.loadUrl("file://"+htmlPath);
//        } catch (TransformerException e) {
//            e.printStackTrace();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        }
    }

    private void updataList(final  List<String> docList) {
        initDocList(docList);
        if (docList.size()==0){
            return;
        }

        adapter=new DocListAdapter(getContext(),R.layout.item_doc,docList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  //position因header会多1
                L.d("position:"+position);
                String docPath=Statics.PATH_CONVERT+docList.get(position-1);
                File file1=new File(docPath);
                if (file1.exists()){
                    opendocFile(getActivity(),docPath);
                }else {
                    showTip("文件不存在");
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                String docPath=Statics.PATH_CONVERT+docList.get(position-1);
                final File file2=new File(docPath);
                if (file2.exists()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setTitle("提示")
                            .setMessage("是否删除此文档？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    file2.delete();
                                    docList.remove(position-1);
                                    showTip("删除成功！");
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("取消",null)
                            .create().show();

                }else {
                    showTip("文件不存在");
                }
                return true;
            }
        });

    }

    private void opendocFile(Context context, String path) {
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

//    private void displayFile(File outputFile) {
////增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
//        String bsReaderTemp =Statics.PATH_TbsReaderTemp;
//        File bsReaderTempFile = new File(bsReaderTemp);
//        if (!bsReaderTempFile.exists()) {
//            L.d( "准备创建/TbsReaderTemp！！");
//            boolean mkdir = bsReaderTempFile.mkdir();
//            if (!mkdir) {
//                L.d("创建/TbsReaderTemp失败！！！！！");
//            }
//        }
//        Bundle bundle = new Bundle();
//        bundle.putString("filePath", Statics.PATH_CONVERT+"2019-5-4对清洁的满意度采访（文）.doc");
//        bundle.putString("tempPath",Statics.PATH_TbsReaderTemp);
//        boolean result = tbsView.preOpen(getFileType(outputFile.getPath()), false);
//        L.d("查看文档---" + result);
//        if (result) {
//            tbsView.openFile(bundle);
//        } else {
//
//        }
//
//
//    }

    /**
     * 后缀名的判断
     *
     * @param paramString
     * @return
     */
    private String getFileType(String paramString) {
        String str = "";
        if (TextUtils.isEmpty(paramString)) {
            L.d("paramString---->null");
            return str;
        }
        L.d("paramString:" + paramString);
        int i = paramString.lastIndexOf('.');
        if (i <= -1) {
            L.d("i <= -1");
            return str;
        }

        str = paramString.substring(i + 1);
        L.d( "paramString.substring(i + 1)------>" + str);
        return str;
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

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
