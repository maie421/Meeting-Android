package com.example.meeting_android.webrtc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaConstraints;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;;
import java.util.Random;
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
        Log.d(TAG,"소켓 연결");
        try {
            mSocket = IO.socket("https://577e-221-148-25-236.ngrok-free.app");
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on("welcome", onWelcome);
            mSocket.on("offer", onOffer);
            mSocket.on("answer", onAnswer);
            mSocket.on("ice", onIce);
            mSocket.connect();

            mSocket.emit("join_room",roomName);
        } catch (Exception e) {
            Log.d(TAG,"연결 실패" + e.getMessage());
            e.printStackTrace();
        }
    }
    private Emitter.Listener onConnect = args -> Log.d(TAG,"연결 성공");
    // 연결 에러 이벤트 핸들러
    private Emitter.Listener onConnectError = args -> {
        Exception e = (Exception) args[0];
        Log.e(TAG, "연결 에러: " + e.getMessage());
    };

    private Emitter.Listener onWelcome = args -> {
        Log.i(TAG, "Welcome");
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveVideo", "false"));

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

                JSONObject message = new JSONObject();
                try {
                    message.put("type","offer");
                    message.put("sdp", sessionDescription.description);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                mSocket.emit("offer", message, roomName);
            }
        }, sdpMediaConstraints);
    };
    private Emitter.Listener onOffer = args -> {
        Log.d(TAG, "onOffer");
        String _sdp;
        try {
            JSONObject offerData = (JSONObject) args[0];
            _sdp = offerData.getString("sdp");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // SDP 생성
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.OFFER, _sdp);

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

                // Answer SDP를 로컬에 설정
                peerConnectionClient.peerConnection.setLocalDescription(new SimpleSdpObserver() {
                    @Override
                    public void onSetFailure(String error) {
                        Log.e(TAG, "setRemoteDescription1 failed: " + error);
                    }
                }, sessionDescription);

                JSONObject message = new JSONObject();
                try {
                    message.put("type","answer");
                    message.put("sdp", sessionDescription.description);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                mSocket.emit("answer", message, roomName);
            }

        }, new MediaConstraints());
    };

    private Emitter.Listener onAnswer = args -> {
        Log.d(TAG, "onAnswer");
        String _sdp;
        try {
            JSONObject offerData = (JSONObject) args[0];
            _sdp = offerData.getString("sdp");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.ANSWER, _sdp);
        peerConnectionClient.peerConnection.setRemoteDescription(new SimpleSdpObserver() {
            @Override
            public void onSetFailure(String error) {
                Log.e(TAG, "setRemoteDescription failed: " + error);
            }
        }, sdp);
    };

    private Emitter.Listener onIce = args -> {
//      IceCandidate iceCandidate = (IceCandidate) args[0];
        Log.d(TAG, "Received onIce message"+ args);
//        peerConnectionClient.peerConnection.addIceCandidate(iceCandidate);
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