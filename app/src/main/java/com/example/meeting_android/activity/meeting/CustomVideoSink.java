package com.example.meeting_android.activity.meeting;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CustomVideoSink implements VideoSink {
    private VideoSink target;
    public VideoSource videoSource;
    public boolean isFilterEnabled = false; // 필터 상태 플래그
    public CustomVideoSink(VideoSink target, VideoSource videoSource) {
        this.target = target;
        this.videoSource = videoSource;

    }
    @Override
    public void onFrame(VideoFrame videoFrame) {
        if (isFilterEnabled) {
            VideoFrame filteredFrame = applyGreenFilter(videoFrame);
            videoSource.getCapturerObserver().onFrameCaptured(filteredFrame);
            target.onFrame(filteredFrame);
        } else {
            target.onFrame(videoFrame);
        }
    }
    public VideoFrame applyGreenFilter(VideoFrame frame){
        VideoFrame.I420Buffer i420 = frame.getBuffer().toI420();

        ByteBuffer uPlane = i420.getDataU();
        ByteBuffer vPlane = i420.getDataV();

        byte[] uArray = new byte[uPlane.remaining()];
        uPlane.get(uArray);
        for (int i = 0; i < uArray.length; i++) {
            int greenU = (uArray[i] & 0xFF) - 10;
            uArray[i] = (byte) Math.max(0, Math.min(255, greenU));
        }
        uPlane.clear();
        uPlane.put(uArray);

        byte[] vArray = new byte[vPlane.remaining()];
        vPlane.get(vArray);
        for (int i = 0; i < vArray.length; i++) {
            int greenV = (vArray[i] & 0xFF) - 10;
            vArray[i] = (byte) Math.max(0, Math.min(255, greenV));
        }
        vPlane.clear();
        vPlane.put(vArray);

        VideoFrame filteredFrame = new VideoFrame(i420, frame.getRotation(), frame.getTimestampNs());
        return filteredFrame;
    }

    public VideoFrame applyGrayFilter(VideoFrame frame){
        VideoFrame.I420Buffer i420 = frame.getBuffer().toI420();

        // Y 플레인은 밝기 정보를 담고 있으므로, 이를 조절하여 초록색의 밝기를 조절할 수 있습니다.
        ByteBuffer yPlane = i420.getDataY();
        ByteBuffer uPlane = i420.getDataU();
        ByteBuffer vPlane = i420.getDataV();

        // U와 V 플레인을 중간 값으로 설정하여 초록색 필터를 적용합니다.
        byte[] uArray = new byte[uPlane.remaining()];
        Arrays.fill(uArray, (byte) 128); // U 값을 중간값으로 설정
        uPlane.clear();
        uPlane.put(uArray);

        byte[] vArray = new byte[vPlane.remaining()];
        Arrays.fill(vArray, (byte) 128); // V 값을 중간값으로 설정
        vPlane.clear();
        vPlane.put(vArray);

        VideoFrame filteredFrame = new VideoFrame(i420, frame.getRotation(), frame.getTimestampNs());
        return filteredFrame;
    }

}
