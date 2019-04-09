package com.example.com.grduatedesign.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import com.example.com.grduatedesign.Entity.Statics;
import com.example.com.grduatedesign.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.lognteng.editspinner.lteditspinner.LTEditSpinner;

public class Fragment_print extends Fragment {
    private LTEditSpinner<String>ltEditSpinner;
    private List<File> list;
    private File file;
    private WebView webView;
    private String itv_name;
    private String txtName;
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
        ltEditSpinner=view.findViewById(R.id.print_spinner);
        String pathname = Statics.PATH_INTERVIEW;
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
        final List<String> dirname = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int start = list.get(i).getPath().lastIndexOf("/");
            dirname.add(list.get(i).getPath().substring(start + 1));
        }
        ltEditSpinner.initData(dirname, new LTEditSpinner.OnESItemClickListener() {
            @Override
            public void onItemClick(Object o) {
            }
        });

        webView=view.findViewById(R.id.webView);
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl("http://debugtbs.qq.com");
                webView.setWebViewClient(new WebViewClient(){
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                        webView.loadUrl(s);
                        return true;
                    }
                });

            };
        });
    }
}
