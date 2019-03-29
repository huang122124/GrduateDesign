package com.example.com.grduatedesign.Fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.grduatedesign.Activity.MainActivity;
import com.example.com.grduatedesign.R;

import java.io.File;
import java.util.Calendar;

public class Fragment_itv_setting extends Fragment {
    private Button dateChoose;
    private TextView date;
    private  int mYear, mMonth, mDay;
    private  final int DATE_DIALOG = 1;
    private TextView itv_person,itv_name;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        View view=inflater.inflate(R.layout.fragment_itv_setting,container,false);
        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        itv_person=view.findViewById(R.id.itv_person);
        itv_name=view.findViewById(R.id.itv_name);
        date=view.findViewById(R.id.date);

        dateChoose=view.findViewById(R.id.dateChoose);
        view.findViewById(R.id.btn_collect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity= (MainActivity) getActivity();
                String person= itv_person.getText().toString();
                String name= itv_name.getText().toString();
                String date1=date.getText().toString();
//                if (activity.itv_collect==null){
//                    activity.itv_collect=new Fragment_itv_collect();
//                }
//                FragmentTransaction ft=activity.getSupportFragmentManager().beginTransaction();
////                if (!TextUtils.isEmpty(itv_name.getText())&&!TextUtils.isEmpty(itv_person.getText())&&!TextUtils.isEmpty(date.getText())){
////                    Toast.makeText(getActivity(), "请输入完整资料", Toast.LENGTH_SHORT).show();
////                    return;
////                }
//                ft.replace(R.id.frame_layout,activity.itv_collect);
//
//                ft.commit();
                File file=new File(Environment.getExternalStorageDirectory() + "/msc/iat/" +name);
                if (file.exists()&&file.isDirectory()){
                    Toast.makeText(getActivity(), "该访谈名称已存在！", Toast.LENGTH_SHORT).show();
                }else if (!TextUtils.isEmpty(itv_person.getText())&&!TextUtils.isEmpty(itv_name.getText())&&!TextUtils.isEmpty(date.getText())){
                    activity.changeToCollect(person,name,date1);
                }else {
                    Toast.makeText(getActivity(), " 请输入完整资料！", Toast.LENGTH_SHORT).show();
                }

            }
        });

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

        view.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itv_person.setText("");
                itv_name.setText("");
                date.setText("");
            }
        });
    }


}
