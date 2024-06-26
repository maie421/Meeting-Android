package com.example.meeting_android.activity.meeting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_android.R;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.Map;
public class SurfaceRendererViewHolder extends RecyclerView.ViewHolder {
    public SurfaceViewRenderer surfaceViewRenderer;
    public String VIDEO_TRACK_ID = "ARDAMSv0";
    public String AUDIO_TRACK_ID = "ARDAMSa0";
    private String TAG = "웹소켓";
    public static VideoTrack localVideoTrack;
    public static AudioTrack localAudioTrack;
    public PeerConnectionFactory peerConnectionFactory;
    public SurfaceTextureHelper surfaceTextureHelper;
    public static CustomVideoSink customVideoSink;
    private boolean isRendererInitialized = false;
    public EglBase.Context eglBaseContext;
    private Map<String, PeerConnection> peerConnectionMap;
    public VideoSource videoSource;
    public MediaConstraints sdpMediaConstraints;
    public Activity mActivity;
    public Context mContext;
    public String name;
    public SurfaceRendererViewHolder(@NonNull View itemView, Activity activity, Context context, EglBase.Context eglBaseContext, PeerConnectionFactory peerConnectionFactory, Map<String, PeerConnection> peerConnectionMap, MediaConstraints sdpMediaConstraints, SurfaceTextureHelper surfaceTextureHelper, String name) {
        super(itemView);
        surfaceViewRenderer = itemView.findViewById(R.id.surfaceRenderer);

        this.eglBaseContext = eglBaseContext;
        this.peerConnectionFactory = peerConnectionFactory;
        this.peerConnectionMap = peerConnectionMap;
        this.sdpMediaConstraints = sdpMediaConstraints;
        this.surfaceTextureHelper = surfaceTextureHelper;
        this.mActivity = activity;
        this.mContext = context;
        this.name = name;
    }

    public void localBind(){
        initSurfaceViewRenderer(surfaceViewRenderer);

        localVideoTrack = getLocalVideo(true);

        customVideoSink = new CustomVideoSink(surfaceViewRenderer, videoSource);
        localVideoTrack.addSink(customVideoSink);

        peerConnectionMap.get(name).addTrack(localVideoTrack);
        peerConnectionMap.get(name).addTrack(getAudioTrack());
    }

    public void remoteBind(MeetingVideo meetingVideo){
        customVideoSink.isFirst = true;
        Log.d("디버그","remoteBind");
        initSurfaceViewRenderer(surfaceViewRenderer);

        VideoTrack remoteVideoTrack = meetingVideo.mediaStream.videoTracks.get(0);
        if (meetingVideo.mediaStream.videoTracks.size() > 0) {
            try {
                remoteVideoTrack.addSink(surfaceViewRenderer);
            } catch (Exception e) {
                Log.e(TAG, "Failed to add video sink", e);
            }
        }
    }

    public void localScreen(MeetingVideo meetingVideo){
        meetingVideo.videoTrack.addSink(surfaceViewRenderer);
        initScreenSurfaceViewRenderer(surfaceViewRenderer, meetingVideo.eglBaseContext);
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
                Log.i(TAG,"onViewDetachedFromWindow" +v);

            }
        });
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            }
        });
        if (!isRendererInitialized) {
            view.init(eglBaseContext, new RendererCommon.RendererEvents() {
                @Override
                public void onFirstFrameRendered() {
                    Log.i(TAG, "onFirstFrameRendered");
                }

                @Override
                public void onFrameResolutionChanged(int i, int i1, int i2) {
                    Log.i(TAG, "onFrameResolutionChanged");
                }
            });
            isRendererInitialized = true;
        }
    }

    void initScreenSurfaceViewRenderer(SurfaceViewRenderer surfaceViewRenderer, EglBase.Context eglBaseContext){
        if (!isRendererInitialized) {
            surfaceViewRenderer.init(eglBaseContext,  new RendererCommon.RendererEvents() {
                @Override
                public void onFirstFrameRendered() {
                    Log.i("RendererEvents","onFirstFrameRendered");
                }
                @Override
                public void onFrameResolutionChanged(int i, int i1, int i2) {
                    Log.i("RendererEvents","onFrameResolutionChanged");
                }
            });
            isRendererInitialized = true;
        }
    }
    public VideoTrack getLocalVideo(boolean status){
        VideoCapturer videoCapturer = createCameraCapture(status);
        videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());

        videoCapturer.initialize(surfaceTextureHelper, mActivity, videoSource.getCapturerObserver());
        videoCapturer.startCapture(200, 200, 100);

        return peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID+name, videoSource);
    }

    private AudioTrack getAudioTrack() {
        AudioSource audioSource = peerConnectionFactory.createAudioSource(sdpMediaConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(true);
        return localAudioTrack;
    }

    private VideoCapturer createCameraCapture(boolean isFront) {
        Camera2Enumerator enumerator = new Camera2Enumerator(mContext);

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
                        Log.e(TAG, "onCameraFreezed" + s);
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
