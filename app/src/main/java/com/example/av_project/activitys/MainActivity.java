package com.example.av_project.activitys;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.av_project.R;
import com.example.av_project.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
//    Button btn_encoder;
//    LameMp3 lameMp3;
//    int sampleRate = 44100;
//    int channels = AudioFormat.CHANNEL_IN_MONO;
//    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
//    int bitRate = 128;
    private String className = MainActivity.class.getSimpleName();
    ListView listView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();
        LogUtils.i(TAG, Build.CPU_ABI);

        /*lameMp3 = new LameMp3();
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
        });*/
        listView = findViewById(R.id.list_for_items);
        final List<String> res = new ArrayList<>();
        res.add("AudioTrack");
        res.add("OpenSLES");
        res.add("AudioRecorder");
        res.add("DirectPlay");
        res.add("Camera");
        res.add("FFMPEG");
        res.add("PULLRTMP");
        res.add("PLAYVIDEO");
        listView.setAdapter(new MyAdapter(this,res,R.layout.activity_main_list_item_layout));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (res.get(position)){
                    case "AudioTrack":
                        startActivity(new Intent(MainActivity.this,AudioTrackActivity.class));
                        break;
                    case "OpenSLES":
                        startActivity(new Intent(MainActivity.this,OpenSLESActivity.class));
                        break;
                    case "AudioRecorder":
                        startActivity(new Intent(MainActivity.this,AudioRecorderActivity.class));
                        break;
                    case "DirectPlay":
                        startActivity(new Intent(MainActivity.this,DirectPlayActivity.class));
                        break;
                    case "Camera":
                        startActivity(new Intent(MainActivity.this,CameraActivity.class));
                        break;
                    case "FFMPEG":
                        startActivity(new Intent(MainActivity.this,FFMpegActivity.class));
                        break;
                    case "PULLRTMP":
                        startActivity(new Intent(MainActivity.this,PullFFMPEGActivity.class));
                        break;
                    case "PLAYVIDEO":
                        startActivity(new Intent(MainActivity.this,DecodeYUVActivity.class));
                        break;
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initPermission() {
       //找到缺失的权限
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA};
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
    static class ViewHolder{
        private TextView textView;
    }

    private class MyAdapter extends BaseAdapter{
        private Context context;
        private List<String> res;
        private int layoutId;

        public MyAdapter(Context context,List<String> res,int layoutId){
            this.context = context;
            this.res = res;
            this.layoutId = layoutId;
        }

        @Override
        public int getCount() {
            return res.size();
        }

        @Override
        public Object getItem(int position) {
            return res.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(layoutId,null);
                viewHolder.textView = convertView.findViewById(R.id.item_name);
                viewHolder.textView.setText(res.get(position));
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
                viewHolder.textView.setText(res.get(position));
            }
            return convertView;
        }
    }
}
