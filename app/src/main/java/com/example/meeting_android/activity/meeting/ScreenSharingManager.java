package com.example.meeting_android.activity.meeting;

import android.media.projection.MediaProjection;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class ScreenSharingManager {
    private SurfaceViewRenderer renderer;
    private MediaProjection mediaProjection;
    private EglBase eglBase;

    // 생성자
    public ScreenSharingManager(SurfaceViewRenderer renderer, MediaProjection mediaProjection, EglBase eglBase) {
        this.renderer = renderer;
        this.mediaProjection = mediaProjection;
        this.eglBase = eglBase;
    }

    // SurfaceViewRenderer 초기화 및 설정
    public void initRenderer() {
        renderer.init(eglBase.getEglBaseContext(), null);
        renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
    }

    // 화면 공유 시작
    public void startScreenSharing() {
        // 화면 캡처 및 WebRTC 설정
        // MediaProjection을 사용하여 화면 캡처 시작
        // 화면 캡처된 프레임을 SurfaceViewRenderer에 렌더링
    }

    // 화면 공유 중지
    public void stopScreenSharing() {
        // 화면 공유 중지 및 정리 작업
    }
}

