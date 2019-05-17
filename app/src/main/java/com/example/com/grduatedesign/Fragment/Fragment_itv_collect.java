package com.example.com.grduatedesign.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.grduatedesign.Entity.Statics;
import com.example.com.grduatedesign.Activity.MainActivity;
import com.example.com.grduatedesign.Model.Imformation;
import com.example.com.grduatedesign.R;
import com.example.com.grduatedesign.Utils.AutoFitTextureView;
import com.example.com.grduatedesign.Utils.IatSettings;
import com.example.com.grduatedesign.Utils.JsonParser;
import com.example.com.grduatedesign.Utils.L;
import com.example.com.grduatedesign.Utils.WavMerge;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Fragment_itv_collect extends Fragment implements View.OnClickListener {
    private TextView tv_ask,itv_show,tv_im;
    private Toast mToast;
    private List<Imformation> list = new ArrayList<>();
    private List<File>inputs;
    private File interviewRecord;
    private String  itv_person,itv_name,date;
    // 身份鉴别对象
    private IdentityVerifier mIdVerifier;
    //语音听写对象
    private SpeechRecognizer mSpeechRecognizer;
    private SharedPreferences mSharedPreferences;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView texture_front;
    private AutoFitTextureView texture_back;


    /**
     * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession_front,mPreviewSession_back;;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private TextureView.SurfaceTextureListener mFrontTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera(false,width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform(false,width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };
    private TextureView.SurfaceTextureListener mBackTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera(true,width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform(true,width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };


    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo=false;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread_front;
    private HandlerThread mBackgroundThread_back;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mHandler_front;
    private Handler mHandler_back;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock_back = new Semaphore(1);
    private Semaphore mCameraOpenCloseLock_front = new Semaphore(1);


    private Button ask;
    private Button stop;
    private Button clear,record;
    private Bundle bundle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itv_collect, container, false);

        mSpeechRecognizer = SpeechRecognizer.createRecognizer(getActivity(), new InitListener() {
            @Override
            public void onInit(int i) {
                if (i != ErrorCode.SUCCESS) {
                    showTip("初始化失败，错误码：" + i);
                } else {
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
private Semaphore getSemaphore(Boolean isBack){
        if (isBack){
            return mCameraOpenCloseLock_back;
        }else {
            return mCameraOpenCloseLock_front;
        }
}

private CameraCaptureSession getCameraSession(boolean isBack){
    if (isBack){
        return mPreviewSession_back;
    }else {
        return mPreviewSession_front;
    }
}
    private void initView(View view) {

        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        tv_ask = view.findViewById(R.id.tv_ask);
        tv_im=view.findViewById(R.id.tv_im);
       itv_show=view.findViewById(R.id.itv_show);
        ask = view.findViewById(R.id.ask);
        stop = view.findViewById(R.id.end);
        ask.setOnClickListener(this);
        stop.setOnClickListener(this);
        clear=view.findViewById(R.id.clear);
        clear.setOnClickListener(this);
        record=view.findViewById(R.id.front_record);
        record.setOnClickListener(this);
        view.findViewById(R.id.answer).setOnClickListener(this);
        view.findViewById(R.id.front_cam).setOnClickListener(this);
        view.findViewById(R.id.back_cam).setOnClickListener(this);
        mSharedPreferences = getActivity().getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        texture_front = view.findViewById(R.id.texture_front);
        texture_back=view.findViewById(R.id.texture_back);

        bundle=getArguments();   //获得访谈设置
        checkBundle(bundle);

        mIdVerifier=IdentityVerifier.createVerifier(getActivity(), new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    L.d("IdVerifier引擎初始化成功");
                } else {
                   L.d("引擎初始化失败，错误码：" + errorCode);
                }
            }
        });
    }

    private void checkBundle(Bundle bundle){
        if (bundle!=null) {
            itv_person = getArguments().getString("itv_person");
            itv_name = getArguments().getString("itv_name");
            date = getArguments().getString("date");
            tv_im.setText("访谈对象："+itv_person+"       "+"访谈命名："+itv_name+"     "+"访谈日期："+date);
            L.d(itv_person + "" + itv_name + " " + date);
        }else {
            final MainActivity activity= (MainActivity) getActivity();
            AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
            dialog.setTitle("提示")
                    .setMessage("请先设置访谈信息！")
                    .setPositiveButton("好", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.changeTab(0);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }
    /**
     * 参数设置
     * @return
     */
    public void setParamRecognizer() {
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
        mSpeechRecognizer.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "2000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mSpeechRecognizer.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mSpeechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mSpeechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat/"+itv_name+"/samples/"+ System.currentTimeMillis()+".wav");
    }

    int ret = 0;
