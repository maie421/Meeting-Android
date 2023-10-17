package com.example.meeting_android.activity.meeting;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_android.R;

import org.webrtc.AudioTrack;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoTrack;
import java.util.List;

public class SurfaceRendererAdapter extends RecyclerView.Adapter<SurfaceRendererViewHolder> {
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
    private int gridCount = 1;
    public SurfaceRendererAdapter(Activity activity, List<MeetingVideo> meetings, EglBase.Context eglBaseContext, PeerConnectionFactory peerConnectionFactory, PeerConnection peerConnection, MediaConstraints sdpMediaConstraints, SurfaceTextureHelper surfaceTextureHelper) {
        this.meetings = meetings;
        this.eglBaseContext = eglBaseContext;
        this.peerConnectionFactory = peerConnectionFactory;
        this.peerConnection = peerConnection;
        this.sdpMediaConstraints = sdpMediaConstraints;
        this.surfaceTextureHelper = surfaceTextureHelper;
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public SurfaceRendererViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surface_renderer, parent, false);
        return new SurfaceRendererViewHolder(view,mActivity, eglBaseContext,peerConnectionFactory ,peerConnection, sdpMediaConstraints, surfaceTextureHelper);
    }

    @Override
    public void onBindViewHolder(@NonNull SurfaceRendererViewHolder holder, int position) {
        MeetingVideo meetingVideo = meetings.get(position);

        if (meetings.size() <= 1) {
            holder.localBind(meetingVideo);
        }else {
            holder.remoteBind(meetingVideo);
        }
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    public void addMeetingVideoName(String userName){
       MeetingVideo meetingVideo = new MeetingVideo(userName);
       this.meetings.add(meetingVideo);
        notifyItemInserted(meetings.size()-1);
    }

    public void addMeetingVideo(String userName, MediaStream mediaStream){
        MeetingVideo meetingVideo = new MeetingVideo(userName, mediaStream);
        this.meetings.add(meetingVideo);
    }
}
