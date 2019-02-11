package com.example.admin.stt_phone;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioRunningThread extends Thread{
    private static String TAG = "AudioRunningThread";
    private int mSampleRate = 8000; // 녹음 음성 음질 8000~48000
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT; //https://developer.android.com/reference/android/media/AudioFormat
    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    private String mFilePath = null;
    private boolean hasFile = false;
    private boolean isPlaying = true;
    private byte[] voiceByteArray = null;

    private AudioTrack mAudioTrack = null;
    private DataInputStream dis = null;
    private FileInputStream fis = null;

    public AudioRunningThread(byte[] _voiceByteArray) {

        voiceByteArray = _voiceByteArray;
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);
    }

    public AudioRunningThread(String path) {
        mFilePath = path;
        hasFile = true;

        Log.d(TAG, "New AudioTrack Thread Created");
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);
    }

    @Override
    public void run() {

        // 녹음된 음성 파일을 재생하는 경우
        if (hasFile) {
            voiceByteArray = new byte[mBufferSize];

            try {
                fis = new FileInputStream(mFilePath);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            dis = new DataInputStream(fis);
        }

        // write 하기 전에 play 를 먼저 수행해 주어야 함
        mAudioTrack.play();

        // voice 재생
        while(isPlaying) {
            if (hasFile) {
                try {
                    int ret = dis.read(voiceByteArray, 0, mBufferSize);
                    if (ret <= 0) {
                        break;
                    }
                    mAudioTrack.write(voiceByteArray, 0, ret);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                // ! voiceByteArray 를 한번에 write가 가능한지 체크해봐야함
                Log.d("TESTTEST : ", String.valueOf(voiceByteArray.length));
                mAudioTrack.write(voiceByteArray, 0, voiceByteArray.length);
                isPlaying = false;
            }
        }

        mAudioTrack.stop();
        mAudioTrack.release();
        mAudioTrack = null;

        try {
            if(dis != null)
                dis.close();
            if(fis != null)
                fis.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopThread() {
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    public void setRunningStatus(boolean _isPlaying) {
        isPlaying = _isPlaying;
    }

    public boolean getRunningStatus() {
        return isPlaying;
    }
}
