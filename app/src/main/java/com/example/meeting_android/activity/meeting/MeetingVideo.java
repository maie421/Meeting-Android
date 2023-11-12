package com.example.meeting_android.activity.meeting;

import org.webrtc.MediaStream;

public class MeetingVideo {
    public String name;
    public MediaStream mediaStream;
    public String type;

    public MeetingVideo(String name, MediaStream mediaStream, String type){
        this.name = name;
        this.mediaStream = mediaStream;
        this.type = type;
    }

    //로컬은 해당 생성자를 탐
    public MeetingVideo(String name){
        this.name = name;
        this.type = "video";
    }
}
