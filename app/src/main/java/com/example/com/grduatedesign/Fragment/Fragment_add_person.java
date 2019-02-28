package com.example.com.grduatedesign.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.grduatedesign.Activity.GroupManagerActivity;
import com.example.com.grduatedesign.Activity.VocalIdentifyActivity;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fragment_add_person extends Fragment implements View.OnClickListener {
    private static final int PWD_TYPE_TEXT = 1;
    private static final int PWD_TYPE_FREE = 2;
    private static final int PWD_TYPE_NUM = 3;
    // 当前声纹密码类型，1、2、3分别为文本、自由说和数字密码
    private int mPwdType = PWD_TYPE_FREE;
    // 声纹识别对象
    private SpeakerVerifier mVerifier;
    // 身份验证对象
    private IdentityVerifier mIdVerifier;
    // 声纹AuthId，用户在云平台的身份标识，也是声纹模型的标识
    // 请使用英文字母或者字母和数字的组合，勿使用中文字符
    private String authId = "";
private String name;
private boolean isMale;       //个人资料
private String role;
    private EditText mResultEditText;
    private TextView mAuthIdTextView;
    private TextView mShowPwdTextView;
    private TextView mShowMsgTextView;
    private TextView mShowRegFbkTextView;
    private TextView mRecordTimeTextView;
    private Toast mToast;
    private LinearLayout ll_add_person;
    private Context mContext=getActivity();

public  static Fragment_add_person newInstance(String auth_id,String name,Boolean isMale,String role){
    Fragment_add_person fragment=new Fragment_add_person();
    Bundle bundle=new Bundle();
    bundle.putString("id",auth_id);
    bundle.putString("name",name);
    bundle.putBoolean("isMale",isMale);
    bundle.putString("role",role);
    fragment.setArguments(bundle);
    return fragment;
}
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_add_person,container,false);
        mIdVerifier = IdentityVerifier.createVerifier(mContext, null);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
 //每个应用不同的AuthId
        if (getArguments()!=null) {
            authId = getArguments().getString("id", "admin");
            name = getArguments().getString("name");
            isMale = getArguments().getBoolean("isMale");
            role = getArguments().getString("role");
            mAuthIdTextView.setText(authId);
        }
          L.d("Hello,"+authId);
        // 初始化SpeakerVerifier，InitListener为初始化完成后的回调接口
        mVerifier = SpeakerVerifier.createVerifier(getActivity(), new InitListener() {

            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    L.d("引擎初始化成功");
                   Toast.makeText(getActivity(),"引擎初始化成功",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(),"引擎初始化失败，错误码："+errorCode,Toast.LENGTH_LONG).show();
                }
            }
        });


    }



    private void initView(@NonNull View view) {
        mResultEditText = (EditText)view.findViewById(R.id.edt_result);
        mAuthIdTextView = (TextView) view.findViewById(R.id.txt_authorid);
        mShowPwdTextView = (TextView) view.findViewById(R.id.showPwd);
        mShowMsgTextView = (TextView) view.findViewById(R.id.showMsg);
        mShowRegFbkTextView = (TextView) view.findViewById(R.id.showRegFbk);
        mRecordTimeTextView = (TextView) view.findViewById(R.id.recordTime);

        view.findViewById(R.id.isv_register).setOnClickListener(this);
        view.findViewById(R.id.isv_verify).setOnClickListener(this);
        view.findViewById(R.id.isv_stop_record).setOnClickListener(this);
        view.findViewById(R.id.isv_cancel).setOnClickListener(this);
        view.findViewById(R.id.isv_search).setOnClickListener(this);
        view.findViewById(R.id.isv_delete).setOnClickListener(this);
        view.findViewById(R.id.back).setOnClickListener(this);
        view.findViewById(R.id.isv_identity).setOnClickListener(this);
        view.findViewById(R.id.isv_join).setOnClickListener(this);
        view.findViewById(R.id.vocal).setOnClickListener(this);
        view.findViewById(R.id.query).setOnClickListener(this);
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        ll_add_person=view.findViewById(R.id.ll_add_person);
        // 等待框设置为不可取消

//        mProDialog.setCanceledOnTouchOutside(false);
//        mProDialog.setTitle("请稍候");
//
//        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                // cancel进度框时,取消正在进行的操作
//                if (null != mIdVerifier) {
//                    mIdVerifier.cancel();
//                }
//            }
//        });

    }

    private void initTextView() {
        mResultEditText.setText("");
        mShowPwdTextView.setText("");
        mShowMsgTextView.setText("");
        mShowRegFbkTextView.setText("");
        mRecordTimeTextView.setText("");

    }


    /**
     * 执行模型操作
     *
     * @param operation 操作命令
     * @param listener  操作结果回调对象
     */
    private void performModelOperation(String operation, SpeechListener listener) {
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        //设置密码类型(就是咱要读的)
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);

        // 设置auth_id，不能设置为空
        mVerifier.sendRequest(operation, authId, listener);
    }

    @Override
    public void onClick(View v) {
        if( !checkInstance() ){
            return;
        }
        switch (v.getId()){
            case R.id.isv_search:
                mShowMsgTextView.setText("");
                performModelOperation("que", mModelOperationListener);
                break;
            case R.id.isv_delete:
                initTextView();
                performModelOperation("del", mModelOperationListener);
                deleteMember(authId,false);
                break;
            //注册
           case R.id.isv_register:
               initTextView();
                // 清空参数
                mVerifier.setParameter(SpeechConstant.PARAMS, null);
                mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/"+authId+"_register.pcm");
                // 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
//       mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);
                if (mPwdType==PWD_TYPE_FREE){
                   //这里插一句嘴，自由说的注册参数之次数 设置为“1” 音质的的设置“8000”
                    mVerifier.setParameter(SpeechConstant.ISV_RGN,"1");
                    mVerifier.setParameter(SpeechConstant.SAMPLE_RATE,"8000");

                }

                // 设置auth_id，不能设置为空
                mVerifier.setParameter(SpeechConstant.AUTH_ID, authId);
                // 设置业务类型为注册
                mVerifier.setParameter(SpeechConstant.ISV_SST, "train");
                // 设置声纹密码类型
                mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
                // 开始注册
                mVerifier.startListening(mRegisterListener);
                break;
            //第三步验证，与上一步注册极其相似，详情请看
         case R.id.isv_verify:
                // 清空提示信息
                mShowMsgTextView.setText("");
                // 清空参数
                mVerifier.setParameter(SpeechConstant.PARAMS, null);
                mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/"+authId+"_verify.pcm");
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
                break;
            case R.id.isv_stop_record:
                mVerifier.stopListening();
                break;
            case R.id.isv_cancel:
                mVerifier.cancel();
                initTextView();
                break;
            case R.id.back:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.isv_join:
                //joinGroup();
                break;
            case R.id.vocal:
                Intent vocal_intent=new Intent(getActivity(), VocalIdentifyActivity.class);
                vocal_intent.putExtra("group_id",Statics.GROUPID);
                startActivity(vocal_intent);
                break;
            case R.id.query:
                queryGroup();
                break;
            case R.id.isv_identity:
                Intent intent=new Intent(getActivity(), GroupManagerActivity.class);
                intent.putExtra("auth_id",authId);
                intent.putExtra("mfv_scenes", "ivp");   // 设置业务场景：声纹（ivp）
                startActivity(intent);
                break;
            default:
                break;

        }
    }

    private void deleteMember(String authId, boolean deleteGroup) {
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
        if(deleteGroup) {
            params2.append("scope=group");
        } else {
            // 删除组中指定auth_id用户
            params2.append("scope=person");
            params2.append(",auth_id="+authId);
        }
        params2.append(",group_id=" + Statics.GROUPID);
        // 执行模型操作
        mIdVerifier.execute("ipt", "delete", params2.toString(), mDeleteListener);
    }

    private void queryGroup() {
        String groupId=Statics.GROUPID;
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authId);
        // 设置模型参数，若无可以传空字符传
        StringBuffer params2 = new StringBuffer();
        params2.append("scope=group");
        params2.append(",group_id=" + groupId);
        // 执行模型操作
        mIdVerifier.execute("ipt", "query", params2.toString(), mQueryListener);
    }


    /**
     * 关闭进度条
     */
    private void stopProgress() {

        ll_add_person.setEnabled(true);
    }


    /**
     * 开启进度条
     */
    private void startProgress(String msg) {
//        mProDialog.setMessage(msg);
//        mProDialog.show();
        ll_add_person.setEnabled(false);
    }

    private void joinGroup() {
        String groupId= Statics.GROUPID;
        startProgress("正在加入组...");

        // sst=add，auth_id=eqhe，group_id=123456，scope=person
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authId);
        // 设置模型参数，若无可以传空字符传
        StringBuffer params2 = new StringBuffer();
        params2.append("auth_id=" + authId);
        params2.append(",scope=person");
        params2.append(",group_id=" + groupId);
        // 执行模型操作
        mIdVerifier.execute("ipt", "add", params2.toString(), mAddListener);

    }
    /**
     * 加入组监听器
     */
    private IdentityListener mAddListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            L.d("加入组result:"+result.getResultString());
            try {
                JSONObject resObj = new JSONObject(result.getResultString());
                // 保存到用户信息中，用来显示用户加人的组
                String group_name=resObj.getString("group_name");
                String group_id=resObj.getString("group_id");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            showTip("加入组成功");
            addToDatabase();   //成功加入组后添加到本地数据库
            stopProgress();
        }

        private void addToDatabase() {
            Imformation imformation= new Imformation();
            imformation.setAuthId(authId);
            imformation.setName(name);
            imformation.setMale(isMale);
            imformation.setRole(role);
            imformation.save();
            if (imformation.save()){
                mShowMsgTextView.setText("加入数据库成功！");
                showTip("加入数据库成功！");
            }else {
                showTip("加入数据库失败！");
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
                        deleteFromDatabase();
                        mShowMsgTextView.setText("成功从数据库删除");
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

    private void deleteFromDatabase() {
        LitePal.deleteAll(Imformation.class,"authId=?",authId);
    }

    /**
     * 查询组中成员监听器
     */
    private IdentityListener mQueryListener = new IdentityListener() {
        /* 查询组中人员结果示例：
{
    "ssub": "ipt",
    "person": [
        {
            "user": " xxxxxxxx "
        }
    ],
    "group_name": " xxxxxxxx ",
    "sst": "query",
    "ret": 0,
    "group_id": " xxxxxxxx "
}*/
        @Override
        public void onResult(IdentityResult result, boolean islast) {
            L.d("查询组中成员结果："+result.getResultString());
            showTip("查询成功");

        }
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
        @Override
        public void onError(SpeechError error) {
            L.d("查询组中成员结果："+error.getPlainDescription(true));
            //showTip(ErrorDesc.getDesc(error) + ":" + error.getErrorCode());
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
                        mResultEditText.setText("");
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        Toast.makeText(getActivity(), "删除失败，模型不存在", Toast.LENGTH_SHORT).show();
                    }
                } else if ("que".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        Toast.makeText(getActivity(), "模型存在", Toast.LENGTH_SHORT).show();
                        QueryData();
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

    private void QueryData() {
            List<Imformation>imformation =LitePal.where("authId like ?",authId).find(Imformation.class);
            Imformation im=imformation.get(0);
            StringBuilder builder = new StringBuilder();
            builder.append("ID:" + im.getAuthId()+"   ");
            builder.append("姓名: " + im.getName() + "\n");
            if ( im.getMale()) {
                builder.append("性别：男"+"  ");
            } else {
                builder.append("性别：女" + "  ");
            }
            builder.append("角色: " + im.getRole());
            mRecordTimeTextView.setText(builder.toString());
            L.d(builder.toString());



    }

    // 第三步 监听参数
    private VerifierListener mVerifyListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {

            showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onResult(VerifierResult result) {
            mShowMsgTextView.setText(result.source);

            if (result.ret == ErrorCode.SUCCESS) {
                // 验证通过 这里就意味着通过了！！！
                mShowMsgTextView.setText("验证通过,打开****"+"声纹ID："+result.vid);
            }
            else{
                // 验证不通过
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        mShowMsgTextView.setText("内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        mShowMsgTextView.setText("出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        mShowMsgTextView.setText("太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        mShowMsgTextView.setText("录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        mShowMsgTextView.setText("验证不通过，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        mShowMsgTextView.setText("音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        mShowMsgTextView.setText("音频长达不到自由说的要求");
                        break;
                    default:
                        mShowMsgTextView.setText("验证不通过,相似度仅为"+result.score+"%。");
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
                    mShowMsgTextView.setText("模型不存在，请先注册");
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
            mShowPwdTextView.setText("");
        }



       @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
           mShowPwdTextView.setText("请随便说些用于验证");
            showTip("开始说话");
        }
    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    //第二步的监听参数，通过调用result的参数获取结果
    private VerifierListener mRegisterListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            showTip("当前正在说话，音量大小：" + volume);
////            L.d("返回音频数据："+data.length);
        }

        @Override
        public void onResult(VerifierResult result) {
            mShowMsgTextView.setText(result.source);
            if (result.ret == ErrorCode.SUCCESS) {
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        mShowMsgTextView.setText("内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
                        mShowRegFbkTextView.setText("训练达到最大次数");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        mShowRegFbkTextView.setText("出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        mShowRegFbkTextView.setText("太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        mShowRegFbkTextView.setText("录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        mShowRegFbkTextView.setText("训练失败，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        mShowRegFbkTextView.setText("音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        mShowMsgTextView.setText("音频长达不到自由说的要求");
                        break;
                    default:
                        mShowRegFbkTextView.setText("");
                        break;
                }

                if (result.suc == result.rgn) {
                    mShowMsgTextView.setText("注册成功");
                    mResultEditText.setText("您的数字密码声纹ID：\n" + result.vid);
                    joinGroup();
                }

            }else {
                mShowMsgTextView.setText("注册失败，请重新开始。");
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

            if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
                mShowMsgTextView.setText("模型已存在，如需重新注册，请先删除");
                showTip("模型已存在，如需重新注册，请先删除");
            } else {
                showTip("onError Code：" + error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }
    };

//    @Override
//    public void finish() {
//        if (null != mTextPwdSelectDialog) {
//            mTextPwdSelectDialog.dismiss();
//        }
//        super.finish();
//    }
    @Override
    public void onDestroy() {
        if (null != mVerifier) {
            mVerifier.stopListening();
            mVerifier.destroy();
        }
        super.onDestroy();
    }


    private boolean checkInstance(){
        if( null == mVerifier ){
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
           Toast.makeText(getActivity(), "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化",Toast.LENGTH_SHORT ).show();
            return false;
        }else{
            return true;
        }
    }

}
