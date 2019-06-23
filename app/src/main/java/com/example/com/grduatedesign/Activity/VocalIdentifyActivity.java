package com.example.com.grduatedesign.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.grduatedesign.R;
import com.example.com.grduatedesign.Utils.L;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.record.PcmRecorder;

import org.json.JSONException;
import org.json.JSONObject;

public class VocalIdentifyActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = VocalIdentifyActivity.class.getSimpleName();

    private static final int PWD_TYPE_FREE = 2;
    // 默认为数字密码
    private int mPwdType = PWD_TYPE_FREE;
    // 用于鉴别的数字密码
    private String mIdentifyNumPwd = "";

    // 用户输入的组ID
    private String mGroupId;
    // 身份鉴别对象
    private IdentityVerifier mIdVerifier;

    // UI控件
    private TextView mResultTextView;
    private TextView mGroupIdTextView;
    private AlertDialog mTextPwdSelectDialog;
    private Toast mToast;

    // 是否已经开始业务
    private boolean mIsWorking = false;
    // 是否可以鉴别
    private boolean mCanIdentify = false;
    // 录音采样率
    private final int SAMPLE_RATE = 16000;
    // pcm录音机
    private PcmRecorder mPcmRecorder;
    // 进度对话框
    private ProgressDialog mProDialog;


    /**
     * 声纹鉴别监听器
     */
    private IdentityListener mSearchListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, "鉴别结果: "+result.getResultString());

            dismissProDialog();
            mIsWorking = false;
            handleResult(result);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if (SpeechEvent.EVENT_VOLUME == eventType) {
                showTip("音量：" + arg1);
            } else if (SpeechEvent.EVENT_VAD_EOS == eventType) {
                showTip("录音结束");
            }
        }

        @Override
        public void onError(SpeechError error) {
            mCanIdentify = false;
            dismissProDialog();
            mIsWorking = false;
            showTip(error.getPlainDescription(true));
            L.e(error.getPlainDescription(true));
        }

    };

    /**
     * 按压监听器
     */
    private View.OnTouchListener mPressTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if( null == mIdVerifier ){
                // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
                showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(!mIsWorking) {
                        vocalSearch();
                        mIsWorking = true;
                        mCanIdentify = true;
                        if(mCanIdentify) {
                            try {
                                mPcmRecorder = new PcmRecorder(SAMPLE_RATE, 40);
                                mPcmRecorder.startRecording(mPcmRecordListener);
                            } catch (SpeechError e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;


                case MotionEvent.ACTION_UP:
                    v.performClick();
                    if(mCanIdentify){
                        showProDialog("鉴别中...");
                    }
                    mIdVerifier.stopWrite("ivp");
                    if (null != mPcmRecorder) {
                        mPcmRecorder.stopRecord(true);
                        mIsWorking = false;
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    };

    /**
     * 录音机监听器
     */
    private PcmRecorder.PcmRecordListener mPcmRecordListener = new PcmRecorder.PcmRecordListener() {

        @Override
        public void onRecordStarted(boolean success) {
        }

        @Override
        public void onRecordReleased() {
        }

        @Override
        public void onRecordBuffer(byte[] data, int offset, int length) {
            StringBuffer params = new StringBuffer();
            // 子业务执行参数，若无可以传空字符传
            params.append("ptxt=" + mIdentifyNumPwd + ",");
            params.append("pwdt=" + mPwdType + ",");
            params.append(",group_id=" + mGroupId +",topc=3");
            mIdVerifier.writeData("ivp", params.toString(), data, 0, length);

        }

        @Override
        public void onError(SpeechError e) {
            L.e(e.getErrorDescription());
            dismissProDialog();
            mCanIdentify = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_vocal_identify);

        mGroupId = getIntent().getStringExtra("group_id");
        mIdVerifier = IdentityVerifier.createVerifier(VocalIdentifyActivity.this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    showTip("引擎初始化成功");
                } else {
                    showTip("引擎初始化失败，错误码：" + errorCode);
                }
            }
        });

        initUI();
    }

    private void handleResult(IdentityResult result) {
        if (null == result) {
            return;
        }
        try {
            String resultStr = result.getResultString();
            L.d(resultStr);
            JSONObject resultJson = new JSONObject(resultStr);
            if(ErrorCode.SUCCESS == resultJson.getInt("ret"))
            {
                // 保存到历史记录中
				/*DemoApp.getmHisList().addHisItem(resultJson.getString("group_id"),
						resultJson.getString("group_name") + "(" + resultJson.getString("group_id") + ")");
				FuncUtil.saveObject(VocalIdentifyActivity.this, DemoApp.getmHisList(), DemoApp.HIS_FILE_NAME);*/

                // 跳转到结果展示页面
                Intent intent = new Intent(VocalIdentifyActivity.this, ResultIdentifyActivity.class);
                intent.putExtra("result", resultStr);
                startActivity(intent);
                this.finish();
            }
            else {
                showTip("鉴别失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        TextView title = (TextView) findViewById(R.id.vocal_idf_txt_title);

        mResultTextView = (TextView) findViewById(R.id.vocal_idf_edt_result);
        mGroupIdTextView = (TextView) findViewById(R.id.vocal_idf_txt_groupid);

        findViewById(R.id.btn_vocal_idf_press_to_talk).setOnTouchListener(mPressTouchListener);

        mProDialog = new ProgressDialog(VocalIdentifyActivity.this);
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍候");
        // cancel进度框时，取消正在进行的操作
        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (null != mIdVerifier) {
                    mIdVerifier.cancel();
                }
            }
        });

        mToast = Toast.makeText(VocalIdentifyActivity.this, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.BOTTOM| Gravity.CENTER_HORIZONTAL, 0, 0);

       // mIdentifyNumPwd = VerifierUtil.generateNumberPassword(8);
        StringBuilder strBufSearch = new StringBuilder();
        strBufSearch.append("请随便说些用于验证");
        strBufSearch.append("请长按“按住说话”按钮进行鉴别！\n");
        mResultTextView.setText(strBufSearch.toString());

        mGroupIdTextView.setText(mGroupId);
    }

    private void vocalSearch() {
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 设置会话类型
        if (mPwdType==PWD_TYPE_FREE){
            mIdVerifier.setParameter(SpeechConstant.SAMPLE_RATE,"8000");

        }
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "identify");
        // 设置组ID
        mIdVerifier.setParameter("group_id", mGroupId);
        mIdVerifier.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
    //    mIdVerifier.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/ivp.wav");
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mSearchListener);
    }

    @Override
    public void finish() {
        if (null != mTextPwdSelectDialog) {
            mTextPwdSelectDialog.dismiss();
        }

        setResult(RESULT_OK);
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mIdVerifier) {
            mIdVerifier.destroy();
            mIdVerifier = null;
        }
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    private void dismissProDialog() {
        if (null != mProDialog) {
            mProDialog.dismiss();
        }
    }

    private void showProDialog(String msg) {
        if (mProDialog != null) {
            mProDialog.setMessage(msg);
            mProDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
    }



}













