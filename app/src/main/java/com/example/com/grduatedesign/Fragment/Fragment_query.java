package com.example.com.grduatedesign.Fragment;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.grduatedesign.Entity.Statics;
import com.example.com.grduatedesign.R;
import com.example.com.grduatedesign.Utils.L;
import com.example.com.grduatedesign.Utils.LoadTxtFile;
import com.example.com.grduatedesign.Utils.TextSearchFile;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.lognteng.editspinner.lteditspinner.LTEditSpinner;

public class Fragment_query extends Fragment implements View.OnClickListener {
    private LTEditSpinner<String> ltEditSpinner;
    private List<File>list;
    private File file;
    private String itv_name;
    private String wavPath;
    private String txtName;
    private Toast mToast;
    private TextView query_content;
    private PlayerView playerView;   //录音  mp3
    private SimpleExoPlayer wavPlayer;   //录音  mp3
    private boolean playWhenReady;
    private int currentWindow;
    private long playbackPosition;
    private MediaSource mediaSource;
    private MyEventlistener1 myEventlistener;
    private SpeechSynthesizer mTts;
    private String STATE="stop";
private Button play_pause;
    private ScrollView man_scrollView;
    private Button load, btn_videoplay;
    private SimpleExoPlayer exoplayer_back,exoplayer_front;              //录像（前后）
    private PlayerView exoplayerView_back,exoplayerView_front;       //录像（前后）
    private  MediaSource videoSource_back,videoSource_front;
    private boolean isPlayingViedo=false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_query, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    private void initView(View view) {
        ltEditSpinner = view.findViewById(R.id.ltd_spinner);
        String pathname =Statics.PATH_INTERVIEW;
        File file = new File(pathname);
        File[] files = file.listFiles();

        if (files == null) {
            list = new ArrayList<>();
        } else {
            list = Arrays.asList(files);
        }
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
         List<String> dirname = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int start = list.get(i).getPath().lastIndexOf("/");
            dirname.add(list.get(i).getPath().substring(start + 1));
        }
        ltEditSpinner.initData(dirname, new LTEditSpinner.OnESItemClickListener() {
            @Override
            public void onItemClick(Object o) {
            }
        });

