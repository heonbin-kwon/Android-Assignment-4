package com.example.audioplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public void checkPermission(){
        int permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permissionWrite != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(this, "이 앱을 실행하기 위해 권한이 필요합니다.",Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},MODE_PRIVATE);

        }
    }

    private ListView listView;
    int currentPlay = 0;
    ArrayList<String> mp3List;
    String mp3Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    String selectMp3;
    MediaPlayer mPlayer;
    boolean[] Playing;
    SeekBar currentSB = null;
    Thread MThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setTitle("DCU Music Player");
        // 저장소 접근 허가 요청
        checkPermission();

        mp3List = new ArrayList<String>();

        File[] listFiles = new File(mp3Path).listFiles();

        String fileName, extName;

        for (File file : listFiles) {
            fileName = file.getName();
            extName = fileName.substring(fileName.length() - 3);
            if (extName.equals((String) "mp3"))
                mp3List.add(fileName);
        }

        for(int i= 0; mp3List.size() <= i; i++){
            System.out.println(mp3List.get(i));
        }

        listView = (ListView) findViewById(R.id.listView3);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.audioview, R.id.musicName, mp3List) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView iv = view.findViewById(R.id.music1);
                if (currentPlay == position) {
                    iv.setImageResource(R.drawable.v);
                    view.setBackgroundColor(Color.parseColor("#991D1B20"));
                } else {
                    iv.setImageResource(R.drawable.a);
                    view.setBackgroundColor(Color.parseColor("#1D1B20"));
                }
                return view;
            }
        };
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);
        listView.setItemChecked(0, true);

        boolean[] Playing = new boolean[mp3List.size()];

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mPlayer != null){
                    mPlayer.stop();
                    mPlayer.release();
                    mPlayer = null;
                    if(currentPlay != position){
                        Playing[currentPlay] = false;
                        if(currentSB != null){
                            currentSB.setVisibility(View.GONE);

                        }
                        if(MThread != null){
                            MThread.interrupt();
                            MThread = null;
                        }
                    }
                }
                selectMp3 = mp3List.get(position);
                try {
                    mPlayer = new MediaPlayer();
                    mPlayer.setDataSource(mp3Path + selectMp3);
                    mPlayer.prepareAsync();
                    mPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        SeekBar seekBar = view.findViewById(R.id.pg1);
                        TextView mTime = view.findViewById(R.id.musicTime);
                        seekBar.setVisibility(View.VISIBLE);
                        seekBar.setMax(mp.getDuration());
                        currentSB = seekBar;

                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if(fromUser){
                                    mPlayer.seekTo(progress);
                                }
                            }
                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }
                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        MThread = new Thread(() -> {
                            SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
                            while (mPlayer !=null && mPlayer.isPlaying()){
                                try{
                                    Thread.sleep(1000);
                                    runOnUiThread(()->{
                                        int currentP = mPlayer.getCurrentPosition();
                                        currentSB.setProgress(currentP);
                                        mTime.setText(timeFormat.format(mPlayer.getCurrentPosition()));
                                    });
                                }catch(Exception e){}
                            }
                        });
                        MThread.start();
                    });
                    currentPlay = position;
                    adapter.notifyDataSetChanged();
                } catch (IOException e) {
                }
            }
        });
        selectMp3 = mp3List.get(0);

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        if (MThread != null) {
            MThread.interrupt();
            MThread = null;
        }
    }
}

