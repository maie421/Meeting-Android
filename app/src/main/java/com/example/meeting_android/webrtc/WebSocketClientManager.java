package com.example.meeting_android.webrtc;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.webrtc.VideoTrack;

import java.net.URI;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class WebSocketClientManager extends WebSocketClient{
    private static final String TAG = "웹소켓";

    public WebSocketClientManager(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "WebSocket connection opened");

        Log.d("웹소켓", "시작");
        JsonObject message = new JsonObject();
        message.addProperty("id", "joinRoom");
        message.addProperty("name", "안녕");
        message.addProperty("roomName", "123");
        sendMessage(message.toString());
    }

    //서버로 부터 텍스트 메시지를 수신 했을 때 처리
    @Override
    public void onMessage(String message) {
        Log.e(TAG, "######## onMessage ########\n" + message);
        JsonObject jsonObject = new Gson().fromJson(message, JsonObject.class);
        String id = jsonObject.get("id").getAsString();

        switch (id) {
            case "existingParticipants":
                onExistingParticipants(jsonObject);
                break;
            case "newParticipantArrived":
                onNewParticipantArrived(jsonObject);
                break;
            case "participantLeft":
                onParticipantLeft(jsonObject);
                break;
            case "receiveVideoAnswer":
                onReceiveVideoAnswer(jsonObject);
                break;
            case "iceCandidate":
                ceCandidate(jsonObject);
                break;
            default:
                break;
        }
    }

    private void ceCandidate(JsonObject jsonObject) {
        Log.d(TAG, "ceCandidate");
    }

    private void onReceiveVideoAnswer(JsonObject jsonObject) {
        Log.d(TAG, "onReceiveVideoAnswer");
        JsonObject message = new Gson().fromJson(jsonObject, JsonObject.class);
        String sdpAnswer = jsonObject.get("sdpAnswer").getAsString();

    }

    private void onParticipantLeft(JsonObject jsonObject) {
        Log.d(TAG, "onParticipantLeft");
        
    }

    private void onNewParticipantArrived(JsonObject jsonObject) {
        Log.d(TAG, "onNewParticipantArrived");

//        JsonObject json = new JsonObject();
//        json.addProperty("id", "onIceCandidate");
//        json.addProperty("candidate", "candidate");
//        json.addProperty("name", "안녕");
//
//        sendMessage(json.toString());
    }

    private void onExistingParticipants(JsonObject jsonObject) {
        Log.d(TAG, "onExistingParticipants");
    }

    // WebSocket 연결이 닫혔을 때
    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "onClose code=" + code + " reason=" + reason + " remote=" + remote);
    }

    // WebSocket 연결 실패
    @Override
    public void onError(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (this.isOpen()) {
            Log.d(TAG, "message" + message);
            this.send(message);
        }else{
            Log.d(TAG, "에러 message" + message);
        }
    }
}