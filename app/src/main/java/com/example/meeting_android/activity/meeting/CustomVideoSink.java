package com.example.meeting_android.activity.meeting;

import static com.example.meeting_android.activity.meeting.SurfaceRendererViewHolder.localVideoTrack;


import android.opengl.GLES20;

import com.example.meeting_android.webrtc.PeerConnectionClient;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.TextureBufferImpl;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.nio.ByteBuffer;

public class CustomVideoSink implements VideoSink {
    private VideoSink target; // SurfaceViewRenderer
    private String name;
    private PeerConnection peerConnection;
    public boolean filterEnabled = false; // 필터 상태 플래그
    public CustomVideoSink(VideoSink target, String name, PeerConnection peerConnection) {
        this.target = target;
        this.peerConnection = peerConnection;
        this.name = name;
    }
    public void enableFilter(boolean enable) {
        this.filterEnabled = enable;
    }
    @Override
    public void onFrame(VideoFrame videoFrame) {
        if (filterEnabled) {
            VideoFrame filteredFrame = applyGreenFilter(videoFrame);
            target.onFrame(filteredFrame);
        } else {
            // 필터가 비활성화되어 있으면, 원본 프레임을 전달합니다.
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
            // 초록색 강도를 조절합니다. 값을 실험적으로 조정해보세요.
            int greenU = (uArray[i] & 0xFF) - 10; // U 값을 감소시킴
            uArray[i] = (byte) Math.max(0, Math.min(255, greenU));
        }
        uPlane.clear();
        uPlane.put(uArray);

        byte[] vArray = new byte[vPlane.remaining()];
        vPlane.get(vArray);
        for (int i = 0; i < vArray.length; i++) {
            // 초록색을 강조하기 위해 V 컴포넌트를 감소시킬 수도 있습니다.
            int greenV = (vArray[i] & 0xFF) - 10; // V 값을 감소시킴
            vArray[i] = (byte) Math.max(0, Math.min(255, greenV));
        }
        vPlane.clear();
        vPlane.put(vArray);

        VideoFrame filteredFrame = new VideoFrame(i420, frame.getRotation(), frame.getTimestampNs());
        return filteredFrame;
    }
}
