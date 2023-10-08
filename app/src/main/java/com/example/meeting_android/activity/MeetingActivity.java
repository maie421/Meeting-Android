package com.example.meeting_android.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.meeting_android.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
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

public class MeetingActivity extends AppCompatActivity {


    VideoCapturer videoCapturer;
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    SurfaceTextureHelper surfaceTextureHelper;
    EglBase rootEglBase;
    EglBase.Context eglBaseContext;
    public PeerConnectionFactory peerConnectionFactory;
    SurfaceViewRenderer renderer;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        requestPermissions();

        renderer = findViewById(R.id.View);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.tab_mic) {
                return true;
            }
            if (itemId == R.id.tab_video) {
                return true;
            }
            if (itemId == R.id.tab_chat) {
                return true;
            }
            return false;
        });

    }

    public void getVideoTrack(){

        renderer.setMirror(false);
        renderer.init(eglBaseContext,  new RendererCommon.RendererEvents() {
            //첫 번째 프레임이 렌더링되면 콜백이 실행됩니다.
            @Override
            public void onFirstFrameRendered() {
                Log.i("RendererEvents","onFirstFrameRendered");
//                box.setVisiProfile(false);

            }
            //렌더링된 프레임 해상도 또는 회전이 변경되면 콜백이 실행됩니다.
            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {
                Log.i("RendererEvents","onFrameResolutionChanged");
            }

        });
        rootEglBase = EglBase.create();
        eglBaseContext= rootEglBase.getEglBaseContext();
        surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName(), eglBaseContext);


        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(getApplicationContext())
                .setEnableInternalTracer(true)
                .createInitializationOptions());
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        Log.i("options : ",options.toString());

        VideoEncoderFactory encoderFactory;
        VideoDecoderFactory decoderFactory;
        encoderFactory = new SoftwareVideoEncoderFactory();
        decoderFactory = new SoftwareVideoDecoderFactory();


        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();


        (getLocalVideo(true)).addSink(renderer);

    }

    // 내 기기의 (카메라 소스로 얻은) video track을 반환한다.
    public VideoTrack getLocalVideo(boolean status){

        VideoTrack localVideo;
        // 앞 카메라 요청
        videoCapturer = createCameraCapturer(status);
        Log.w("createCameraCapturer",videoCapturer.toString());
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());

        // 비디오 캡쳐 : getUserMedia 로 스트림 받아오기 시작?
        videoCapturer.startCapture(240, 320, 30);
        localVideo = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);

        return localVideo;

    }

    private VideoCapturer createCameraCapturer(boolean isFront) {

        Camera1Enumerator enumerator = new Camera1Enumerator(false);
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
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


    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Dynamic permissions are not required before Android M.
//onPermissionsGranted();
            return;
        }

        String[] missingPermissions = getMissingPermissions();
        if (missingPermissions.length != 0) {
            requestPermissions(missingPermissions, PERMISSION_REQUEST);
        } else {
//onPermissionsGranted();
        }
    }

    private static final int PERMISSION_REQUEST = 2;
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            String[] missingPermissions = getMissingPermissions();

            getVideoTrack();

        }

    }
    private void onPermissionsGranted() {
        // If an implicit VIEW intent is launching the app, go directly to that URL.
        final Intent intent = getIntent();

    }
    @TargetApi(Build.VERSION_CODES.M)
    private String[] getMissingPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new String[0];
        }

        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("TAG", "Failed to retrieve permissions.");
            return new String[0];
        }

        if (info.requestedPermissions == null) {
            Log.w("TAG", "No requested permissions.");
            return new String[0];
        }

        ArrayList<String> missingPermissions = new ArrayList<>();
        for (int i = 0; i < info.requestedPermissions.length; i++) {
            if ((info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                missingPermissions.add(info.requestedPermissions[i]);
            }
        }
        Log.d("TAG", "Missing permissions: " + missingPermissions);

        return missingPermissions.toArray(new String[missingPermissions.size()]);

    }
}