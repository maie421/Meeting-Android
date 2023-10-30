package com.example.meeting_android.webrtc;
import static com.example.meeting_android.activity.chatting.ChattingMainActivity.messageAdapter;
import static com.example.meeting_android.activity.chatting.MemberData.getRandomColor;
import static com.example.meeting_android.activity.chatting.Message.GUIDE;
import static com.example.meeting_android.activity.chatting.Message.MESSAGE;
import static com.example.meeting_android.activity.chatting.MessageAdapter.messages;
import static com.example.meeting_android.common.Common.getNowTime;
import static com.example.meeting_android.webrtc.PeerConnectionClient.peerDataChannelnMap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.meeting_android.activity.chatting.MemberData;
import com.example.meeting_android.activity.chatting.Message;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
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
    public static String fromName;
    private static Socket mSocket;
    public PeerConnectionClient peerConnectionClient;
    public List<String> offerList;
    public WebSocketClientManager(Context Context, Activity activity, String roomName, String name) {
        peerConnectionClient = new PeerConnectionClient(Context, activity, name);
        this.offerList = new ArrayList<>();
        this.mActivity = activity;
        this.mContext = Context;
        this.roomName = roomName;
        this.name = name;
        this.fromName = name;
        connect();
    }

    private void connect(){
        Log.d(TAG,"소켓 연결");
        try {
            mSocket = IO.socket("https://5165-27-35-20-189.ngrok-free.app");
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
        String name = (String) args[1];
        fromName = name;
        peerConnectionClient.createPeerConnection(name);
        offerList.add(name);
        createOfferAndSend(name);

        MemberData memberData = new MemberData(fromName, getRandomColor());
        Message message = new Message(" 님이 방에 참가했습니다.", memberData, true, GUIDE, getNowTime());
        if (messageAdapter == null){
            messages.add(message);
        }else {
            messageAdapter.add(message);
        }
    };

    private void createOfferAndSend(String _name) {
        peerConnectionClient.peerConnectionMap.get(_name).createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                mSocket.emit("offer", message, roomName, _name, name);
                peerConnectionClient.peerConnectionMap.get(_name).setLocalDescription(new SimpleSdpObserver() {
                    @Override
                    public void onSetFailure(String error) {
                        Log.e(TAG, "setLocalDescription failed: " + error + "( "+ _name +" )");
                    }
                }, sessionDescription);
            }
        }, peerConnectionClient.sdpMediaConstraints);
    }
    private Emitter.Listener onOffer = args -> {
        String _sdp;
        String name;
        String socketId;

        try {
            JSONObject offerData = (JSONObject) args[0];
            _sdp = offerData.getString("sdp");

            name = (String) args[1];
            socketId = (String) args[2];
            fromName = (String) args[3];
            Log.d(TAG2, "onOffer "+ name);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // SDP 생성
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.OFFER, _sdp);

        if (!offerList.contains(socketId)) {
            offerList.add(socketId);
            peerConnectionClient.createPeerConnection(socketId);
            Log.i(TAG2, "createPeerConnection " + socketId);
        }

        // 로컬 PeerConnection에 Offer를 설정
        peerConnectionClient.peerConnectionMap.get(socketId).setRemoteDescription(new SimpleSdpObserver() {
            @Override
            public void onSetFailure(String error) {
                Log.e(TAG, "setRemoteDescription failed: " + error + "( "+ socketId +" )");
            }
        }, sdp);
        Log.i(TAG2, "setRemoteDescription " + socketId);
        // Answer 생성
        peerConnectionClient.peerConnectionMap.get(socketId).createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                // Answer SDP를 로컬에 설정
                peerConnectionClient.peerConnectionMap.get(socketId).setLocalDescription(new SimpleSdpObserver() {
                    @Override
                    public void onSetFailure(String error) {
                        Log.e(TAG, "setRemoteDescription1 failed: " + error + "( "+ socketId +" )");
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
                mSocket.emit("answer", message, roomName, name, socketId);
            }

        }, peerConnectionClient.sdpMediaConstraints);
    };

    private Emitter.Listener onAnswer = args -> {
        String _sdp;
        String name;

        try {
            JSONObject offerData = (JSONObject) args[0];
            name = (String) args[1];
            fromName = name;
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
        Log.d("미디어","나간 회원 name :"+ msg);

        MemberData memberData = new MemberData(msg, getRandomColor());
        Message message = new Message(" 님이 방에서 나갔습니다.", memberData, true, GUIDE, getNowTime());
        if (messageAdapter == null){
            messages.add(message);
        }else {
            messageAdapter.add(message);
        }

        if (peerConnectionClient.gridCount >= 2) {
            int i = peerConnectionClient.surfaceRendererAdapter.deleteMeetingVideo(msg);
            peerConnectionClient.peerConnectionMap.get(name).close();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (i != 404) {
                        Log.d("미디어","나간 회원 if 문 들어옴:" + i);
                        peerConnectionClient.surfaceRendererAdapter.notifyItemRemoved(i);
                        GridLayoutManager layoutManager = (GridLayoutManager) peerConnectionClient.userRecyclerView.getLayoutManager();
                        layoutManager.setSpanCount(--peerConnectionClient.gridCount);
                    }
                }
            });
        }
    };
    private Emitter.Listener onIce = args -> {
        JSONObject msg = (JSONObject) args[0];
        offerList.forEach(key -> {
            try {
                IceCandidate iceCandidate = new IceCandidate(msg.getString("sdpMid"), msg.getInt("sdpMLineIndex"), msg.getString("candidate"));
                peerConnectionClient.peerConnectionMap.get(key).addIceCandidate(iceCandidate);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    };

    public static void sendIce(IceCandidate iceCandidate) {
        mSocket.emit("ice", toJsonCandidate(iceCandidate), roomName);
    }
    public static void sendLeave() {
        Log.d(TAG, "sendLeave :" + name);
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