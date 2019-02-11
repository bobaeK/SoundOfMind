package com.example.admin.stt_phone;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PhoneSocket {
    private static final String TAG = "PhoneSocket";
    private Socket mSocket = null;
    private String _ipFromServer = null;

    public static final String SOCKET_URL = "http://101.101.162.106:8000";
    public static final String EVENT_ENTERED = "join-user";
    public static final String EVENT_SYSTEM = "system";
    public static final String EVENT_MESSAGE = "chat-message";
    public static final String SEND_DATA_USERNAME = "username";
    public static final String SEND_DATA_MESSAGE = "message";
    public static final String MESSAGE_TYPE_SYSTEM = "system";
    public static final String MESSAGE_TYPE_SELF = "self";
    public static final String MESSAGE_TYPE_RECEIVE = "received";
    public static final String EXTRA_USERNAME = "username";

/*    private Queue<String> messageList = new LinkedList<String>();
    private Queue<String> emotionList = new LinkedList<String>();
    //private Queue<byte[]> voiceList = new LinkedList<byte[]>();*/


    //ui
    private Context context = null;
    private ViewGroup container = null;
    private String name =null;
    @SuppressLint("HandlerLeak")
    private android.os.Handler handler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TextView tv = new TextView(context);

            String[] content = (String[])msg.obj;
            tv.setText(content[0]);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_message_received, container, false);

            TextView body_text = (TextView) view.findViewById(R.id.text_message_body);
            body_text.setText(content[0]);
            TextView name_text = (TextView) view.findViewById(R.id.text_message_name);
            name_text.setText(name);
            TextView time_text = (TextView) view.findViewById(R.id.text_message_time);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdFormatter = new SimpleDateFormat("HH:mm");
            time_text.setText(mdFormatter.format(calendar.getTime()));

            ImageView emotionview = (ImageView) view.findViewById(R.id.image_message_emotion);

            switch (content[1]) {
                case "joy":
                    emotionview.setImageResource(R.drawable.joy);
                    break;
                case "anger":
                    emotionview.setImageResource(R.drawable.anger);
                    break;
                case "fear":
                    emotionview.setImageResource(R.drawable.fear);
                    break;
                case "love":
                    emotionview.setImageResource(R.drawable.love);
                    break;
                case "neutral":
                    emotionview.setImageResource(R.drawable.neutral);
                    break;
                case "sadness":
                    emotionview.setImageResource(R.drawable.sadness);
                    break;
                case "surprise":
                    emotionview.setImageResource(R.drawable.surprise);
                    break;
                case "not yet":
                    emotionview.setImageResource(R.drawable.neutral);
                    break;
            }

            container.addView(view);
        }
    };

    public PhoneSocket(){

    }
    public PhoneSocket(Context context, ViewGroup container, String name){
        this.context = context;
        this.container = container;
        this.name = name;
    }

    public void start() {
        try {
            Log.d(TAG,"trying to create socket");
            mSocket = IO.socket(SOCKET_URL);

            mSocket.on("init", onConnect);
            mSocket.on("json", onMessageReceived);
            //mSocket.on(Constants.EVENT_MESSAGE, onMessageReceived);

            mSocket.connect();
            mSocket.emit("init");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.d(TAG,"Failed to connect to server");
        }
    }

/*    public String getMessages() {
        if (messageList.size() == 0) {
            return "None";
        }
        else {
            return messageList.poll();
        }
    }*/

/*
    public String getEmotion() {
        if (emotionList.size() == 0) {
            return "None";
        }
        else {
            return emotionList.poll();
        }
    }
*/

    public byte[] getVoice() {
        return null;
    }

    public void sendVoice(byte[] byteArray) {
        mSocket.emit("receive", byteArray);
        Log.d(TAG,"Send Voice : " + byteArray.length);
    }

    public void exit() {
        mSocket.close();
    }

    /**
     * Socket Server 연결 Listener
     */
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            if (args.length > 0) {
                JSONObject rcvData = (JSONObject) args[0];

                _ipFromServer = rcvData.optString("success");
                Log.d("ipFromServer", _ipFromServer);
            }
        }
    };

    /**
     * Message 전달 Listener
     */
    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject rcvData = (JSONObject) args[0];

            String ip = rcvData.optString("ip");

            if (ip.equals(_ipFromServer)) {
                Log.d("IP TEST", " ip address is same");

                //String voice = rcvData.optString("voice"); // 현재 voice는 받아올 수 없음
                byte[] voice = null;
                try {
                    voice = (byte[])rcvData.get("voice");
                    Log.d("SocketListener_MessageReceived", "voice: " + voice.length);  // 현재 voice는 받아올 수 없음
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String messageText = rcvData.optString("text");
                String emotion = rcvData.optString("emotion");


                Log.d("SocketListener_MessageReceived", "text: " + messageText.length());
                Log.d("SocketListener_MessageReceived", "emotion: " + emotion);

                //ui 변경
                if(messageText != null && !messageText.isEmpty()) {
                    Message message = Message.obtain(handler);
                    String[] content = new String[]{messageText, emotion};
                    message.obj = content;
                    handler.sendMessage(message);
                }
                // 받은 voice 데이터 재생
                if(voice != null) {

                    AudioRunningThread art = new AudioRunningThread(voice);
                    art.start();
                }
            }
        }
    };

    private void viewText(String emotion, String text){

    }

}