boolean isAsk;
    @Override
    public void onClick(final View view) {
        checkBundle(bundle);
        switch (view.getId()) {
            case R.id.ask:
                isAsk=true;
                tv_ask.setText("");
                mIatResults.clear();
                // 设置参数
                setParamRecognizer();
                vocalSearch();
                mSpeechRecognizer.startListening(listener);
                if (ret != ErrorCode.SUCCESS) {
                    showTip("听写失败,错误码：" + ret);
                } else {
                    showTip(getString(R.string.text_begin));
                }

                break;
            case R.id.answer:
                isAsk=false;
                tv_ask.setText("");
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
                AlertDialog.Builder confrimDialog=new AlertDialog.Builder(getActivity());
                confrimDialog.setTitle("提示")
                        .setMessage("是否结束本次访谈?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final File[] files = new File(Environment.getExternalStorageDirectory() + "/msc/iat/"+itv_name+"/samples/").listFiles();
                                final List<File> list;
                                if (files == null) {
                                    list = new ArrayList<>();
                                } else {
                                    list = Arrays.asList(files);
                                    Collections.sort(list, new Comparator<File>() {
                                        @Override
                                        public int compare(File o1, File o2) {
                                            if (o1.isDirectory() && o2.isFile())
                                                return -1;
                                            if (o1.isFile() && o2.isDirectory())
                                                return 1;
                                            return o1.getName().compareTo(o2.getName());
                                        }
                                    });
                                }
                                //判断是否保存
                                final AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                                builder.setTitle("提示")
                                        .setMessage("已结束,是否保存本次访谈?")
                                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                try {
                                                    if (date!=null&&itv_name!=null&&list!=null) {
                                                        if (files!=null&&!TextUtils.isEmpty(itv_show.getText())) {
                                                            WavMerge.mergeWav(list, new File(Environment.getExternalStorageDirectory() + "/msc/iat/" + itv_name + "/" + date + itv_name + "采访（音）.wav"));
                                                            FileOutputStream outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() +
                                                                    "/msc/iat/" + itv_name + "/" + date + itv_name + "采访（文）.txt");
                                                            outputStream.write(itv_show.getText().toString().getBytes());
                                                            outputStream.close();

                                                            showTip("已保存到" + Environment.getExternalStorageDirectory() + "/msc/iat/" + itv_name + "/" + date + itv_name + "采访（音）.wav");
                                                            tv_ask.setText("请按开始并说话");
                                                            itv_show.setText("");
                                                            bundle = null;
                                                        }else {
                                                            showTip("保存失败，没有访谈内容！");
                                                        }
                                                    }
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        })
                                        .setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                File samplefiles=new File(Environment.getExternalStorageDirectory() + "/msc/iat/"+itv_name);
                                                deleteFile(samplefiles);
                                                tv_ask.setText("请按开始并说话");
                                                itv_show.setText("");
                                                bundle = null;
                                            }
                                        })
                                        .setCancelable(false);
                                builder.create().show();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();


                break;
            case R.id.clear:
                File samplefiles=new File(Environment.getExternalStorageDirectory() + "/msc/iat/"+itv_name);
                deleteFile(samplefiles);
                tv_ask.setText("请按开始并说话");
                itv_show.setText("");
                break;
            case R.id.front_record:
                //这里怎么写？
                recordHandler.sendEmptyMessage(1);

                break;
            case  R.id.front_cam:
                if (texture_front.isAvailable()) {
                    openCamera(false,texture_front.getWidth(), texture_front.getHeight());
                } else {
                    texture_front.setSurfaceTextureListener(mFrontTextureListener);
                }
                break;
            case R.id.back_cam:
                if (texture_back.isAvailable()) {
                    openCamera(true,texture_back.getWidth(), texture_back.getHeight());
                } else {
                    texture_back.setSurfaceTextureListener(mBackTextureListener);
                }
                break;
        }
    }

    //flie：要删除的文件夹的所在位置
    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
           file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 声纹鉴别监听器
     */
    private IdentityListener mSearchListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
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
            showTip(error.getPlainDescription(true));
        }

    };

    private void vocalSearch() {
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 设置会话类型
            mIdVerifier.setParameter(SpeechConstant.SAMPLE_RATE,"8000");
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "identify");
        // 设置组ID
        mIdVerifier.setParameter("group_id", Statics.GROUPID);
        mIdVerifier.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        //mIdVerifier.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/ivp.wav");
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mSearchListener);
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
//                Intent intent = new Intent(getContext(), ResultIdentifyActivity.class);
//                intent.putExtra("result", resultStr);
//                startActivity(intent);

            }
            else {
                showTip("鉴别失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private RecognizerListener listener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

            }

        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            showTip("结束说话");
            tv_ask.setText("请按开始并说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean b) {
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

            StringBuffer resultBuffer=new StringBuffer();
            if (isAsk) {
                resultBuffer.append("问：");
            }else {
                resultBuffer.append(itv_person+"：");
            }
            //加入说话人身份
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }
            tv_ask.setTextColor(Color.RED);
            tv_ask.setText(resultBuffer.toString());
            if (b) {
                itv_show.append(tv_ask.getText() + "\n");
            }
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

    }

