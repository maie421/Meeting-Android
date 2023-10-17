package com.example.meeting_android.activity.meeting;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_android.R;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

public class SurfaceRendererViewHolder extends RecyclerView.ViewHolder {
    public SurfaceViewRenderer surfaceViewRenderer;
    public String VIDEO_TRACK_ID = "ARDAMSv0";
    public String AUDIO_TRACK_ID = "ARDAMSa0";
    private String TAG = "웹소켓";
    public static VideoTrack localVideoTrack;
    public static AudioTrack localAudioTrack;
    public PeerConnectionFactory peerConnectionFactory;
    public SurfaceTextureHelper surfaceTextureHelper;
    public EglBase.Context eglBaseContext;
    private PeerConnection.RTCConfiguration configuration;
    public PeerConnection peerConnection;
    public PeerConnection.Observer pcObserver;
    public MediaConstraints sdpMediaConstraints;
    SurfaceViewRenderer localView;
    SurfaceViewRenderer remoteView;
    private AudioSource audioSource;
    private SurfaceRendererAdapter surfaceRendererAdapter;
    public Activity mActivity;
    public SurfaceRendererViewHolder(@NonNull View itemView, Activity activity, EglBase.Context eglBaseContext, PeerConnectionFactory peerConnectionFactory, PeerConnection peerConnection, MediaConstraints sdpMediaConstraints, SurfaceTextureHelper surfaceTextureHelper) {
        super(itemView);
        surfaceViewRenderer = itemView.findViewById(R.id.surfaceRenderer);

        this.eglBaseContext = eglBaseContext;
        this.peerConnectionFactory = peerConnectionFactory;
        this.peerConnection = peerConnection;
        this.sdpMediaConstraints = sdpMediaConstraints;
        this.surfaceTextureHelper = surfaceTextureHelper;
        this.mActivity = activity;
    }

    public void localBind(MeetingVideo meetingVideo){
        initSurfaceViewRenderer(surfaceViewRenderer);

        localVideoTrack = getLocalVideo(true);
        localVideoTrack.addSink(surfaceViewRenderer);
        peerConnection.addTrack(localVideoTrack);
        peerConnection.addTrack(getAudioTrack());
    }

    public void remoteBind(MeetingVideo meetingVideo){
        initSurfaceViewRenderer(surfaceViewRenderer);
        Log.d("디버그","remoteBind");
        VideoTrack remoteVideoTrack = meetingVideo.mediaStream.videoTracks.get(0);
        if (meetingVideo.mediaStream.videoTracks.size() > 0) {
            mActivity.runOnUiThread(() -> {
                try {
                    remoteVideoTrack.addSink(surfaceViewRenderer);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to add video sink", e);
                }
            });
        }
    }

    void initSurfaceViewRenderer(SurfaceViewRenderer view){
        Log.d("디버그","initSurfaceViewRenderer");
        view.setMirror(false);
        //뷰의 크기에 맞게 비디오, 프레임의 크기 조정 가로 세로 비율을 유지, 검은 색 테두리가 표시 될 수 있습니다.
        view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        view.removeFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap bitmap) {
                Log.i(TAG,"removeFxrameListener");
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

    public VideoTrack getLocalVideo(boolean status){
        VideoCapturer videoCapturer = createCameraCapturer(status);
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());

        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(240, 320, 30);

        return peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
    }

    private AudioTrack getAudioTrack() {
        AudioSource audioSource = peerConnectionFactory.createAudioSource(sdpMediaConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(true);
        return localAudioTrack;
    }

    private VideoCapturer createCameraCapturer(boolean isFront) {
        Camera1Enumerator enumerator = new Camera1Enumerator(false);

        final String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new CameraVideoCapturer.CameraEventsHandler() {
                    @Override
                    public void onCameraError(String s) {
                        Log.e(TAG, "onCameraError");
                    }

                    @Override
                    public void onCameraDisconnected() {
                        Log.e(TAG, "onCameraDisconnected");
                    }

                    @Override
                    public void onCameraFreezed(String s) {
                        Log.e(TAG, "onCameraFreezed");
                    }

                    @Override
                    public void onCameraOpening(String s) {
                        Log.e(TAG, "onCameraOpening");
                    }

                    @Override
                    public void onFirstFrameAvailable() {
                        Log.e(TAG, "onFirstFrameAvailable");
                    }

                    @Override
                    public void onCameraClosed() {
                        Log.e(TAG, "onCameraClosed");
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