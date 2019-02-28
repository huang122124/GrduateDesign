package com.example.com.grduatedesign.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.grduatedesign.Model.Imformation;
import com.example.com.grduatedesign.R;
import com.iflytek.cloud.SpeakerVerifier;

import java.util.List;

public class ImformationAdapter extends ArrayAdapter<Imformation> {
    private int resouceId;
    private MyVerifyListener verifyListener;
    public ImformationAdapter(Context context, int resource, List<Imformation>objects,MyVerifyListener listener) {
        super(context, resource,objects);
        resouceId=resource;
        verifyListener=listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
       Imformation imformation=getItem(position);
       View view= LayoutInflater.from(getContext()).inflate(resouceId,parent,false);
        TextView tv_role=view.findViewById(R.id.tv_role);
        TextView tv_sex=view.findViewById(R.id.tv_sex);
        TextView tv_birthday=view.findViewById(R.id.tv_birthday);
        TextView tv_authId=view.findViewById(R.id.tv_authId);
        TextView tv_name=view.findViewById(R.id.tv_name);
        Button verify=view.findViewById(R.id.btn_verify);
        String authId=imformation.getAuthId();
        tv_role.setText(imformation.getRole());
        tv_name.setText(imformation.getName());
        tv_authId.setText(authId);
        if (imformation.getMale()){
        tv_sex.setText("男");
        }else {
            tv_sex.setText("女");
        }
        verify.setTag(position);
        verify.setOnClickListener(verifyListener);
        return view;
    }

    public static abstract class MyVerifyListener implements View.OnClickListener{
    @Override
    public void onClick(View view) {
        myOnClick((Integer) view.getTag(),view);
    }
        public abstract void myOnClick(int position, View v);

    }


    private void verify_person(int position) {
        Imformation imformation=getItem(position);
        Toast.makeText(getContext(), "authId: "+ imformation.getAuthId(), Toast.LENGTH_SHORT).show();
    }
}