//-----------------------------------------------------以下是录像部分-------------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private void stopRecordingVideo() {
    // UI
    mIsRecordingVideo = false;
    record.setText("开始录制");
    // Stop recording
    mMediaRecorder.stop();
    mMediaRecorder.reset();

    Activity activity = getActivity();
    if (null != activity) {
        Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
                Toast.LENGTH_SHORT).show();
        L.d( "Video saved: " + mNextVideoAbsolutePath);
    }
    mNextVideoAbsolutePath = null;
    startPreview(false);
}
    private void startRecordingVideo() {          //录front camera
        if (null == mCameraDevice || !texture_front.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            L.d("这里");
            closePreviewSession(false);
            setUpMediaRecorder();
            SurfaceTexture texture = texture_front.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession_front = cameraCaptureSession;
                    //updatePreview();
                    if (null == mCameraDevice) {
                        return;
                    }
                    try {
                        setUpCaptureRequestBuilder(mPreviewBuilder);
                        HandlerThread thread = new HandlerThread("Camera_front_preview");
                        thread.start();
                        mPreviewSession_front .setRepeatingRequest(mPreviewBuilder.build(), null, mHandler_front);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // UI
                            mIsRecordingVideo = true;
                            record.setText("停止录制");
                            // Start recording
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            },mHandler_front);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }
    private void setUpMediaRecorder() throws IOException {
       final Activity activity = getActivity();
       if (null == activity) {
        return;
    }
    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
        mNextVideoAbsolutePath = getVideoFilePath();
    }
    mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
    mMediaRecorder.setVideoEncodingBitRate(10000000);
    mMediaRecorder.setVideoFrameRate(30);
    mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    switch (mSensorOrientation) {
        case SENSOR_ORIENTATION_DEFAULT_DEGREES:
            mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
            break;
        case SENSOR_ORIENTATION_INVERSE_DEGREES:
            mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
            break;
    }
    mMediaRecorder.prepare();
}

    private String getVideoFilePath() {
        final File dir = Environment.getExternalStorageDirectory();
        return (dir == null ? "" : (dir.getAbsolutePath() + "/msc/"))
                + System.nanoTime()+ ".mp4";
    }
    private void configureTransform(boolean isBack,int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null ==getTexture(isBack) || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        getTexture(isBack).setTransform(matrix);
    }
    private AutoFitTextureView getTexture(boolean isBack){
        if (isBack){
            return texture_back;
        }else {
            return texture_front;
        }
}

    @Override
    public void onResume() {
        super.onResume();

        if (texture_front.isAvailable()) {
            openCamera(false,texture_front.getWidth(), texture_front.getHeight());
        } else {
            texture_front.setSurfaceTextureListener(mFrontTextureListener);
        }
        if (texture_back.isAvailable()) {
            openCamera(true,texture_back.getWidth(), texture_back.getHeight());
        } else {
            texture_back.setSurfaceTextureListener(mBackTextureListener);
        }
        startBackgroundThread();

    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock_back.acquire();
            mCameraOpenCloseLock_front.acquire();
            closePreviewSession(true);
            closePreviewSession(false);
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock_back.release();
            mCameraOpenCloseLock_front.release();
        }
    }

    private Integer mSensorOrientation;
    private String mNextVideoAbsolutePath;
    private CaptureRequest.Builder mPreviewBuilder;



    @SuppressLint("MissingPermission")
    private void openCamera(boolean isBack,int width, int height) {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions();
            return;
        }
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!getSemaphore(isBack).tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId;
            if (isBack){
                cameraId = manager.getCameraIdList()[0];      //0后   1前
                // Choose the sizes for camera preview and video recording
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                if (map == null) {
                    throw new RuntimeException("Cannot get available preview/video sizes");
                }
                mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        width, height, mVideoSize);

                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    texture_back.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    texture_back.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
                configureTransform(true,width, height);
                mMediaRecorder = new MediaRecorder();
                manager.openCamera(cameraId, mStateCallback_back, null);
            }else {
                cameraId = manager.getCameraIdList()[1];     //0后   1前
            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                texture_front.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                texture_front.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(false,width, height);
            mMediaRecorder = new MediaRecorder();
            manager.openCamera(cameraId, mStateCallback_front, null);
            }

        } catch (CameraAccessException e) {
           showTip("Cannot access the camera.");
            activity.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }
    private CameraDevice.StateCallback mStateCallback_back = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview(true);
            mCameraOpenCloseLock_back.release();
            if (null != texture_back) {
                configureTransform(true,texture_back.getWidth(), texture_back.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock_back.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock_back.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    private CameraDevice.StateCallback mStateCallback_front = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview(false);
            mCameraOpenCloseLock_front.release();
            if (null != texture_front) {
                configureTransform(false,texture_front.getWidth(), texture_front.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock_front.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock_front.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    private void startPreview(final Boolean isBack) {
            if (null == mCameraDevice || !getTexture(isBack).isAvailable() || null == mPreviewSize) {
                return;
            }
            final String threadName;
            if (isBack){
            threadName="Camera_back_preview";
        }else {
            threadName="Camera_front_preview";
        }
            try {
                closePreviewSession(isBack);
                SurfaceTexture texture = getTexture(isBack).getSurfaceTexture();
                assert texture != null;
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                Surface previewSurface = new Surface(texture);
                mPreviewBuilder.addTarget(previewSurface);
                mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                        new CameraCaptureSession.StateCallback() {

                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                if (isBack) {
                                    mPreviewSession_back = session;
                                }else {
                                    mPreviewSession_front=session;
                                }
                                //updatePreview
                                if (null == mCameraDevice) {
                                    return;
                                }
                                try {
                                    setUpCaptureRequestBuilder(mPreviewBuilder);
                                    HandlerThread thread = new HandlerThread(threadName);
                                    thread.start();
                                    if (isBack) {
                                        mPreviewSession_back.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler_back);
                                    }else {
                                        mPreviewSession_front.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler_front);
                                    }
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                Activity activity = getActivity();
                                if (null != activity) {
                                    Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, getHandler(isBack));
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

    }
    private Handler getHandler(boolean isBack){
        if (isBack){
            return mHandler_back;
        }else {
            return mHandler_front;
        }
    }
    private void closePreviewSession(boolean isBack) {
        if (getCameraSession(isBack)!= null) {
            getCameraSession(isBack).close();
            if (isBack){
                mPreviewSession_back=null;
            }else {
                mPreviewSession_front=null;
            }
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }
    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            L.e( "Couldn't find any suitable preview size");
            return choices[0];
        }
    }
    private static Size chooseVideoSize(Size[] choices) {
            for (Size size : choices) {
                if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                    return size;
                }
            }
            L.e( "Couldn't find any suitable video size");
            return choices[choices.length - 1];
    }
    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Requests permissions needed for recording video.
     */
    private void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS.toString())) {
            new ConfirmationDialog().show(getChildFragmentManager(),FRAGMENT_DIALOG);
        } else {
            requestPermissions( VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    private Handler recordHandler=new Handler(){
        @Override
        public void handleMessage(Message message) {
            switch (message.what){
                case 1:
                    if (mIsRecordingVideo) {
                    stopRecordingVideo();
                } else {
                  startRecordingVideo();
                }
                default:
            }
        }
    };
    private void startBackgroundThread() {
        mBackgroundThread_front = new HandlerThread("Camera_front");
        mBackgroundThread_front.start();
        mHandler_front = new Handler(mBackgroundThread_front.getLooper());

        mBackgroundThread_back=new HandlerThread("Camera_back");
        mBackgroundThread_back.start();
        mHandler_back=new Handler(mBackgroundThread_back.getLooper());
    }
    private void stopBackgroundThread() {
        if (mBackgroundThread_front!=null) {
            mBackgroundThread_front.quitSafely();
            try {
                mBackgroundThread_front.join();
                mBackgroundThread_front = null;
                mHandler_front = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mBackgroundThread_back!=null) {
            mBackgroundThread_back.quitSafely();
            try {
                mBackgroundThread_back.join();
                mBackgroundThread_back = null;
                mHandler_back = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpeechRecognizer.cancel();
        mSpeechRecognizer.destroy();
    }
    public static class ConfirmationDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_request)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions( VIDEO_PERMISSIONS,
                                    REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parent.getActivity().finish();
                                }
                            })
                    .create();
        }

    }

}
