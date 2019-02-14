package com.example.admin.stt_phone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.ImageView;
import android.widget.LinearLayout;

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
    public AudioRunningThread mPlayThread = null;

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
        setTitle(phone);

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
/*        if (mPlayThread != null) {
            mPlayThread.stopThread();
        }*/
        if (phoneSocket != null) {
            phoneSocket.exit();
        }
    }

}
