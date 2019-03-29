package com.example.com.grduatedesign.Fragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.com.grduatedesign.R;
import com.example.com.grduatedesign.Utils.L;
import com.example.com.grduatedesign.Utils.LoadTxtFile;
import com.example.com.grduatedesign.Utils.TextSearchFile;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Fragment_contrast extends Fragment implements View.OnClickListener, Player.EventListener {
    private Spinner spinner;
    private EditText content;
    private int position;
    private List<File>list;
    private File file1;
    private String wavPath;
    private String txtName;
    private Toast mToast;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady;
    private int currentWindow;
    private long playbackPosition;
    private MediaSource mediaSource;
    private MyEventlistener myEventlistener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_contrast,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    private void initView(View view) {
        mToast=Toast.makeText(getActivity(),"",Toast.LENGTH_SHORT);
        spinner=view.findViewById(R.id.spinner);
        content=view.findViewById(R.id.contrast_content);
        playerView=view.findViewById(R.id.video_view);
        myEventlistener=new MyEventlistener();

        view.findViewById(R.id.loadfile).setOnClickListener(this);
        view.findViewById(R.id.save).setOnClickListener(this);
        String pathname=Environment.getExternalStorageDirectory() + "/msc/iat/";
        File file=new File(pathname);
        File[]files=file.listFiles();

        if (files==null){
            list=new ArrayList<>();
        }else {
            list= Arrays.asList(files);
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
        List<String>dirname =new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int start=list.get(i).getPath().lastIndexOf("/");
            dirname.add(list.get(i).getPath().substring(start+1));
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<>(getActivity(),R.layout.support_simple_spinner_dropdown_item,dirname);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setDropDownVerticalOffset(100);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        content.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        spinner.setSelection(position,true);
        L.d("click");
    }
});





    }

    private void initPlayer() {
            player = ExoPlayerFactory.newSimpleInstance(
                    getActivity(), new DefaultRenderersFactory(getActivity()), new DefaultTrackSelector(), new DefaultLoadControl());

            playerView.setPlayer(player);
            playerView.setControllerHideOnTouch(false);

            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
            playerView.setControllerShowTimeoutMs(100000);
        //创建wav文件
        //http://www.170mv.com/kw/other.web.nf01.sycdn.kuwo.cn/resource/n2/29/58/1319188966.mp3
        Uri uri=Uri.parse(wavPath);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), Util.getUserAgent(getContext(),"MyApplication"));
       mediaSource=new ExtractorMediaSource.Factory(
                dataSourceFactory).createMediaSource(uri);
        if (mediaSource!=null) {

            player.addListener(myEventlistener);
            player.prepare(mediaSource, false, true);

        }else {
            showTip("mediaSource   null");
        }


    }
    private class MyEventlistener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_ENDED:
                    showTip("播放结束");
                    break;
                case Player.STATE_READY:

            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }
    }
    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(runnable,100);
        }
    };


    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.loadfile:
                position=spinner.getSelectedItemPosition();
                String path=list.get(position).getPath();
                file1=new File(path);
                 //加载对应文本txt
                List<File>txtlist=TextSearchFile.searchFiles(file1,".txt");
                for (int i=0;i<txtlist.size();i++){
                   // L.d(txtlist.get(i).getName());
                }
                File txtfile=txtlist.get(0);
                txtName=txtlist.get(0).getPath();
                L.d("txtName:"+txtName);
                List<String>result= LoadTxtFile.txtList(txtfile);
                if (result!=null) {
                    content.setText("");
                    for (int i = 0; i < result.size(); i++) {
                        content.append(result.get(i));
                    }
                }
                //重置播放器
                if (player!=null) {
                    player.stop(true);
                }
                List<File>wavlist=TextSearchFile.searchFiles(file1,".wav");
                if (wavlist!=null) {
                    wavPath = wavlist.get(0).getPath();
                }

                if (content!=null) {
                    initPlayer();
                    showTip("已加载完成");
                }else {
                    showTip("加载失败");
                }
                break;
            case R.id.save:
                try {
                    FileOutputStream outputStream=new FileOutputStream(txtName);
                    outputStream.write(content.getText().toString().getBytes());
                    outputStream.close();
                    showTip("已保存");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }
    }


    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }



    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        if (player!=null) {
            player.removeListener(myEventlistener);
            releasePlayer();
        }
        super.onDestroy();

    }

    @Override
    public void onPause() {
         // 暂停播放
        if (player != null) {
            player.stop();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        // 继续播放
        if (player != null) {
            player.prepare(mediaSource, false, false);
        }
        handler.post(runnable);   //通过Handler启动Runnable
        super.onResume();
    }
}
