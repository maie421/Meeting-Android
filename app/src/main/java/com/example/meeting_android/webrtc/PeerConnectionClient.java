package com.example.meeting_android.webrtc;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.meeting_android.R;

import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerConnectionClient {
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public Context mContext;
    public EglBase rootEglBase;
    public Activity mActivity;
    public VideoCapturer videoCapturer;
    private final PCObserver pcObserver = new PCObserver();
    public PeerConnectionFactory peerConnectionFactory;
    public SurfaceTextureHelper surfaceTextureHelper;
    private boolean isError = false;
    public EglBase.Context eglBaseContext;
    private final PeerConnectionEvents events;

    private static final String TAG = "PCRTCClient";
    private PeerConnection.RTCConfiguration configuration;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    public PeerConnection peerConnection;
    public PeerConnectionClient(Context mContext, Activity mActivity, PeerConnectionEvents events) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.events = events;
        initPeer();
        getVideoTrack();
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
        peerConnection = peerConnectionFactory.createPeerConnection(configuration, pcObserver);
    }

    private class PCObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            Log.i("웹소켓", String.valueOf(candidate));
            executor.execute(() -> events.onIceCandidate(candidate));
        }

        @Override
        public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
            executor.execute(() -> events.onIceCandidatesRemoved(candidates));
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {
            Log.d(TAG, "SignalingState: " + newState);
        }

        @Override
        public void onIceConnectionChange(final PeerConnection.IceConnectionState newState) {
            executor.execute(() -> {
                Log.d(TAG, "IceConnectionState: " + newState);
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    events.onIceConnected();
                } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    events.onIceDisconnected();
                } else if (newState == PeerConnection.IceConnectionState.FAILED) {
                    reportError("ICE connection failed.");
                }
            });
        }

        @Override
        public void onConnectionChange(final PeerConnection.PeerConnectionState newState) {
            executor.execute(() -> {
                Log.d(TAG, "PeerConnectionState: " + newState);
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    events.onConnected();
                } else if (newState == PeerConnection.PeerConnectionState.DISCONNECTED) {
                    events.onDisconnected();
                } else if (newState == PeerConnection.PeerConnectionState.FAILED) {
                    reportError("DTLS connection failed.");
                }
            });
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            Log.d(TAG, "IceGatheringState: " + newState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            Log.d(TAG, "IceConnectionReceiving changed to " + receiving);
        }

        @Override
        public void onSelectedCandidatePairChanged(CandidatePairChangeEvent event) {
            Log.d(TAG, "Selected candidate pair changed because: " + event);
        }

        @Override
        public void onAddStream(final MediaStream stream) {}

        @Override
        public void onRemoveStream(final MediaStream stream) {}

        @Override
        public void onDataChannel(final DataChannel dc) {
            Log.d(TAG, "New Data channel " + dc.label());

//            if (!dataChannelEnabled)
//                return;

            dc.registerObserver(new DataChannel.Observer() {
                @Override
                public void onBufferedAmountChange(long previousAmount) {
                    Log.d(TAG, "Data channel buffered amount changed: " + dc.label() + ": " + dc.state());
                }

                @Override
                public void onStateChange() {
                    Log.d(TAG, "Data channel state changed: " + dc.label() + ": " + dc.state());
                }

                @Override
                public void onMessage(final DataChannel.Buffer buffer) {
                    if (buffer.binary) {
                        Log.d(TAG, "Received binary msg over " + dc);
                        return;
                    }
                    ByteBuffer data = buffer.data;
                    final byte[] bytes = new byte[data.capacity()];
                    data.get(bytes);
                    String strData = new String(bytes, Charset.forName("UTF-8"));
                    Log.d(TAG, "Got msg: " + strData + " over " + dc);
                }
            });
        }

        @Override
        public void onRenegotiationNeeded() {
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }

        @Override
        public void onAddTrack(final RtpReceiver receiver, final MediaStream[] mediaStreams) {}

    }
    public interface PeerConnectionEvents {
        /**
         * Callback fired once local SDP is created and set.
         */
        void onLocalDescription(final SessionDescription sdp);

        /**
         * Callback fired once local Ice candidate is generated.
         */
        void onIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once local ICE candidates are removed.
         */
        void onIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * Callback fired once connection is established (IceConnectionState is
         * CONNECTED).
         */
        void onIceConnected();

        /**
         * Callback fired once connection is disconnected (IceConnectionState is
         * DISCONNECTED).
         */
        void onIceDisconnected();

        /**
         * Callback fired once DTLS connection is established (PeerConnectionState
         * is CONNECTED).
         */
        void onConnected();

        /**
         * Callback fired once DTLS connection is disconnected (PeerConnectionState
         * is DISCONNECTED).
         */
        void onDisconnected();

        /**
         * Callback fired once peer connection is closed.
         */
        void onPeerConnectionClosed();

        /**
         * Callback fired once peer connection statistics is ready.
         */
        void onPeerConnectionStatsReady(final StatsReport[] reports);

        /**
         * Callback fired once peer connection error happened.
         */
        void onPeerConnectionError(final String description);
    }

    private void reportError(final String errorMessage) {
        Log.e(TAG, "Peerconnection error: " + errorMessage);
        executor.execute(() -> {
            if (!isError) {
                events.onPeerConnectionError(errorMessage);
                isError = true;
            }
        });
    }

    public void getVideoTrack(){
        SurfaceViewRenderer renderer = mActivity.findViewById(R.id.View);
        renderer.setMirror(false);
        renderer.init(eglBaseContext,  new RendererCommon.RendererEvents() {
            //첫 번째 프레임이 렌더링되면 콜백이 실행됩니다.
            @Override
            public void onFirstFrameRendered() {
                Log.i("RendererEvents","onFirstFrameRendered");

            }
            //렌더링된 프레임 해상도 또는 회전이 변경되면 콜백이 실행됩니다.
            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {
                Log.i("RendererEvents","onFrameResolutionChanged");
            }

        });
        rootEglBase = EglBase.create();
        eglBaseContext = rootEglBase.getEglBaseContext();
        surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName(), eglBaseContext);
        (getLocalVideo(true)).addSink(renderer);
    }

    public VideoTrack getLocalVideo(boolean isFront){

        VideoTrack localVideo;
        // 앞 카메라 요청
        videoCapturer = createVideoCapturer(isFront);
        Log.w("createVideoCapturer",videoCapturer.toString());
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());

        // 비디오 캡쳐 : getUserMedia 로 스트림 받아오기 시작?
        videoCapturer.startCapture(240, 320, 30);
        localVideo = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);

        return localVideo;

    }

    private VideoCapturer createVideoCapturer(boolean isFront) {

        Camera1Enumerator enumerator = new Camera1Enumerator(false);
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new CameraVideoCapturer.CameraEventsHandler() {
                    @Override
                    public void onCameraError(String s) {
                        Log.w("onCameraError",s);
                    }

                    @Override
                    public void onCameraDisconnected() {
                        Log.w("onCameraDisconnected","");

                    }

                    @Override
                    public void onCameraFreezed(String s) {
                        Log.w("onCameraFreezed",s);

                    }

                    @Override
                    public void onCameraOpening(String s) {
                        Log.w("onCameraOpening",s);

                    }

                    @Override
                    public void onFirstFrameAvailable() {
                        Log.w("onFirstFrameAvailable","");

                    }

                    @Override
                    public void onCameraClosed() {
                        Log.w("onCameraClosed","");
                    }
                });

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

}

