package com.example.meeting_android.webrtc;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class WebSocketClientManager {
    private static final String TAG = "웹소켓";
    private static String TAG2 = "디버그2";
    public Context mContext;
    public Activity mActivity;
    public static String roomName;
    public static String name;
    private static Socket mSocket;
    public PeerConnectionClient peerConnectionClient;
    public List<String> offerList = new ArrayList<>();
    public Map<String,Boolean> isCandidate = new HashMap<>();
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
    private Emitter.Listener onConnect = args -> {
        Log.d(TAG,"연결 성공");
    };
    // 연결 에러 이벤트 핸들러
    private Emitter.Listener onConnectError = args -> {
        Exception e = (Exception) args[0];
        Log.e(TAG, "연결 에러: " + e.getMessage());
    };

    private Emitter.Listener onWelcome = args -> {
        JSONObject participants;
        try {
            JSONObject room = (JSONObject) args[0];
            participants = room.getJSONObject("participants");

            Iterator<String> keys = participants.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Log.i(TAG2, "welcome participants " + key);
                if (!name.equals(key) && peerConnectionClient.peerConnectionMap.get(key) == null){
                    Log.i(TAG2, "welcome participants null " + key);
                    peerConnectionClient.createPeerConnection(key);
                    offerList.add(key);
                    createOfferAndSend(key);
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    };

    private boolean isRenderAdapterBoolean(int count) {
        return peerConnectionClient.surfaceRendererAdapter.getItemCount() >= count;
    }

    private void createOfferAndSend(String name) {
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

                Log.i(TAG2, "send offer " + name);
                mSocket.emit("offer", message, roomName, name);
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
        String name;

        try {
            JSONObject offerData = (JSONObject) args[0];
            _sdp = offerData.getString("sdp");

            name = (String) args[1];
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // SDP 생성
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.OFFER, _sdp);
//        if (isCreateConnection() && isRenderAdapterBoolean(3)) {
//            name = (String) args[1];
//            peerConnectionClient.createPeerConnection(name);
//        }
        offerList.add(name);
        peerConnectionClient.createPeerConnection(name);
        Log.i(TAG2, "createPeerConnection " + name);

        // 로컬 PeerConnection에 Offer를 설정
        peerConnectionClient.peerConnectionMap.get(name).setRemoteDescription(new SimpleSdpObserver() {
            @Override
            public void onSetFailure(String error) {
                Log.e(TAG, "setRemoteDescription failed: " + error + "( "+ name +" )");
            }
        }, sdp);
        Log.i(TAG2, "setRemoteDescription " + name);
        // Answer 생성
        peerConnectionClient.peerConnectionMap.get(name).createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                // Answer SDP를 로컬에 설정
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
                Log.i(TAG2, "sendAnswer " + name);
                mSocket.emit("answer", message, roomName, name);
            }

        }, peerConnectionClient.sdpMediaConstraints);
    };

    private Emitter.Listener onAnswer = args -> {
        String _sdp;
        String name;
        try {
            JSONObject offerData = (JSONObject) args[0];
            name = (String) args[1];
            _sdp = offerData.getString("sdp");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.ANSWER, _sdp);
        Log.i(TAG2, "onAnswer " + name);
        peerConnectionClient.peerConnectionMap.get(name).setRemoteDescription(new SimpleSdpObserver() {
            @Override
            public void onSetFailure(String error) {
                Log.e(TAG, "setRemoteDescription failed: " + error);
            }
        }, sdp);
    };

    private Emitter.Listener onLeaveRoom = args -> {
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
    };
    private Emitter.Listener onIce = args -> {
        JSONObject msg = (JSONObject) args[0];

        offerList.forEach(key -> {
            try {
                Log.d(TAG2, "여긴???");
                IceCandidate iceCandidate = new IceCandidate(msg.getString("sdpMid"), msg.getInt("sdpMLineIndex"), msg.getString("candidate"));
                peerConnectionClient.peerConnectionMap.get(key).addIceCandidate(iceCandidate);
                isCandidate.put(key, true);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

    };
    public static void sendIce(IceCandidate iceCandidate) {
        mSocket.emit("ice", toJsonCandidate(iceCandidate), roomName);
    }
    public static void sendLeave() {
        Log.d(TAG, "sendLeave");
        mSocket.emit("leave_room", roomName, name);
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