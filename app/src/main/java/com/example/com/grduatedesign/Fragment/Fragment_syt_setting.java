package com.example.com.grduatedesign.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.com.grduatedesign.R;

public class Fragment_syt_setting extends Fragment implements View.OnClickListener {
    private Button createPerson;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_syt_setting,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    private void initView(View view) {
        createPerson=view.findViewById(R.id.createPerson);
        createPerson.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.createPerson:
                FragmentManager fm=getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.frame_layout,new Fragment_add_imformation())
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }

}
