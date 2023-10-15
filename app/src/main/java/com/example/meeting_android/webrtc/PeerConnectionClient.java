package com.example.meeting_android.webrtc;

import static com.example.meeting_android.webrtc.WebSocketClientManager.sendIce;
import static org.webrtc.ContextUtils.getApplicationContext;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.meeting_android.R;
import com.google.gson.JsonObject;

import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
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
    public VideoTrack localTrack;
    public VideoTrack remoteTrack;
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

        SurfaceViewRendererInit(localView);
        SurfaceViewRendererInit(remoteView);

        localTrack = getLocalVideo(true);
        localTrack.addSink(localView);
        peerConnection.addTrack(localTrack);

    }
    void SurfaceViewRendererInit(SurfaceViewRenderer view){
        view.setMirror(false);
        view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        view.removeFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap bitmap) {
                Log.i("removeFrameListener :","");
            }
        });
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                Log.i("onViewAttached","ToWindow :");

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                Log.i("onViewDetached","FromWindow :");

            }
        });
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            }
        });
        view.init(eglBaseContext,  new RendererCommon.RendererEvents() {
            //            첫 번째 프레임이 렌더링되면 콜백이 실행됩니다.
            @Override
            public void onFirstFrameRendered() {
                Log.i("RendererEvents","onFirstFrameRendered");
//                box.setVisiProfile(false);

            }
            //            렌더링된 프레임 해상도 또는 회전이 변경되면 콜백이 실행됩니다.
            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {
                Log.i("RendererEvents","onFrameResolutionChanged");


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

    /**내 기기의(카메라 소스로 얻은) video track을 반환한다.
     *
     *@paramstatus
     *@return:내 기기의(카메라 소스로 얻은) video track
     */
    public VideoTrack getLocalVideo(boolean status){

        // localVideo 변수 선언
        VideoTrack localVideo;

        // videoCapturer : 비디오 소스에서 비디오 프레임을 캡처하고 VideoSource 객체에 전달하는 데 사용됩니다.
        videoCapturer = createCameraCapturer(status);
        // createCameraCapturer {videoCapturer} 출력
        Log.w("createCameraCapturer",videoCapturer.toString());

        // VideoSource 객체를 생성합니다. 이 VideoSource 객체는 로컬 미디어 스트림에서 비디오 소스로 사용됩니다.
        // 1번째 매개변수는 비디오 소스가 카메라 스트림(실시간 비디오)을 사용하는지 또는 비디오 파일 스트림(미리 녹화된 비디오)을 사용하는지를 나타냅니다.
        // videoCapturer.isScreencast() :비디오 캡처기가 화면 녹화 모드에서 동작하는 경우, 즉 사용자가 화면 공유를 수행하는 경우에 true 값을 반환합니다. 반면에 일반적인 카메라 캡처기의 경우, false 값을 반환합니다.
        // 즉, VideoCapturer 객체가 현재 어떤 모드로 동작하는지를 나타내는
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());


        //  비디오 캡처기를 초기화하고 비디오 프레임을 캡처하기 시작하는 데 사용
        // 비디오 캡처기의 콜백 함수를 설정하고, 캡처할 비디오 해상도, 비율 및 프레임 속도 등의 속성을 설정
        //  videoSource.getCapturerObserver() : 비디오 프레임을 전달할 CapturerObserver 객체
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        Log.i("videoSource.getObserver",videoSource.getCapturerObserver().toString());

        // 비디오 캡쳐 : getUserMedia 로 스트림 받아오기 시작?
        // 비디오 캡처를 시작하고, 캡처된 비디오 프레임을 VideoSink로 전달하기 위해 호출
        // 캡처할 비디오 프레임의 너비, 높이, 및 프레임 속도
        // 240, 320,30
        videoCapturer.startCapture(240, 320, 30);

        //        이 메서드는 로컬 비디오 트랙을 생성하는 데 사용됩니다.
        //  VideoSource 객체와 연결된 VideoTrack을 만듭니다.
        // VIDEO_TRACK_ID은 비디오 트랙 고유 식별자로 사용.
        localVideo = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);

        // 내 기기의 (카메라 소스로 얻은) video track
        return localVideo;
        // getLocalVideo 함수의 끝.

    }
    /**
     *
     *@paramisFront
     *@return:생성한videoCapturer반환,생성 실패시null반환.
     */
    private VideoCapturer createCameraCapturer(boolean isFront) {


        // Camera1Enumerator : Android 디바이스의 카메라 목록을 가져오고 선택한 카메라를 열기 위한 클래스
        // 매개변수 : true를 전달하면 전면 카메라만 사용하며, false를 전달하거나 이 매개변수를 생략하면 전면 카메라와 후면 카메라 모두 사용
        Camera1Enumerator enumerator = new Camera1Enumerator(false);

        // Android 디바이스에서 사용 가능한 카메라 디바이스를 열거할 수 있습니다.
        // 카메라의 ID와 이름을 갖는 CameraEnumerationAndroid.CaptureDeviceInfo 객체의 목록을 반환합니다.
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        // deviceNames 요소의수만큼 반복
        for (String deviceName : deviceNames) {

            Log.i("deviceName : ",deviceName);

            // A ? B : C => 조건 연산자, A가 참이면 B를 반환, 거짓이면 C를 반환
            // Camera1Enumerator.isFrontFacing() : 전면카메라면 true, 후면 카메라라면 false를 반환
            // Camera1Enumerator.isBackFacing() : 후면카메라면 true, 전면 카메라라면 false를 반환
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {

                // 매개변수로는 CameraEnumerationAndroid.CaptureDeviceInfo 객체와 CapturerObserver 객체, CameraEventsHandler 객체를 받습니다.
                // deviceName 카메라로 VideoCapturer 객체 생성
                //  선택한 카메라를 열기 위한 메소드
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new CameraVideoCapturer.CameraEventsHandler() {
                    // CameraVideoCapturer에서 발생하는 각 이벤트를 처리하는 기본 구현을 제공하며,
                    // 이벤트를 수신하려면 CameraVideoCapturer.setCameraEventsHandler() 메서드를 사용하여 CameraEventsHandler 객체를 등록해야 합니다.

                    // 오버라이딩, 함수 재정의
                    @Override
                    // 카메라에서 오류가 발생할 때 호출됩니다. 오류 메시지를 매개변수로 받습니다.
                    public void onCameraError(String s) {
                        // onCameraError 메세지 출력.
                        Log.w("onCameraError",s);
                        // onCameraError 함수의 끝.
                    }

                    // 오버라이딩, 함수 재정의
                    @Override
                    // 카메라 연결이 끊어졌을 때 호출됩니다.
                    public void onCameraDisconnected() {
                        // onCameraDisconnected 메세지 출력.
                        Log.w("onCameraDisconnected","");
                        // onCameraDisconnected 함수의 끝.
                    }

                    // 오버라이딩, 함수 재정의
                    @Override
                    // 카메라가 정지되거나 동결될 때 호출됩니다. 오류 메시지를 매개변수로 받습니다.
                    public void onCameraFreezed(String s) {
                        // onCameraFreezed 메세지 출력.
                        Log.w("onCameraFreezed",s);
                        // onCameraFreezed 함수의 끝.
                    }

                    // 오버라이딩, 함수 재정의
                    @Override
                    // 카메라를 열고 있는 동안 호출됩니다. 열려고 하는 카메라의 이름을 매개변수로 받습니다.
                    public void onCameraOpening(String s) {
                        // onCameraOpening 메세지 출력.
                        Log.w("onCameraOpening",s);
                        // onCameraOpening 함수의 끝.
                    }

                    // 오버라이딩, 함수 재정의
                    @Override
                    // 첫 번째 비디오 프레임이 사용 가능할 때 호출됩니다.
                    public void onFirstFrameAvailable() {
                        // onFirstFrameAvailable 메세지 출력.
                        Log.w("onFirstFrameAvailable","");
                        // onFirstFrameAvailable 함수의 끝.
                    }

                    // 오버라이딩, 함수 재정의
                    @Override
                    // 카메라가 닫혔을 때 호출됩니다.
                    public void onCameraClosed() {
                        // onCameraClosed 메세지 출력.
                        Log.w("onCameraClosed","");
                        // onCameraClosed 함수의 끝.
                    }
                    // createCapturer () 끝.
                });

                // 생성성공시 {} 안의 코드 실행.
                if (videoCapturer != null) {
                    // 생성한 videoCapturer 반환
                    return videoCapturer;
                    // 생성성공시 실행할 코드 끝.
                }

                // isFront가 true일 때 전면카메라, false일 때 후면 카메라면 실행할 코드 끝.
            }
            /// 반복하는 코드의 끝.
        }

        // null 반환
        return null;

        /// createCameraCapturer 함수 끝.
    }
    private void pcObserver() {
        pcObserver = new PeerConnection.Observer() {
            // SignalingState이 변경될 때 호출되는 콜백
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
                Log.d(TAG, "onRenegotiationNeeded : ");
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
}

