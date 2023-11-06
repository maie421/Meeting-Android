package com.example.meeting_android.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import org.webrtc.VideoFrame;
import org.webrtc.YuvConverter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class VideoFrameToBitmapConverter {
    private final YuvConverter yuvConverter;

    public VideoFrameToBitmapConverter() {
        yuvConverter = new YuvConverter();
    }

    public Bitmap convert(VideoFrame frame) {
        VideoFrame.I420Buffer i420Buffer = frame.getBuffer().toI420();
        int width = i420Buffer.getWidth();
        int height = i420Buffer.getHeight();

        ByteBuffer yBuffer = i420Buffer.getDataY();
        ByteBuffer uBuffer = i420Buffer.getDataU();
        ByteBuffer vBuffer = i420Buffer.getDataV();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // Y
        yBuffer.get(nv21, 0, ySize);

        // U and V (NV21 format)
        ByteBuffer uvBuffer = ByteBuffer.wrap(nv21, ySize, uSize + vSize);
        vBuffer.get(uvBuffer.array(), ySize, vSize);
        uBuffer.get(uvBuffer.array(), ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);

        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        i420Buffer.release();

        return bitmap;
    }
}
