package com.example.meeting_android.webrtc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.meeting_android.api.notification.NotificationService;
import com.example.meeting_android.common.Common;

import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;

import java.util.ArrayList;
import java.util.List;

public class PeerConnectionClient {
    public Context mContext;
    public Activity mActivity;
    public PeerConnectionFactory peerConnectionFactory;
    private PeerConnection.RTCConfiguration configuration;

    public PeerConnectionClient(Context mContext, Activity mActivity) {
        this.mContext = mContext;
        this.mActivity = mActivity;
    }
    public void initPeer() {
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

        // STUN 서버 설정
//        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
//        PeerConnection.IceServer stunServer = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
//        iceServers.add(stunServer);

//        configuration = new PeerConnection.RTCConfiguration(iceServers);

    }
}

