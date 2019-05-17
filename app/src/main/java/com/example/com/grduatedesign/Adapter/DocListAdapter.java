package com.example.com.grduatedesign.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.com.grduatedesign.R;

import java.util.List;

public class DocListAdapter extends ArrayAdapter {
    private int resouceId;
    private Context context;
    private List<String>docnameList;
    private LayoutInflater inflater;
    public DocListAdapter(Context context, int resource,  List<String>docnameList){
        super(context,resource,docnameList);
        this.resouceId=resource;
        this.context=context;
        this.docnameList=docnameList;
        inflater=LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return docnameList.size();
    }

    @Override
    public Object getItem(int position) {
        return docnameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null) {
            convertView = LayoutInflater.from(getContext()).inflate(resouceId,null,false);
        }
        TextView docname_item=convertView.findViewById(R.id.docname_item);
        docname_item.setText(docnameList.get(position));
        return convertView;
    }
}
