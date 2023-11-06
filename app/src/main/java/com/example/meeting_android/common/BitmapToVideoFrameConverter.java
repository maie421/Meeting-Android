package com.example.meeting_android.common;

import android.graphics.Bitmap;

import org.webrtc.JavaI420Buffer;
import org.webrtc.VideoFrame;

import java.nio.ByteBuffer;

public class BitmapToVideoFrameConverter {
    public VideoFrame convert(Bitmap bitmap, long captureTimeNs) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] argb = new int[width * height];

        // Bitmap의 픽셀 데이터를 가져옵니다.
        bitmap.getPixels(argb, 0, width, 0, 0, width, height);

        // ARGB를 I420로 변환합니다.
        ByteBuffer yuvPlanes = ByteBuffer.allocateDirect(width * height * 3 / 2);
        ByteBuffer yPlane = yuvPlanes.slice();
        ByteBuffer uPlane = yuvPlanes.slice();
        ByteBuffer vPlane = yuvPlanes.slice();

        // 각각의 Plane의 위치를 지정합니다. Y는 전체 면적을 차지하지만 U와 V는 각각 1/4 면적을 차지합니다.
        uPlane.position(width * height);
        vPlane.position(width * height + width * height / 4);

        // WebRTC의 native 메서드를 사용하여 ARGB를 I420로 변환합니다.
        nativeARGBToI420(argb, yPlane, uPlane, vPlane, width, height);

        // VideoFrame 생성을 위해 JavaI420Buffer를 사용합니다.
        VideoFrame.I420Buffer i420Buffer = JavaI420Buffer.wrap(width, height, yPlane, width, uPlane, width / 2, vPlane, width / 2, null);

        // VideoFrame을 생성합니다. 회전값은 0으로 가정합니다.
        return new VideoFrame(i420Buffer, 0, captureTimeNs);
    }

    private native void nativeARGBToI420(int[] argb, ByteBuffer yPlane, ByteBuffer uPlane, ByteBuffer vPlane, int width, int height);

    // Ensure to load native library where 'nativeARGBToI420' is implemented
    static {
        System.loadLibrary("your_native_lib");
    }
}