package com.example.com.grduatedesign.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.grduatedesign.Adapter.ImformationAdapter;
import com.example.com.grduatedesign.Entity.Statics;
import com.example.com.grduatedesign.Model.Imformation;
import com.example.com.grduatedesign.R;
import com.example.com.grduatedesign.Utils.L;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class Fragment_syt_setting extends Fragment implements View.OnClickListener, AdapterView.OnItemLongClickListener {

    private static final int PWD_TYPE_FREE = 2;
    // 当前声纹密码类型，1、2、3分别为文本、自由说和数字密码
    private int mPwdType = PWD_TYPE_FREE;
    private Button createPerson;
    private Button showPerson;
    private ListView listView;
    private View headerView;
    private TextView tv_msg;
    private Toast mToast;
    private Boolean delete_success=false;
    private List<Imformation>personList=new ArrayList<>();
    // 身份验证对象
    private IdentityVerifier mIdVerifier;
    private SpeakerVerifier mVerifier;
    private ImformationAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_syt_setting,container,false);
        mIdVerifier = IdentityVerifier.createVerifier(getActivity(), null);
        // 初始化SpeakerVerifier，InitListener为初始化完成后的回调接口
        mVerifier = SpeakerVerifier.createVerifier(getActivity(), new InitListener() {

            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    Toast.makeText(getActivity(),"引擎初始化成功",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(),"引擎初始化失败，错误码："+errorCode,Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    private void initView(View view) {
        createPerson=view.findViewById(R.id.createPerson);
        createPerson.setOnClickListener(this);
        showPerson=view.findViewById(R.id.showPerson);
        showPerson.setOnClickListener(this);
        tv_msg=view.findViewById(R.id.tv_msg);
        mToast=Toast.makeText(getActivity(),"",Toast.LENGTH_SHORT);
        headerView=View.inflate(getActivity(),R.layout.listview_headerview,null);
        listView=view.findViewById(R.id.lv_person);
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
            case R.id.showPerson:
                personList= LitePal.findAll(Imformation.class);
                adapter=new ImformationAdapter(getActivity(), R.layout.person_item, personList, new ImformationAdapter.MyVerifyListener() {
                    @Override
                    public void myOnClick(int position, View v) {
                        Imformation im=personList.get(position);
                        String authId=im.getAuthId();
                        personVerify(authId);
                    }
                });
                listView.setAdapter(adapter);
                listView.setOnItemLongClickListener(this);
                break;
        }
    }

    private void personVerify(String authId) {
        tv_msg.setText("");
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/verify.pcm");
        mVerifier = SpeakerVerifier.getVerifier();
        // 设置业务类型为验证
        mVerifier.setParameter(SpeechConstant.ISV_SST, "verify");
        // 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
        // mVerify.setParameter(SpeechC  onstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);

        if (mPwdType==PWD_TYPE_FREE){
            mVerifier.setParameter(SpeechConstant.SAMPLE_RATE,"8000");

        }
        // 设置auth_id，不能设置为空
        mVerifier.setParameter(SpeechConstant.AUTH_ID, authId);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
        // 开始验证
        mVerifier.startListening(mVerifyListener);
    }

    private VerifierListener mVerifyListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {

            showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onResult(VerifierResult result) {
            mToast.setText(result.source);

            if (result.ret == ErrorCode.SUCCESS) {
                // 验证通过 这里就意味着通过了！！！
                tv_msg.setText("验证通过");
               showTip("验证通过,"+"声纹ID："+result.vid);
            }
            else{
                // 验证不通过
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        showTip("内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        showTip("出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        showTip("太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        showTip("录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        showTip("验证不通过，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        showTip("音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        showTip("音频长达不到自由说的要求");
                        break;
                    default:
                        showTip("验证不通过,相似度仅为"+result.score+"%。");
                        break;
                }
            }
        }
        // 保留方法，暂不用
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //    String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //    Log.d(TAG, "session id =" + sid);
            // }
        }

        @Override
        public void onError(SpeechError error) {

            switch (error.getErrorCode()) {
                case ErrorCode.MSP_ERROR_NOT_FOUND:
                    showTip("模型不存在，请先注册");
                    break;

                default:
                    showTip("onError Code："    + error.getPlainDescription(true));
                    break;
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
            tv_msg.setText("");
        }



        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            tv_msg.setText("请随便说些用于验证");
            showTip("开始说话");
        }
    };
    private SpeechListener mModelOperationListener = new SpeechListener() {

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            String result = new String(buffer);
            try {
                JSONObject object = new JSONObject(result);
                String cmd = object.getString("cmd");
                int ret = object.getInt("ret");

                if ("del".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        Toast.makeText(getActivity(),"删除成功",Toast.LENGTH_SHORT).show();
                        delete_success=true;
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        Toast.makeText(getActivity(), "删除失败，模型不存在", Toast.LENGTH_SHORT).show();
                    }
                } else if ("que".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        Toast.makeText(getActivity(), "模型存在", Toast.LENGTH_SHORT).show();
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        Toast.makeText(getActivity(), "模型不存在", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        @Override
        public void onCompleted(SpeechError error) {
            if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
                Toast.makeText(getActivity(), "操作失败：" + error.getPlainDescription(true), Toast.LENGTH_SHORT).show();

            }
        }
    };
    /**
     * 删除组成员监听器
     */
    private IdentityListener mDeleteListener = new IdentityListener() {
        @Override
        public void onResult(IdentityResult result, boolean islast) {
            L.d(result.getResultString());
            try {
                JSONObject resObj = new JSONObject(result.getResultString());
                L.d("resObj == "+resObj.toString());
                int ret = resObj.getInt("ret");
                if(0 != ret) {
                    onError(new SpeechError(ret));
                    return;
                } else {
                    if(result.getResultString().contains("user")) {
                        String user = resObj.getString("user");
                        showTip("删除组成员"+user+"成功");
                        deleteFromDatabase(user);
                        tv_msg.setText("成功从数据库删除");
                    } else {
                        showTip("删除组成功");
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }

        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }
    };

    private void deleteFromDatabase(String authId) {
        LitePal.deleteAll(Imformation.class,"authId=?",authId);
       for (int i=0;i<personList.size();i++){
           if (personList.get(i).getAuthId().contentEquals(authId)){
              personList.remove(i);
           }
       }

    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (i==0){
            return true;
        }
        String authId=personList.get(i-1).getAuthId();
        showDialog(authId);
        return true;
    }

    private void showDialog(final String authId) {
        final AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
        dialog.setIcon(R.drawable.ic_launcher_foreground)
                .setTitle("删除成员"+authId+"?")
                .setMessage("是否从数据库删除？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMember(authId);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        dialog.show();

    }

    private void deleteMember(String authId) {
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        //设置密码类型(就是咱要读的)
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);

        // 设置auth_id，不能设置为空
        mVerifier.sendRequest("del", authId,mModelOperationListener);
        if (delete_success=true) {
            deleteFromGroup(authId, false);
        }
    }

    private void deleteFromGroup(String authId, boolean b) {
        if (authId==null){
            return;
        }
        // sst=add，auth_id=eqhe，group_id=123456，scope=person
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authId);

        // 设置模型参数，若无可以传空字符传
        StringBuffer params2 = new StringBuffer();
            // 删除组中指定auth_id用户
        params2.append("scope=person");
        params2.append(",auth_id="+authId);
        params2.append(",group_id=" + Statics.GROUPID);
        // 执行模型操作
        mIdVerifier.execute("ipt", "delete", params2.toString(), mDeleteListener);
    }

}
