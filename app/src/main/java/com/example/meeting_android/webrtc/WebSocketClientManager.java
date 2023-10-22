package com.example.meeting_android.webrtc;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class WebSocketClientManager {
    private static final String TAG = "웹소켓";
    private static String TAG2 = "디버그2";
    public Context mContext;
    public Activity mActivity;
    public static String roomName;
    public String name;
    private static Socket mSocket;
    public PeerConnectionClient peerConnectionClient;
    public static boolean isFirst = true;
    public List<String> offerList = new ArrayList<>();
    public WebSocketClientManager(Context Context, Activity activity, String roomName, String name) {
        peerConnectionClient = new PeerConnectionClient(Context, activity, name);
        this.mActivity = activity;
        this.mContext = Context;
        this.roomName = roomName;
        this.name = name;
        connect();
    }

    private void connect(){
        Log.d(TAG,"소켓 연결");
        try {
            mSocket = IO.socket("https://9b8c-27-35-20-189.ngrok-free.app");
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on("welcome", onWelcome);
            mSocket.on("offer", onOffer);
            mSocket.on("answer", onAnswer);
            mSocket.on("ice", onIce);
            mSocket.on("leave_room", onLeaveRoom);
            mSocket.connect();

            mSocket.emit("join_room", roomName, name);
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

        if (isRenderAdapterBoolean(2)) {
            name = (String) args[1];
            peerConnectionClient.createPeerConnection(name);
            Log.i(TAG2, "onWelcome 들어옴 : " + name);
        }
        Log.i(TAG2, "onWelcome Welcome: " + name);
        offerList.add(name);
        createOfferAndSend();
    };

    private boolean isRenderAdapterBoolean(int count) {
        return peerConnectionClient.surfaceRendererAdapter.getItemCount() >= count;
    }

    private void createOfferAndSend() {
        peerConnectionClient.peerConnectionMap.get(name).createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Log.i(TAG, sessionDescription.description);

                mSocket.emit("offer", message, roomName);
                peerConnectionClient.peerConnectionMap.get(name).setLocalDescription(new SimpleSdpObserver() {
                    @Override
                    public void onSetFailure(String error) {
                        Log.e(TAG, "setLocalDescription failed: " + error + "( "+ name +" )");
                    }
                }, sessionDescription);
            }
        }, peerConnectionClient.sdpMediaConstraints);
    }
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
//TODO  해당 조건문 타고 들어가는지 확인 첫번째 화면이 정상적으로 동작하나 두번째 create peer 부터는 이상하게 보인다...?
        if (isCreateConnection() && isRenderAdapterBoolean(3)) {
            name = (String) args[1];
            peerConnectionClient.createPeerConnection(name);
        }
        offerList.add(name);
        Log.i(TAG2, "onOffer " + name);

        // 로컬 PeerConnection에 Offer를 설정
        peerConnectionClient.peerConnectionMap.get(name).setRemoteDescription(new SimpleSdpObserver() {
            @Override
            public void onSetFailure(String error) {
                Log.e(TAG, "setRemoteDescription failed: " + error + "( "+ name +" )");
            }
        }, sdp);
        Log.i(TAG2, "createAnswer " + name);
        // Answer 생성
        peerConnectionClient.peerConnectionMap.get(name).createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                // Answer SDP를 로컬에 설정
                Log.i(TAG, "setLocalDescription " + name);
                peerConnectionClient.peerConnectionMap.get(name).setLocalDescription(new SimpleSdpObserver() {
                    @Override
                    public void onSetFailure(String error) {
                        Log.e(TAG, "setRemoteDescription1 failed: " + error + "( "+ name +" )");
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

    private boolean isCreateConnection() {
        Log.d(TAG, "isCreateConnection : " + name);
        return  peerConnectionClient.peerConnectionMap.get(name) == null;
    }

    private Emitter.Listener onAnswer = args -> {
        String _sdp;
        try {
            JSONObject offerData = (JSONObject) args[0];
            _sdp = offerData.getString("sdp");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.ANSWER, _sdp);
        Log.d(TAG, "onAnswer" + name);
        peerConnectionClient.peerConnectionMap.get(name).setRemoteDescription(new SimpleSdpObserver() {
            @Override
            public void onSetFailure(String error) {
                Log.e(TAG, "setRemoteDescription failed: " + error);
            }
        }, sdp);
    };

    private Emitter.Listener onLeaveRoom = args -> {
        if (args != null || args[0] != null) {
            String msg = (String) args[0];
            Log.d("디버그","나간 회원"+ msg);
            if (peerConnectionClient.gridCount >= 2) {
                peerConnectionClient.surfaceRendererAdapter.deleteMeetingVideo(msg);
//                peerConnectionClient.peerConnectionMap.get(name).close();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GridLayoutManager layoutManager = (GridLayoutManager) peerConnectionClient.userRecyclerView.getLayoutManager();
                        layoutManager.setSpanCount(--peerConnectionClient.gridCount);
                        //peerConnectionClient.surfaceRendererAdapter.getItemCount()
//                        peerConnectionClient.surfaceRendererAdapter.notifyItemRemoved(0);
                    }
                });
            }
        }
    };
    private Emitter.Listener onIce = args -> {
        isFirst = false;
        if (args != null || args[0] != null) {
            JSONObject msg = (JSONObject) args[0];
            try {
                IceCandidate iecCandidate = new IceCandidate(msg.getString("sdpMid"), msg.getInt("sdpMLineIndex"), msg.getString("candidate"));
                for (String name : offerList) {
                    peerConnectionClient.peerConnectionMap.get(name).addIceCandidate(iecCandidate);
                    Log.i(TAG2, "offerList " + name);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };
    public static void sendIce(IceCandidate iceCandidate) {
        isFirst = false;
        mSocket.emit("ice", toJsonCandidate(iceCandidate), roomName);
    }
    public static void sendLeave() {
        Log.d(TAG, "sendLeave");
        mSocket.emit("leave_room", roomName, "TEST");
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