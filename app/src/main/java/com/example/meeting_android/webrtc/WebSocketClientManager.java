package com.example.meeting_android.webrtc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.meeting_android.activity.MeetingActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoTrack;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class WebSocketClientManager extends WebSocketClient {
    private static final String TAG = "웹소켓";
    String randomNumberAsString;
    public Context mContext;
    public Activity mActivity;
    public PeerConnectionClient peerConnectionClient;
    public WebSocketClientManager(Context mContext, Activity mActivity, URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri);
        Random random = new Random();
        int randomNumber = random.nextInt(100); //
        randomNumberAsString = Integer.toString(randomNumber);

        peerConnectionClient = new PeerConnectionClient(mContext, mActivity);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "WebSocket connection opened");

        JsonObject message = new JsonObject();
        message.addProperty("id", "joinRoom");
        message.addProperty("name", randomNumberAsString);
        message.addProperty("roomName", "1234");
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
        Log.d(TAG, "iceCandidate");
    }

    //상대방 sdp 가져오기
    private void onReceiveVideoAnswer(JsonObject jsonObject) {
        Log.d(TAG, "onReceiveVideoAnswer");
        try {
            String sdpAnswer = jsonObject.get("sdpAnswer").getAsString();

            // SDP 생성
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.ANSWER, sdpAnswer);

            // 로컬 PeerConnection에 SDP 설정
            SimpleSdpObserver sdpObserver = new SimpleSdpObserver();
            peerConnectionClient.peerConnection.setRemoteDescription(new SimpleSdpObserver() {
                @Override
                public void onSetFailure(String error) {
                    // setRemoteDescription 실패 시 호출됩니다.
                    Log.e(TAG, "setRemoteDescription failed: " + error);
                    // 실패 처리를 수행합니다.
                }
            }, sdp);

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
        }
    }

    private void onParticipantLeft(JsonObject jsonObject) {
        Log.d(TAG, "onParticipantLeft");

    }
    //다른 참가자가 새로 연결 되었을 때 호출
    private void onNewParticipantArrived(JsonObject jsonObject) {
        Log.d(TAG, "onNewParticipantArrived");

//        JsonObject json = new JsonObject();
//        json.addProperty("id", "onIceCandidate");
//        json.addProperty("candidate", "candidate");
//        json.addProperty("name", randomNumberAsString);
//
//        sendMessage(json.toString());
    }

    //이미 존재하는 참가자 목록
    private void onExistingParticipants(JsonObject jsonObject) {
        Log.d(TAG, "onExistingParticipants");
        JsonObject json = new JsonObject();
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        //본인 sdp 전송
        peerConnectionClient.peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "onCreateSuccess");
                peerConnectionClient.peerConnection.setLocalDescription(new SimpleSdpObserver() {
                    @Override
                    public void onSetFailure(String error) {
                        // 실패 시 호출됩니다.
                        Log.e(TAG, "setLocalDescription failed: " + error);
                    }
                }, sessionDescription);

                json.addProperty("sdpOffer",sessionDescription.description);
                json.addProperty("id", "receiveVideoFrom");
                json.addProperty("sender", randomNumberAsString);

                sendMessage(json.toString());
            }
        }, sdpMediaConstraints);
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

    class SimpleSdpObserver implements SdpObserver {

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(String s) {
        }

        @Override
        public void onSetFailure(String s) {
        }

    }
}