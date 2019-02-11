package com.example.admin.stt_phone;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import io.socket.client.Socket;

public class AudioRecordingThread extends Thread {
    private static String TAG = "AudioRecordingThread";
    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = 8000; // 녹음 음성 음질 8000~48000
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT; //https://developer.android.com/reference/android/media/AudioFormat
    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);
    private String mFilePath = null;
    private boolean isRecording = true;

    private AudioRecord mAudioRecord = null;
    private PhoneSocket phoneSocket = null;

    public AudioRecordingThread(String path, PhoneSocket _phoneSocket) {
        mFilePath = path;
        this.phoneSocket = _phoneSocket;

        mAudioRecord =  new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        mAudioRecord.startRecording();
    }

    @Override
    public void run() {
        byte[] readData = new byte[2000000];
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(mFilePath);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        int totalSize = 0;

        while(isRecording) {
            int ret = mAudioRecord.read(readData, totalSize, mBufferSize);  //  AudioRecord의 read 함수를 통해 pcm data 를 읽어옴
            if (ret < 0) {
                break;
            }

            totalSize += ret;
        }
        Log.d(TAG, "Audio size : " + String.valueOf(totalSize) + " bytes");

        /**
         * Send Audio Byte Array
         */

        // MARK: - TEST 하기 위해서 external cache 에 저장
        try {
            fos.write(readData, 0,totalSize);    //  읽어온 readData 를 파일에 write 함
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] sendArray = Arrays.copyOfRange(readData, 0, totalSize);
        phoneSocket.sendVoice(sendArray);

        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRecordingStatus(boolean _isRecording) {
        isRecording = _isRecording;
    }

    public boolean getRecordingStatus() {
        return isRecording;
    }

    public void stopThread() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    /*
    PCM to WAV 바꾸기 위해 필요한
     */
    /*
    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;/*
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, 44100); // sample rate
            writeInt(output, RECORDER_SAMPLERATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }

            output.write(fullyReadFileToBytes(rawFile));
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis= new FileInputStream(f);
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }  catch (IOException e){
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }
*/
}
