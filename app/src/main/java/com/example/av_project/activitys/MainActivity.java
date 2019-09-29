package com.example.av_project.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.av_project.Entity.LameMp3;
import com.example.av_project.R;
import com.example.av_project.Utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btn_encoder;
    LameMp3 lameMp3;
    int sampleRate = 44100;
    int channels = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    int bitRate = 128;
    private String className = MainActivity.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();
        lameMp3 = new LameMp3();
        System.out.println(lameMp3.getVersion());
        File file = new File(Environment.getExternalStorageDirectory()+"/1.pcm");
        if(!file.exists()){
            try {
                throw new FileNotFoundException("1.pcm文件未找到");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        lameMp3.init(Environment.getExternalStorageDirectory()+"/1.pcm",channels,bitRate,sampleRate,
                Environment.getExternalStorageDirectory()+"/new.mp3");
        btn_encoder = findViewById(R.id.encode);
        btn_encoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        lameMp3.encode();
                        LogUtils.i(className,"编码完成");
                        lameMp3.destroy();
                    }
                }).start();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initPermission() {
       //找到缺失的权限
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        List<String> lackPermissions = new ArrayList<>();
        for(String item:permissions){
            if(checkSelfPermission(item) != PackageManager.PERMISSION_GRANTED){
                lackPermissions.add(item);
            }
        }
        String[] newPermissions = new String[lackPermissions.size()];
        for(int i=0;i<lackPermissions.size();i++){
           newPermissions[i] = lackPermissions.get(i);
        }
        if(newPermissions.length >0){
            ActivityCompat.requestPermissions(this,newPermissions,0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 0:
                for(int item:grantResults){
                    if(item != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, permissions[item]+"没有权限!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
