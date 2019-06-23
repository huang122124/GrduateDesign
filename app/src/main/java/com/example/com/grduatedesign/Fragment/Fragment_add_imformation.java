package com.example.com.grduatedesign.Fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.com.grduatedesign.R;

public class Fragment_add_imformation extends Fragment {
    private String auth_id;
    private String name;
    private Boolean isMale;
    private String role;
    private EditText et_auth_id;
    private EditText et_name;
    private RadioGroup radio_group;
    private EditText et_role;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_add_imformation,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView(View view) {
        et_auth_id=view.findViewById(R.id.et_auth_id);
        et_name=view.findViewById(R.id.et_name);
        et_role=view.findViewById(R.id.et_role);

        isMale=true;   //默认男性

        radio_group=view.findViewById(R.id.radio_group);
        radio_group.check(R.id.rb_male);
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_male:
                        isMale=true;
                        break;
                    case R.id.rb_female:
                        isMale=false;
                        break;
                    default:    break;
                }
            }
        });
        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth_id=et_auth_id.getText().toString();
                name=et_name.getText().toString();
                role=et_role.getText().toString();
                if (!TextUtils.isEmpty(auth_id)){       //后面添加姓名 角色
                    Fragment_add_person  fragment=Fragment_add_person.newInstance(auth_id,name,isMale,role);   //传值
                    FragmentManager fm=getActivity().getSupportFragmentManager();      //下一步
                    fm.beginTransaction()
                            .replace(R.id.frame_layout,fragment)
                            .addToBackStack(null)
                            .commit();
                    clearView();         //清空输入框
                }else {
                    Toast.makeText(getContext(), "请输入完整资料", Toast.LENGTH_SHORT).show();
                }

            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();;
            }
        });
    }
    private void clearView(){
        et_auth_id.setText("");
        et_name.setText("");
        et_role.setText("");
        radio_group.check(R.id.rb_male);
    }
}
