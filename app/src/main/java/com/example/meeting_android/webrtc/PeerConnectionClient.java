package com.example.meeting_android.webrtc;

import static com.example.meeting_android.webrtc.WebSocketClientManager.sendIce;
import static org.webrtc.ContextUtils.getApplicationContext;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.example.meeting_android.R;

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
import java.util.List;

public class PeerConnectionClient {
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public Context mContext;
    public EglBase rootEglBase;
    public Activity mActivity;
    public VideoCapturer videoCapturer;
    public VideoTrack localTrack;
    public VideoTrack remoteTrack;
    public Boolean isCamera = true;
    public PeerConnectionFactory peerConnectionFactory;
    public SurfaceTextureHelper surfaceTextureHelper;
    public EglBase.Context eglBaseContext;
    private static final String TAG = "웹소켓";
    private PeerConnection.RTCConfiguration configuration;
    public PeerConnection peerConnection;
    public PeerConnection.Observer pcObserver;
    public MediaConstraints sdpMediaConstraints;
    SurfaceViewRenderer localView;
    SurfaceViewRenderer remoteView;

    public PeerConnectionClient(Context mContext, Activity mActivity) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        localView = mActivity.findViewById(R.id.View);
        remoteView = mActivity.findViewById(R.id.pip_video_view);

        initPeer();

        initSurfaceViewRenderer(localView);
        initSurfaceViewRenderer(remoteView);

        localTrack = getLocalVideo(true);
        localTrack.addSink(localView);
        peerConnection.addTrack(localTrack);
    }
    // SurfaceViewRenderer 초기화 메서드
    void initSurfaceViewRenderer(SurfaceViewRenderer view){
        view.setMirror(false);
        //뷰의 크기에 맞게 비디오, 프레임의 크기 조정 가로 세로 비율을 유지, 검은 색 테두리가 표시 될 수 있습니다.
        view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        view.removeFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap bitmap) {
                Log.i(TAG,"removeFrameListener");
            }
        });
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                Log.i(TAG,"onViewAttachedToWindow");

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                Log.i(TAG,"onViewDetachedFromWindow");

            }
        });
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            }
        });
        view.init(eglBaseContext,  new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {
                Log.i(TAG,"onFirstFrameRendered");
            }
            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {
                Log.i(TAG,"onFrameResolutionChanged");
            }
        });
    }

    private void initPeer() {
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
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer stunServer = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        iceServers.add(stunServer);

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
        createPeerConnection();
    }

    private void createPeerConnection() {
        peerConnection = peerConnectionFactory.createPeerConnection(configuration, pcObserver);
    }

    public VideoTrack getLocalVideo(boolean status){
        VideoTrack localVideo;
        videoCapturer = createCameraCapturer(status);
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());

        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(240, 320, 30);

        localVideo = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);

        return localVideo;
    }

    //전면 카메라 설정
    private VideoCapturer createCameraCapturer(boolean isFront) {
        Camera1Enumerator enumerator = new Camera1Enumerator(false);

        final String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new CameraVideoCapturer.CameraEventsHandler() {
                    @Override
                    public void onCameraError(String s) {
                        Log.e(TAG,"onCameraError");
                    }
                    @Override
                    public void onCameraDisconnected() {
                        Log.e(TAG,"onCameraDisconnected");
                    }

                    @Override
                    public void onCameraFreezed(String s) {
                        Log.e(TAG,"onCameraFreezed");
                    }

                    @Override
                    public void onCameraOpening(String s) {
                        Log.e(TAG,"onCameraOpening");
                    }

                    @Override
                    public void onFirstFrameAvailable() {
                        Log.e(TAG,"onFirstFrameAvailable");
                    }

                    @Override
                    public void onCameraClosed() {
                        Log.e(TAG,"onCameraClosed");
                    }
                });

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
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
                Log.d(TAG, "onRemoveStream : "+ mediaStream);
                SurfaceViewRenderer pip_video_view = mActivity.findViewById(R.id.pip_video_view);

                // 제거된 미디어 스트림의 비디오 트랙을 제거
                if (mediaStream.videoTracks.size() > 0) {
                    mediaStream.videoTracks.get(0).removeSink(pip_video_view);
                }

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
//                Log.d(TAG, "onAddTrack"+ mediaStreams);
            }
        };
    }

    private void getRemoteStream(MediaStream mediaStream) {
        try {
            SurfaceViewRenderer pip_video_view = mActivity.findViewById(R.id.pip_video_view);
            VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
            mActivity.runOnUiThread(() -> {
                try {
                    remoteVideoTrack.addSink(pip_video_view);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to add video sink", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to get video track", e);
        }
    }

    public void onCameraSwitch(){
        localTrack.setEnabled(isCamera = !isCamera);
    }
}