        man_scrollView=view.findViewById(R.id.query_scrollView);
        load=view.findViewById(R.id.query_loadfile);
        load.setOnClickListener(this);
        view.findViewById(R.id.itv_delete).setOnClickListener(this);
        play_pause=view.findViewById(R.id.itv_play);
        play_pause.setOnClickListener(this);
        mToast=Toast.makeText(getActivity(),"",Toast.LENGTH_SHORT);
        playerView=view.findViewById(R.id.wav_view);
        exoplayerView_back =view.findViewById(R.id.back_video);
        exoplayerView_front=view.findViewById(R.id.front_video);
        query_content=view.findViewById(R.id.query_content);
        btn_videoplay =view.findViewById(R.id.video_play);
        btn_videoplay.setOnClickListener(this);
        myEventlistener=new MyEventlistener1();
        query_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mTts = SpeechSynthesizer.createSynthesizer(getActivity() ,null);
        set_mTts();


    }

    private void set_mTts() {
        // 设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");

        // 设置语速
        mTts.setParameter(SpeechConstant.SPEED, "20");

        // 设置音调
        mTts.setParameter(SpeechConstant.PITCH, "50");

        // 设置音量0-100
        mTts.setParameter(SpeechConstant.VOLUME, "100");

        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 如果不需要保存保存合成音频，请注释下行代码
        // mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
        // "./sdcard/iflytek.pcm");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.query_loadfile:
                itv_name=ltEditSpinner.getValue();
                if (!itv_name.equals("")) {
                    String path = Statics.PATH_INTERVIEW + itv_name;
                    file = new File(path);
                    List<File> txtlist = TextSearchFile.searchFiles(file, ".txt");
                    for (int i = 0; i < txtlist.size(); i++) {
                        // L.d(txtlist.get(i).getName());
                    }
                    File txtfile = txtlist.get(0);
                    txtName = txtlist.get(0).getPath();
                    L.d("txtName:" + txtName);
                    List<String> result = LoadTxtFile.txtList(txtfile);
                    if (result != null) {
                        query_content.setText("");
                        for (int i = 0; i < result.size(); i++) {
                            query_content.append(result.get(i));
                        }
                    }

                    //重置播放器
                    if (wavPlayer != null) {
                        wavPlayer.stop(true);
                    }
                    List<File> wavlist = TextSearchFile.searchFiles(file, ".wav");
                    if (wavlist != null) {
                        wavPath = wavlist.get(0).getPath();
                    }

                    if (query_content != null) {
                        initPlayer();
                        initBackVideo();
                        initFrontVideo();
                        showTip("已加载完成");
                    } else {
                        showTip("加载失败");
                    }
                }else {
                    showTip("请选择一个访谈");
                }
                break;
            case R.id.itv_delete:
                if (itv_name!=null){
                    File file=new File(Statics.PATH_INTERVIEW+itv_name);
                    if (file.exists()) {
                        deleteFile(file);    //删除文件夹
                        showTip("已删除该访谈！");
                       //这里还有清除界面，数据重置未完成，留给后续
                    }else {
                        showTip("访谈'"+itv_name+"'不存在！");
                    }
                }
                break;
            case R.id.itv_play:   //朗读
                if (!query_content.getText().equals("")){
                    if (STATE=="stop") {
                        mTts.startSpeaking(query_content.getText().toString(), mTtsListener);
                    }else if (STATE=="playing"){
                        mTts.pauseSpeaking();
                    }else if (STATE=="pause"){
                        mTts.resumeSpeaking();
                    }
                }else {
                    showTip("请读取一个访谈");
                }

                break;
            case  R.id.video_play :
             
                break;
        }
    }

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

    private void initFrontVideo() {
        File video_front = null;
        List<File> files = TextSearchFile.searchFiles(new File(Statics.PATH_INTERVIEW + itv_name), "front.mp4");
        if (files.size()==0){
            return;
        }
        video_front= files.get(0);
         /*You can create an ExoPlayer instance using ExoPlayerFactory. The factory provides a range of methods for creating
        ExoPlayer instances with varying levels of customization. For the vast majority of use cases one of the ExoPlayerFactory.
        newSimpleInstance methods should be used.
        These methods return SimpleExoPlayer, which extends ExoPlayer to add additional high level wavPlayer functionality. */
        exoplayer_front = ExoPlayerFactory.newSimpleInstance(getActivity());
        exoplayerView_front.setPlayer(exoplayer_front);
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(),
                Util.getUserAgent(getActivity(), "yourApplicationName"));
// This is the MediaSource representing the media to be played.
        videoSource_front= new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(video_front));
// Prepare the wavPlayer with the source.
        exoplayer_front.prepare(videoSource_front);
    }

    private void initBackVideo() {
        File video_back = null;
        List<File> files = TextSearchFile.searchFiles(new File(Statics.PATH_INTERVIEW + itv_name), "back.mp4");
        if (files.size()==0){
           return;
        }
        video_back= files.get(0);
         /*You can create an ExoPlayer instance using ExoPlayerFactory. The factory provides a range of methods for creating
        ExoPlayer instances with varying levels of customization. For the vast majority of use cases one of the ExoPlayerFactory.
        newSimpleInstance methods should be used.
        These methods return SimpleExoPlayer, which extends ExoPlayer to add additional high level wavPlayer functionality. */
        exoplayer_back = ExoPlayerFactory.newSimpleInstance(getActivity());
        exoplayerView_back.setPlayer(exoplayer_back);
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(),
                Util.getUserAgent(getActivity(), "yourApplicationName"));
// This is the MediaSource representing the media to be played.
        videoSource_back = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(video_back));
