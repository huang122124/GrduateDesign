package com.example.com.grduatedesign.Fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.com.grduatedesign.R;
import com.iflytek.cloud.SpeakerVerifier;

import java.util.Calendar;

public class Fragment_itv_setting extends Fragment {
    private Button dateChoose;
    private TextView date;
    private  int mYear, mMonth, mDay;
    private  final int DATE_DIALOG = 1;
    // 声纹识别对象
    private SpeakerVerifier mVerifier;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        View view=inflater.inflate(R.layout.fragment_itv_setting,container,false);
        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        date=view.findViewById(R.id.date);
        dateChoose=view.findViewById(R.id.dateChoose);


        dateChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dialog = new
                        DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int
                            dayOfMonth) {
                        mYear=year;
                        if(monthOfYear<=9){
                            mMonth=monthOfYear+1;
                        }else{
                            mMonth=monthOfYear+1;
                        }
                        if(dayOfMonth<=9){
                            mDay= dayOfMonth;
                        }else{
                            mDay=dayOfMonth;
                        }
                        date.setText(mYear+"-"+mMonth+"-"+mDay);


                    }
                },calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.setCancelable(true);
                dialog.show();
            }
        });


    }


}
