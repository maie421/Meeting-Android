package com.example.meeting_android.webrtc;
import com.example.meeting_android.R;
import static com.example.meeting_android.activity.chatting.ChattingMainActivity.messageAdapter;
import static com.example.meeting_android.activity.chatting.MemberData.getRandomColor;
import static com.example.meeting_android.activity.chatting.Message.GUIDE;
import static com.example.meeting_android.activity.chatting.Message.MESSAGE;
import static com.example.meeting_android.activity.chatting.MessageAdapter.messages;
import static com.example.meeting_android.activity.meeting.MeetingActivity.customDialog;
import static com.example.meeting_android.activity.meeting.MeetingActivity.hostRecordName;
import static com.example.meeting_android.activity.meeting.Recorder.isPermissionMap;
import static com.example.meeting_android.activity.meeting.Recorder.isRecording;
import static com.example.meeting_android.common.Common.getNowTime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.meeting_android.activity.chatting.MemberData;
import com.example.meeting_android.activity.chatting.Message;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public static Socket mSocket;
    public PeerConnectionClient peerConnectionClient;
    public Boolean isDuplicateRecording = false;
    public List<String> offerList;
    public WebSocketClientManager(Context context, Activity activity, String roomName, String name) {
        peerConnectionClient = new PeerConnectionClient(context, activity, name);
        this.offerList = new ArrayList<>();
        this.mActivity = activity;
        this.mContext = context;
        this.roomName = roomName;
        this.name = name;
        this.fromName = name;
        connect();
    }

    private void connect(){
        Log.d(TAG,"소켓 연결");
        try {
            mSocket = IO.socket("https://12ff-221-148-25-236.ngrok-free.app");
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on("welcome", onWelcome);
            mSocket.on("offer", onOffer);
            mSocket.on("answer", onAnswer);
            mSocket.on("ice", onIce);
            mSocket.on("leave_room", onLeaveRoom);
            mSocket.on("recorder_room", onRecorder);
            mSocket.on("recorder_name", onRecorder);
            mSocket.on("stop_recorder_room", onStopRecorder);
            mSocket.on("stop_screen_room", onStopScreen);
            mSocket.on("change_host", onChangeHost);
            mSocket.on("send_android_message", onSendAndroidMessage);//web 으로 부터 메세지 받아오기
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
                if (isRecording) {
                    mSocket.emit("recorder_name", roomName, _name);
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
        String hostName = (String) args[4];

        mActivity.runOnUiThread(() -> {
            hostRecordName = hostName;
            customDialog.hostView.setText(hostName);
        });

        try {
            JSONObject offerData = (JSONObject) args[0];
            _sdp = offerData.getString("sdp");

            name = (String) args[1];
            socketId = (String) args[2];
            fromName = (String) args[3];
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // SDP 생성
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.OFFER, _sdp);

        if (!offerList.contains(socketId)) {
            offerList.add(socketId);
            Log.i(TAG2, "createPeerConnection " + socketId);
        }
        peerConnectionClient.createPeerConnection(socketId);

        // 로컬 PeerConnection에 Offer를 설정
        peerConnectionClient.peerConnectionMap.get(socketId).setRemoteDescription(new SimpleSdpObserver() {
            @Override
            public void onSetFailure(String error) {
                Log.e(TAG2, "setRemoteDescription failed: " + error + "( "+ socketId +" )");
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
                        Log.e(TAG2, "setLocalDescription failed: " + error + "( "+ socketId +" )");
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
        String hostName = (String) args[1];
        Log.d("미디어","나간 회원 name :"+ msg);

        mActivity.runOnUiThread(() -> {
            hostRecordName = hostName;
            customDialog.hostView.setText(hostName);
        });

        MemberData memberData = new MemberData(msg, getRandomColor());
        Message message = new Message(" 님이 방에서 나갔습니다.", memberData, true, GUIDE, getNowTime());
        if (messageAdapter == null){
            messages.add(message);
        }else {
            messageAdapter.add(message);
        }

        peerConnectionClient.surfaceTextureHelperList.clear();

        if (peerConnectionClient.gridCount >= 2) {
            int i = peerConnectionClient.surfaceRendererAdapter.deleteMeetingVideo(msg);
            peerConnectionClient.peerConnectionMap.get(name).close();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (i != -1) {
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

    private Emitter.Listener onRecorder = args -> {
        Log.d("녹화", "onRecorder");
        String roomName = (String) args[0];
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!isDuplicateRecording) {
                    if (isPermissionMap.containsKey(roomName)) {
                        startRecorder(roomName);
                    } else {
                        showDialog(roomName);
                        Log.d("녹화", "isRecording 활성화");
                    }
                    isDuplicateRecording = true;
                }
            }
        });
    };

    private Emitter.Listener onStopRecorder = args -> {
        Log.d("녹화", "onStopRecorder");
        mActivity.runOnUiThread(new Runnable() {
            TextView recorderView = mActivity.findViewById(R.id.recorderView);
            @Override
            public void run() {
                if (isRecording) {
                    Log.d("녹화", "isRecording 비 활성화");
                    recorderView.setVisibility(View.GONE);
                    isRecording = false;
                    isDuplicateRecording = false;
                }
            }
        });
    };
    private Emitter.Listener onStopScreen = args -> {
        Log.d("화면공유", "onStopScreen");
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("화면 공유", "isRecording 비 활성화");
                deleteScreenLayoutManager();
            }
        });
    };

    public void deleteScreenLayoutManager() {
        if (peerConnectionClient.surfaceTextureHelperList.contains("ARDAMSv1")) {
            peerConnectionClient.surfaceTextureHelperList.remove("ARDAMSv1");
        }
        if (peerConnectionClient.gridCount >= 2) {
            int i = peerConnectionClient.surfaceRendererAdapter.deleteScreenVideo();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (i != -1) {
                        peerConnectionClient.surfaceRendererAdapter.notifyItemRemoved(i);
                        GridLayoutManager layoutManager = (GridLayoutManager) peerConnectionClient.userRecyclerView.getLayoutManager();
                        layoutManager.setSpanCount(--peerConnectionClient.gridCount);
                    }
                }
            });
        }
    }

    private void startRecorder(String roomName) {
        TextView recorderView = mActivity.findViewById(R.id.recorderView);
        recorderView.setVisibility(View.VISIBLE);
        Toast.makeText(mActivity, "기록을 시작합니다.", Toast.LENGTH_SHORT).show();
        isRecording = true;
        isPermissionMap.put(roomName, true);
    }

    private Emitter.Listener onChangeHost = args -> {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, "이제 귀하는 호스트입니다.", Toast.LENGTH_SHORT).show();
            }
        });
    };

    private Emitter.Listener onSendAndroidMessage = args -> {
        String msg = (String) args[0];
        if (!Objects.equals(peerConnectionClient.doubleMessage, msg)) {
            peerConnectionClient.addMessage(msg, MESSAGE);
        }
    };

    public static void sendIce(IceCandidate iceCandidate) {
        mSocket.emit("ice", toJsonCandidate(iceCandidate), roomName);
    }
    public static void sendLeave() {
        Log.d(TAG, "sendLeave :" + name);
        mSocket.emit("leave_room", roomName, name);
    }
    public static void sendWebMessage(String msg) {
        mSocket.emit("send_web_message", roomName, msg);
    }

    public static void sendOffer(JSONObject message, String _name){
        Log.d(TAG, "sendOffer 재협상:" + name);
        mSocket.emit("offer", message, roomName, _name, name);
    }
    public static void sendRecorderRoom() {
        mSocket.emit("recorder_room", roomName);
    }
    public static void sendStopRecorderRoom() {
        mSocket.emit("stop_recorder_room", roomName);
    }

    public static void sendStopScreenRoom() {
        mSocket.emit("stop_screen_room", roomName);
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
    public static class SimpleSdpObserver implements SdpObserver {

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.e("재협상", "setLocalDescription 성공");
        }

        @Override
        public void onSetSuccess() {
            Log.e("재협상", "onSetSuccess");
        }

        @Override
        public void onCreateFailure(String s) {
            Log.e("재협상", "onCreateFailure : "+ s);
        }

        @Override
        public void onSetFailure(String s) {
            Log.e("재협상", "onSetFailure");
        }

    }
    void showDialog(String roomName) {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(mContext)
                .setTitle("이 방은 기록되고 있습니다.")
                .setMessage("회의 대화 내용을 저장하고 공유할 수도 있습니다.\n" +
                        "이 회의에 머무르면 귀하는 기록되는 것에 동의하는 것입니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startRecorder(roomName);
                    }
                })
                .setNegativeButton("회의 나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mActivity.finish();
                    }
                })
                .setCancelable(false);
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }
}