// Prepare the wavPlayer with the source.
        exoplayer_back.prepare(videoSource_back);
    }

    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        // 缓冲进度回调，arg0为缓冲进度，arg1为缓冲音频在文本中开始的位置，arg2为缓冲音频在文本中结束的位置，arg3为附加信息
        @Override
        public void onBufferProgress(int progress, int beginPos, int endPos, String arg3) {
            // TODO Auto-generated method stub

        }

        // 会话结束回调接口，没有错误时error为空
        @Override
        public void onCompleted(SpeechError error) {
            L.d("SpeakCompleted");
            mTts.stopSpeaking();
            play_pause.setText("朗读");
            STATE="stop";

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }

        // 开始播放
        @Override
        public void onSpeakBegin() {
            L.d("SpeakBegin");
            play_pause.setText("暂停");
            STATE="playing";

        }

        // 停止播放
        @Override
        public void onSpeakPaused() {
            L.d("SpeakPaused");
            play_pause.setText("继续");
            STATE="pause";

        }

        // 播放进度回调,arg0为播放进度0-100；arg1为播放音频在文本中开始的位置，arg2为播放音频在文本中结束的位置。
        @Override
        public void onSpeakProgress(int arg0, int arg1, int arg2) {

        }

        // 恢复播放回调接口
        @Override
        public void onSpeakResumed() {
            L.d("SpeakResumed");
            play_pause.setText("暂停");
            STATE="playing";

        }

    };
    private void initPlayer() {
        wavPlayer = ExoPlayerFactory.newSimpleInstance(
                getActivity(), new DefaultRenderersFactory(getActivity()), new DefaultTrackSelector(), new DefaultLoadControl());

        playerView.setPlayer(wavPlayer);
        playerView.setControllerHideOnTouch(false);

        wavPlayer.setPlayWhenReady(playWhenReady);
        wavPlayer.seekTo(currentWindow, playbackPosition);
        playerView.setControllerShowTimeoutMs(100000);
        //创建wav文件
        //http://www.170mv.com/kw/other.web.nf01.sycdn.kuwo.cn/resource/n2/29/58/1319188966.mp3
        Uri uri=Uri.parse(wavPath);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), Util.getUserAgent(getContext(),"MyApplication"));
        mediaSource=new ExtractorMediaSource.Factory(
                dataSourceFactory).createMediaSource(uri);
        if (mediaSource!=null) {

            wavPlayer.addListener(myEventlistener);
            wavPlayer.prepare(mediaSource, false, true);

        }else {
            showTip("mediaSource   null");
        }


    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    private class MyEventlistener1 implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_ENDED:
                    showTip("播放结束");
                    break;
                case Player.STATE_READY:

            }
        }
    }
    private void releasePlayer() {
        if (wavPlayer != null) {
            playbackPosition = wavPlayer.getCurrentPosition();
            currentWindow = wavPlayer.getCurrentWindowIndex();
            playWhenReady = wavPlayer.getPlayWhenReady();
            wavPlayer.release();
            wavPlayer = null;
        }

    }
    @Override
    public void onDestroy() {
        if (wavPlayer != null) {
            wavPlayer.removeListener(myEventlistener);
            releasePlayer();
        }
        if (exoplayer_back != null) {
            //     exoplayer_back.removeListener(myEventlistener);
            releaseExoPlayerBack();
        }
        if (exoplayer_front != null) {
            releaseExoPlayerfront();
        }
            mTts.stopSpeaking();
            mTts.destroy();// 退出时释放连接
            super.onDestroy();
        }

    private void releaseExoPlayerBack() {
        if (exoplayer_back != null) {
            playbackPosition = exoplayer_back.getCurrentPosition();
            currentWindow = exoplayer_back.getCurrentWindowIndex();
            playWhenReady = exoplayer_back.getPlayWhenReady();
            exoplayer_back.release();
            exoplayer_back = null;
        }
    }
        private void releaseExoPlayerfront(){
            if (exoplayer_front != null) {
                playbackPosition = exoplayer_front.getCurrentPosition();
                currentWindow = exoplayer_front.getCurrentWindowIndex();
                playWhenReady = exoplayer_front.getPlayWhenReady();
                exoplayer_front.release();
                exoplayer_front = null;
            }
        }


    @Override
    public void onPause() {
        // 暂停播放
        if (wavPlayer != null) {
            wavPlayer.stop();
        }
        if (exoplayer_back!=null){
            exoplayer_back.stop();
        }
        if (exoplayer_front!=null){
            exoplayer_front.stop();
        }
       if (mTts.isSpeaking()){
           mTts.pauseSpeaking();
       }
        super.onPause();
    }

    @Override
    public void onResume() {
        // 继续播放
        if (wavPlayer != null) {
            wavPlayer.prepare(mediaSource, false, false);
        }
        if (exoplayer_back != null) {
            exoplayer_back.prepare(videoSource_back);
        }
        if (exoplayer_front!= null) {
            exoplayer_front.prepare(videoSource_front);
        }
        super.onResume();
    }
}
