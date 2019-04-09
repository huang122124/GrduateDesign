package com.example.com.grduatedesign.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.com.grduatedesign.Entity.Statics;
import com.example.com.grduatedesign.R;
import com.example.com.grduatedesign.Utils.L;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class New_templet_dialog extends Dialog implements View.OnClickListener {
    private EditText et_templet;
    private Button yes,cancel;
    private String path,doc_name;
    public New_templet_dialog(Context context,int themeResId) {
        super(context,themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_new_templet);
        et_templet=findViewById(R.id.et_templet);
        yes=findViewById(R.id.yes);
        cancel=findViewById(R.id.no);
        yes.setOnClickListener(this);
        cancel.setOnClickListener(this);
        path= Statics.PATH_TEMPLET ;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.yes:
                if (!TextUtils.isEmpty(et_templet.getText().toString())) {
                    doc_name=et_templet.getText().toString() + ".doc";
                    makeFilePath(path, doc_name);
                    try {
                        //写字符串进word
                        InputStream is = getContext().getAssets().open("empty_doc.doc");
                        HWPFDocument doc = new HWPFDocument(is);
                        //获取Range
                        Range range = doc.getRange();
                       range.insertAfter("666");
                        //写到原文件中
                        //OutputStream os = new FileOutputStream(源文件path);
                        //写到另一个文件中
                        OutputStream os = new FileOutputStream(path+doc_name);
                        doc.write(os);
                        closeStream(is);
                        closeStream(os);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setTitle("提示")
                            .setMessage("新建成功!是否进行编辑？")
                            .setPositiveButton("确认", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    LocalBroadcastManager manager=LocalBroadcastManager.getInstance(getContext());
                                    Intent intent=new Intent("com.example.graduatedesign.NEW_TEMPLET");
                                    intent.putExtra("path",path+doc_name);
                                    manager.sendBroadcast(intent);

                                }
                            })
                            .setNegativeButton("取消",null)
                            .show();
                    dismiss();
                }else {

                }
                break;
            case R.id.no:
                New_templet_dialog.this.cancel();
                break;
        }
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
    /**
     * 关闭输入流
     * @param is
     */
    private void closeStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 关闭输出流
     * @param os
     */
    private void closeStream(OutputStream os) {
        if (os != null) {
            try {
               os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
