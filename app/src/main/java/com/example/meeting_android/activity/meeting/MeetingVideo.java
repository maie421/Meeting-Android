package com.example.meeting_android.activity.meeting;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.VideoTrack;

public class MeetingVideo {
    public String name;
    public MediaStream mediaStream;
    public String type;
    public EglBase.Context eglBaseContext;
    public VideoTrack videoTrack;

    public MeetingVideo(String name, MediaStream mediaStream, String type){
        this.name = name;
        this.mediaStream = mediaStream;
        this.type = type;
    }

    public MeetingVideo(String name, MediaStream mediaStream, String type, EglBase.Context eglBaseContext, VideoTrack videoTrack){
        this.name = name;
        this.mediaStream = mediaStream;
        this.type = type;
        this.eglBaseContext = eglBaseContext;
        this.videoTrack = videoTrack;
    }

    //로컬은 해당 생성자를 탐
    public MeetingVideo(String name){
        this.name = name;
        this.type = "video";
    }
}
