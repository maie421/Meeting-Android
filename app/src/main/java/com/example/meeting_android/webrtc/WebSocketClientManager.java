package com.example.meeting_android.webrtc;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.meeting_android.activity.MeetingActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class WebSocketClientManager {
    private static final String TAG = "웹소켓";
    String randomNumberAsString;
    public Context mContext;
    public Activity mActivity;
    private Socket mSocket;
    private String roomName;
    public PeerConnectionClient peerConnectionClient;
    public WebSocketClientManager(Context mContext, Activity mActivity) {
        Random random = new Random();
        int randomNumber = random.nextInt(100); //
        roomName = "12345";
        randomNumberAsString = Integer.toString(randomNumber);
        peerConnectionClient = new PeerConnectionClient(mContext, mActivity);
        connect();
    }

    private void connect(){
        try {
            mSocket = IO.socket("https://16dc-27-35-20-189.ngrok-free.app");
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on("welcome", onWelcome);
            mSocket.on("offer", onOffer);
            mSocket.on("answer", onAnswer);
            mSocket.connect();

            mSocket.emit("join_room",roomName);
        } catch (Exception e) {
            Log.d(TAG,"연결 실패" + e.getMessage());
            e.printStackTrace();
        }
    }
    private Emitter.Listener onConnect = args -> Log.d(TAG,"연결 성공");

    private Emitter.Listener onWelcome = args -> {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        peerConnectionClient.peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnectionClient.peerConnection.setLocalDescription(new SimpleSdpObserver() {
                    @Override
                    public void onSetFailure(String error) {
                        // 실패 시 호출됩니다.
                        Log.e(TAG, "setLocalDescription failed: " + error);
                    }
                }, sessionDescription);
                Log.d(TAG,sessionDescription.description);
                mSocket.emit("offer", sessionDescription.description, roomName);
            }
        }, sdpMediaConstraints);
    };
    private Emitter.Listener onOffer = args -> {
        String offer = (String) args[0];
        Log.d(TAG, "Received onOffer message: " + offer);

        // SDP 생성
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.OFFER, offer);

        // 로컬 PeerConnection에 Offer를 설정
        peerConnectionClient.peerConnection.setRemoteDescription(new SimpleSdpObserver() {
            @Override
            public void onSetFailure(String error) {
                Log.e(TAG, "setRemoteDescription failed: " + error);
            }
        }, sdp);

        // Answer 생성
        peerConnectionClient.peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "Created Answer: " + sessionDescription.description);

                // Answer SDP를 로컬에 설정
                peerConnectionClient.peerConnection.setLocalDescription(new SimpleSdpObserver() {
                    @Override
                    public void onSetFailure(String error) {
                        Log.e(TAG, "setRemoteDescription1 failed: " + error);
                    }
                }, sessionDescription);
                 mSocket.emit("answer", sessionDescription.description, roomName);
            }

        }, new MediaConstraints());
    };

    private Emitter.Listener onAnswer = args -> {
        String answer = (String) args[0];
        Log.d(TAG, "Received onAnswer message: " + answer);;

        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.ANSWER, answer);
        peerConnectionClient.peerConnection.setRemoteDescription(new SimpleSdpObserver() {
            @Override
            public void onSetFailure(String error) {
                Log.e(TAG, "setRemoteDescription failed: " + error);
            }
        }, sdp);
    };

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