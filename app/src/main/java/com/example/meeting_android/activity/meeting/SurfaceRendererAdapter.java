package com.example.meeting_android.activity.meeting;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_android.R;

import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SurfaceRendererAdapter extends RecyclerView.Adapter<SurfaceRendererViewHolder> {
    private List<MeetingVideo> meetings;

    public EglBase.Context eglBaseContext;
    public PeerConnectionFactory peerConnectionFactory;
    private Map<String, PeerConnection> peerConnectionMap;
    public MediaConstraints sdpMediaConstraints;
    public SurfaceTextureHelper surfaceTextureHelper;
    public Activity mActivity;
    public Context mContext;
    public String name;

    public SurfaceRendererAdapter(Activity activity, Context context, List<MeetingVideo> meetings, EglBase.Context eglBaseContext, PeerConnectionFactory peerConnectionFactory, Map<String, PeerConnection> peerConnectionMap, MediaConstraints sdpMediaConstraints, SurfaceTextureHelper surfaceTextureHelper, String name) {
        this.meetings = meetings;
        this.eglBaseContext = eglBaseContext;
        this.peerConnectionFactory = peerConnectionFactory;
        this.peerConnectionMap = peerConnectionMap;
        this.sdpMediaConstraints = sdpMediaConstraints;
        this.surfaceTextureHelper = surfaceTextureHelper;
        this.mContext = context;
        this.mActivity = activity;
        this.name = name;
    }

    @NonNull
    @Override
    public SurfaceRendererViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surface_renderer, parent, false);
        return new SurfaceRendererViewHolder(view,mActivity, mContext, eglBaseContext, peerConnectionFactory ,peerConnectionMap, sdpMediaConstraints, surfaceTextureHelper, name);
    }

    @Override
    public void onBindViewHolder(@NonNull SurfaceRendererViewHolder holder, int position) {
        MeetingVideo meetingVideo = meetings.get(position);

        if (Objects.equals(meetingVideo.type, "localScreen")){
            holder.localScreen(meetingVideo);
        }else{
            if (meetings.size() <= 1) {
                holder.localBind();
            } else {
                holder.remoteBind(meetingVideo);
            }
        }
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    public void addMeetingVideoName(String userName){
        MeetingVideo meetingVideo = new MeetingVideo(userName);

        this.meetings.add(meetingVideo);
        notifyItemInserted(meetings.size() - 1);
    }

    public void addMeetingVideo(String userName, MediaStream mediaStream, String type){
        MeetingVideo meetingVideo = new MeetingVideo(userName, mediaStream, type);
        this.meetings.add(meetingVideo);
    }
    public void addScreenVideo(String userName, MediaStream mediaStream, String type, EglBase.Context eglBase, VideoTrack videoTrack){
        MeetingVideo meetingVideo = new MeetingVideo(userName, mediaStream, type, eglBase, videoTrack);
        this.meetings.add(meetingVideo);
    }

    public int deleteMeetingVideo(String userName){
        for(int i = 0 ; i< meetings.size() ; i++){
            if (Objects.equals(meetings.get(i).name, userName)){
                Log.d("meetings","미디어 삭제 : " + meetings.get(i).name);
                meetings.remove(i);
                return i;
            }
        }
        return -1;
    }

    public int deleteScreenVideo(){
        for(int i = 0 ; i< meetings.size() ; i++){
            if (Objects.equals(meetings.get(i).type, "localScreen") || Objects.equals(meetings.get(i).type, "remoteScreen")){
                Log.d("meetings","미디어 삭제 : " + meetings.get(i).name);
                meetings.remove(i);
                return i;
            }
        }
        return -1;
    }
    public void clearMeetingVideo(){
        this.meetings.clear();
    }
}
