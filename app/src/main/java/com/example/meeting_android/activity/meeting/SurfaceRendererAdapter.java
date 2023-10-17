package com.example.meeting_android.activity.meeting;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_android.R;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
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

import java.util.List;

public class SurfaceRendererAdapter extends RecyclerView.Adapter<SurfaceRendererAdapter.RendererViewHolder> {
    public String VIDEO_TRACK_ID = "ARDAMSv0";
    public String AUDIO_TRACK_ID = "ARDAMSa0";
    private List<MeetingVideo> meetings;
    private static final String TAG = "웹소켓";
    public EglBase.Context eglBaseContext;
    public static VideoTrack localVideoTrack;
    public static AudioTrack localAudioTrack;
    public PeerConnectionFactory peerConnectionFactory;
    public PeerConnection peerConnection;
    public MediaConstraints sdpMediaConstraints;
    public SurfaceTextureHelper surfaceTextureHelper;
    public Activity mActivity;
    public SurfaceRendererAdapter(Activity activity, List<MeetingVideo> meetings, EglBase.Context eglBaseContext, PeerConnectionFactory peerConnectionFactory, PeerConnection peerConnection, MediaConstraints sdpMediaConstraints, SurfaceTextureHelper surfaceTextureHelper) {
        this.meetings = meetings;
        this.eglBaseContext = eglBaseContext;
        this.peerConnectionFactory = peerConnectionFactory;
        this.peerConnection = peerConnection;
        this.sdpMediaConstraints = sdpMediaConstraints;
        this.surfaceTextureHelper = surfaceTextureHelper;
        this.mActivity = activity;
    }

    @Override
    public RendererViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surface_renderer, parent, false);
        return new RendererViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RendererViewHolder holder, int position) {
// Initialize and set up the SurfaceViewRenderer for each user
        MeetingVideo meetingVideo = meetings.get(position);
        SurfaceViewRenderer renderer = holder.surfaceRenderer;

        // Set renderer properties and add it to the appropriate view hierarchy
        initSurfaceViewRenderer(renderer);
        Log.d("디버그", "뭐냐");
        // Handle local or remote video tracks
        if (position == 0) {
            Log.d("디버그", "여긴어디");
            // Local user
            localVideoTrack = getLocalVideo(true);
            localVideoTrack.addSink(renderer);
            peerConnection.addTrack(localVideoTrack);
            peerConnection.addTrack(getAudioTrack());
        } else {
            Log.d("디버그", "여기?");
            // Remote user
            if (meetingVideo.mediaStream != null && !meetingVideo.mediaStream.videoTracks.isEmpty()) {
                Log.d("디버그", "들어와...?");
                VideoTrack remoteVideoTrack = meetingVideo.mediaStream.videoTracks.get(0);
                remoteVideoTrack.addSink(renderer);
                notifyItemInserted(meetings.size() - 1);
            }
        }
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    public void addUser(String userId) {
        MeetingVideo meetingVideo = new MeetingVideo(userId);
        meetings.add(meetingVideo);
        notifyItemInserted(meetings.size() - 1);
    }
    public void addMediaStream(String userId, MediaStream mediaStream) {
        MeetingVideo meetingVideo = new MeetingVideo(userId, mediaStream);
        meetings.add(meetingVideo);
//        notifyDataSetChanged();
    }

    public void removeUser(int position) {
        meetings.remove(position);
        notifyItemRemoved(position);
    }

    class RendererViewHolder extends RecyclerView.ViewHolder {
        SurfaceViewRenderer surfaceRenderer;

        RendererViewHolder(View itemView) {
            super(itemView);
            surfaceRenderer = itemView.findViewById(R.id.surfaceRenderer);
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
}
