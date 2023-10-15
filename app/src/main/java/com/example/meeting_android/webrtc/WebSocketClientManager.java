package com.example.meeting_android.webrtc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;;
import java.lang.reflect.Type;
import java.util.Random;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class WebSocketClientManager {
    private static final String TAG = "웹소켓";
    String randomNumberAsString;
    public Context mContext;
    public Activity mActivity;
    private static Socket mSocket;
    private static String roomName;
    public PeerConnectionClient peerConnectionClient;
    public WebSocketClientManager(Context mContext, Activity mActivity) {
        Random random = new Random();
        int randomNumber = random.nextInt(100); //
        roomName = "123457";
        randomNumberAsString = Integer.toString(randomNumber);
        peerConnectionClient = new PeerConnectionClient(mContext, mActivity);
        connect();
    }

    private void connect(){
        Log.d(TAG,"소켓 연결");
        try {
            mSocket = IO.socket("https://1918-221-148-25-236.ngrok-free.app");
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on("welcome", onWelcome);
            mSocket.on("offer", onOffer);
            mSocket.on("answer", onAnswer);
            mSocket.on("ice", onIce);
            mSocket.connect();

            mSocket.emit("join_room", roomName);
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

        peerConnectionClient.peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                JSONObject message = new JSONObject();
                try {
                    message.put("type","offer");
                    message.put("sdp", sessionDescription.description);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Log.i(TAG, sessionDescription.description);

                mSocket.emit("offer", message, roomName);
                peerConnectionClient.peerConnection.setLocalDescription(new SimpleSdpObserver() {
                    @Override
                    public void onSetFailure(String error) {
                        // 실패 시 호출됩니다.
                        Log.e(TAG, "setLocalDescription failed: " + error);
                    }
                }, sessionDescription);
            }
        }, peerConnectionClient.sdpMediaConstraints);
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

        }, peerConnectionClient.sdpMediaConstraints);
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
        if (args != null || args[0] != null) {
            JSONObject msg = (JSONObject) args[0];
            try {
                IceCandidate iecCandidate = new IceCandidate(  msg.getString("sdpMid"),msg.getInt("sdpMLineIndex"), msg.getString("candidate"));
                peerConnectionClient.peerConnection.addIceCandidate(iecCandidate);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    };

    public static void sendIce(IceCandidate iceCandidate) {
        Log.d(TAG, "ice");
        mSocket.emit("ice", toJsonCandidate(iceCandidate), roomName);
    }
    private static JSONObject toJsonCandidate(final IceCandidate candidate) {
        JSONObject json = new JSONObject();
        try {
            json.put("sdpMLineIndex", candidate.sdpMLineIndex);
            json.put("sdpMid", candidate.sdpMid);
            json.put("candidate", candidate.sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
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