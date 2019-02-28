package com.example.com.grduatedesign.Fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Camera;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.grduatedesign.Model.Imformation;
import com.example.com.grduatedesign.R;
import com.example.com.grduatedesign.Utils.IatSettings;
import com.example.com.grduatedesign.Utils.JsonParser;
import com.example.com.grduatedesign.Utils.L;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Fragment_itv_collect extends Fragment implements View.OnClickListener {
    private TextView tv_speak;
    private Toast mToast;
    private  List<Imformation>list=new ArrayList<>();
    //语音听写对象
    private SpeechRecognizer mSpeechRecognizer;
    private SharedPreferences mSharedPreferences;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private Button ask;
    private Button stop;
    private Button start_record;
    private boolean isAsk;
    private SurfaceView mSurfaceview;
    private boolean mStartedFlg = false;
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private Camera myCamera;
    private boolean isView = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_itv_collect,container,false);

        mSpeechRecognizer=SpeechRecognizer.createRecognizer(getActivity(), new InitListener() {
            @Override
            public void onInit(int i) {
                if (i!= ErrorCode.SUCCESS) {
                    showTip("初始化失败，错误码：" + i);
                }else {
                    L.d("初始化成功！");
                    showTip("初始化成功！");
                }
            }

        });
        mSpeechRecognizer = SpeechRecognizer.createRecognizer(getActivity(), null);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }
    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
    private void initView(View view) {
        mToast=Toast.makeText(getActivity(),"",Toast.LENGTH_SHORT);
        tv_speak=view.findViewById(R.id.tv_speak);
        ask=view.findViewById(R.id.ask);
        stop=view.findViewById(R.id.end);
        ask.setOnClickListener(this);
        stop.setOnClickListener(this);
        mSharedPreferences = getActivity().getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
    }

    /**
     * 参数设置
     * @return
     */
    public void setParamRecognizer(){
//        1.创建语音听写对象   第二个参数与服务方式关联  本地服务的话传初始化监听器，云服务的话传null
//        if (mSpeechRecognizer == null) {
//            mSpeechRecognizer = SpeechRecognizer.createRecognizer(MainActivity.this, null);
//        }
        //2.设置参数
//       mSpeechRecognizer.setParameter(SpeechConstant.DOMAIN, "iat");
//        mSpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
//        mSpeechRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin");
//        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
//        mSpeechRecognizer.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));



// 清空参数
        mSpeechRecognizer.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mSpeechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mSpeechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mSpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mSpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mSpeechRecognizer.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mSpeechRecognizer.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mSpeechRecognizer.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1500"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mSpeechRecognizer.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mSpeechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mSpeechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    int ret=0;
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ask:
                tv_speak.setText("");
                mIatResults.clear();
                // 设置参数
                setParamRecognizer();
                mSpeechRecognizer.startListening(listener);
                if (ret != ErrorCode.SUCCESS) {
                    showTip("听写失败,错误码：" + ret);
                } else {
                   showTip(getString(R.string.text_begin));
                }

                break;
            case R.id.end:
                mSpeechRecognizer.stopListening();
                break;
        }
    }
    private RecognizerListener listener=new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }

        @Override
        public void onEndOfSpeech(){
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
           String resultText= JsonParser.parseIatResult(recognizerResult.getResultString());
            L.d(resultText);
            printResult(recognizerResult);
        }

        @Override
        public void onError(SpeechError speechError) {
            showTip(speechError.getErrorDescription());
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        //加入说话人身份
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        tv_speak.setText(resultBuffer.toString());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpeechRecognizer.cancel();
        mSpeechRecognizer.destroy();
    }
}
