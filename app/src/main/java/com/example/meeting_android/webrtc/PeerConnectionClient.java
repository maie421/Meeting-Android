package com.example.meeting_android.webrtc;

import static com.example.meeting_android.activity.meeting.SurfaceRendererViewHolder.localAudioTrack;
import static com.example.meeting_android.activity.meeting.SurfaceRendererViewHolder.localVideoTrack;
import static com.example.meeting_android.webrtc.WebSocketClientManager.sendIce;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_android.R;
import com.example.meeting_android.activity.meeting.SurfaceRendererAdapter;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerConnectionClient {
    public Context mContext;
    public EglBase rootEglBase;
    public Activity mActivity;
    public Boolean isCamera = true;
    public Boolean isAudio = true;
    public PeerConnectionFactory peerConnectionFactory;
    public SurfaceTextureHelper surfaceTextureHelper;
    public EglBase.Context eglBaseContext;
    private String TAG = "웹소켓";
    private PeerConnection.RTCConfiguration configuration;
    public Map<String, PeerConnection> peerConnectionMap = new HashMap<>();
    public PeerConnection.Observer pcObserver;
    public MediaConstraints sdpMediaConstraints;
    public SurfaceRendererAdapter surfaceRendererAdapter;
    public RecyclerView userRecyclerView;
    public int gridCount = 1;
    public PeerConnectionClient(Context mContext, Activity mActivity, String name){
        this.mContext = mContext;
        this.mActivity = mActivity;

        userRecyclerView = mActivity.findViewById(R.id.recyclerView);

        initPeer(name);

        surfaceRendererAdapter = new SurfaceRendererAdapter(mActivity,new ArrayList<>(), eglBaseContext, peerConnectionFactory, peerConnectionMap ,sdpMediaConstraints, surfaceTextureHelper, name);
        userRecyclerView.setAdapter(surfaceRendererAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 1);
        userRecyclerView.setLayoutManager(gridLayoutManager);

        surfaceRendererAdapter.addMeetingVideoName(name);
    }
    private void initPeer(String name) {
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(mContext)
                .setEnableInternalTracer(true)
                .createInitializationOptions());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        Log.i("options : ", options.toString());

        VideoEncoderFactory encoderFactory = new SoftwareVideoEncoderFactory();
        VideoDecoderFactory decoderFactory = new SoftwareVideoDecoderFactory();

        // PeerConnectionFactory 생성 및 IceServer 추가
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        //서버 설정
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer stunServer = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        PeerConnection.IceServer turnServer = PeerConnection.IceServer.builder("turn:13.124.5.88:3478")
                .setUsername("username1")
                .setPassword("key1")
                .createIceServer();

        iceServers.add(stunServer);
        iceServers.add(turnServer);

        configuration = new PeerConnection.RTCConfiguration(iceServers);
        rootEglBase = EglBase.create();
        eglBaseContext= rootEglBase.getEglBaseContext();
        surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName(), eglBaseContext);

        Log.d(TAG,"createPeerConnection");
        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"));

        pcObserver();
        createPeerConnection(name);
    }

    public void createPeerConnection(String name) {
        peerConnectionMap.put(name, peerConnectionFactory.createPeerConnection(configuration, pcObserver));
    }

    private void pcObserver() {
        pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange : "+String.valueOf(signalingState));
            }

            // ICE (Interactive Connectivity Establishment) 연결 상태 변경에 대한 콜백
            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange : "+ iceConnectionState);

            }
            // ICE 연결 수신 상태가 변경될 때 호출되는 콜백
            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(TAG, "onIceConnectionReceivingChange : "+ b);
            }
            // ICE 후보 수집 상태 변경에 대한 콜백
            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange : "+ iceGatheringState);
            }
            // 새 ICE 후보가 생성될 때 호출되는 콜백
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate : "+ iceCandidate);
                sendIce(iceCandidate);
            }
            // 제거된 ICE 후보에 대한 콜백
            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved : "+ iceCandidates);
            }
            // 새로운 미디어 스트림이 추가될 때 호출되는 콜백
            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream : " + mediaStream);
                getRemoteStream(mediaStream);
            }
            // 제거된 미디어 스트림에 대한 콜백
            @Override
            public void onRemoveStream(MediaStream mediaStream) {
//                Log.d(TAG, "onRemoveStream : "+ mediaStream);
//                SurfaceViewRenderer pip_video_view = mActivity.findViewById(R.id.pip_video_view);
//
//                // 제거된 미디어 스트림의 비디오 트랙을 제거
//                if (mediaStream.videoTracks.size() > 0) {
//                    mediaStream.videoTracks.get(0).removeSink(pip_video_view);
//                }

            }
            // 데이터 채널이 생성될 때 호출되는 콜백
            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel : "+ dataChannel);
            }
            // 재협상이 필요한 경우 호출되는 콜백
            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded");
            }
            // 트랙이 추가될 때 호출되는 콜백
            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
//                SurfaceViewRenderer pip_video_view = mActivity.findViewById(R.id.pip_video_view);
//                for (MediaStream mediaStream : mediaStreams) {
//                    if (mediaStream.videoTracks.size() > 0) {
//                        Log.d(TAG, "onAddTrack: Adding video sink");
//                        mediaStream.videoTracks.get(1).addSink(pip_video_view);
//                    }
//                }
//                Log.d(TAG, "onAddTrack"+ 그mediaStreams);
            }
        };
    }

    private void getRemoteStream(MediaStream mediaStream) {
        if (mediaStream.videoTracks.size() > 0) {
            gridCount++;
            userRecyclerView.post(new Runnable() {
                public void run() {
                    surfaceRendererAdapter.addMeetingVideo(WebSocketClientManager.name, mediaStream);
                    GridLayoutManager layoutManager = (GridLayoutManager) userRecyclerView.getLayoutManager();
                    layoutManager.setSpanCount(gridCount);
                    surfaceRendererAdapter.notifyItemInserted(surfaceRendererAdapter.getItemCount() - 1);
                }
            });

        }
    }
//
    public void onCameraSwitch(){
        localVideoTrack.setEnabled(isCamera = !isCamera);
    }
    public void onAudioTrackSwitch(){
        localAudioTrack.setEnabled(isAudio = !isAudio);
    }
}

