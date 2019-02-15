package com.example.admin.stt_phone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "***MAINACTIVITY***";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    public AudioRecordingThread mRecordThread = null;

    private static String mFilepath = null;

    //recording flag
    public boolean mStartRecording = true;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    // Socket
    private PhoneSocket phoneSocket = null;

    //ui
    private LinearLayout chat_ll = null;
    private String name = null;

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    private void onRecord(boolean start) {
        if (start) {
            mRecordThread = new AudioRecordingThread(mFilepath, phoneSocket);
            mRecordThread.setRecordingStatus(start);
            mRecordThread.start();
        } else {
            mRecordThread.setRecordingStatus(start);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        //ui



        name = getIntent().getStringExtra("name");
        String phone = getIntent().getStringExtra("phone");
        if(name == null)
            setTitle("C&C 김보배");
        else
            setTitle(name);
        //chat_ll = (LinearLayout)findViewById(R.id.chat_ll);
        final ImageView record_btn = (ImageView)findViewById(R.id.record_btn);
        record_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i(LOG_TAG, "action_down");
                        //bb
                        onRecord(mStartRecording);
                        record_btn.setImageResource(R.drawable.record_ing);
                        mStartRecording = !mStartRecording;
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i(LOG_TAG, "action_up");
                        //bb
                        onRecord(mStartRecording);
                        record_btn.setImageResource(R.drawable.record_start);
                        mStartRecording = !mStartRecording;
                        break;
                }
                return false;
            }
        });
        record_btn.setClickable(true);

        ImageView disconnect_btn = (ImageView)findViewById(R.id.disconnect_btn);
        disconnect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("save")        // 제목 설정
                        .setMessage("통화 내용을 저장하시겠습니까?")        // 메세지 설정
                        .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                        .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                            // 확인 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton){
                                Toast.makeText(MainActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener(){

                            // 취소 버튼 클릭시 설정

                            public void onClick(DialogInterface dialog, int whichButton){
                               finish();
                            }
                        });



                AlertDialog dialog = builder.create();    // 알림창 객체 생성
                dialog.show();


                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                positiveButton.setTextColor(Color.parseColor("#000000"));
                negativeButton.setTextColor(Color.parseColor("#000000"));
            }
        });



        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        ArrayList<Data> sampleData = new ArrayList<Data>();
        mAdapter = new MyAdapter(sampleData, getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);

        // MARK: - TEST 용
        // 녹음한 음성 저장하기 위함
        mFilepath = getExternalCacheDir().getAbsolutePath() +"/record.pcm";

        // Permission
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }



    @Override
    protected void onStart() {
        super.onStart();
        //bb
        // Create Socket
        Log.d(LOG_TAG, "onStart()");
        phoneSocket = new PhoneSocket(this, mAdapter, name);
        phoneSocket.start();


    }

    @Override
    public void onStop() {
        super.onStop();

        if (mRecordThread != null) {
            mRecordThread.stopThread();
        }
        if (phoneSocket != null) {
            phoneSocket.exit();
        }
    }
    public void hang_up(View view){

        //finish();
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getApplicationContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getApplicationContext());
        }
        builder.setTitle("Save")
                .setMessage("통화내용을 저장하시겠습니까?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with save
                        Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }


}
