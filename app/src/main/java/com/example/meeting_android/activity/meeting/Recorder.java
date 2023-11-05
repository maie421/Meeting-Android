package com.example.meeting_android.activity.meeting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.meeting_android.R;

import org.w3c.dom.Text;
import org.webrtc.MediaStream;

import java.io.File;
import java.io.IOException;

public class Recorder {
    public Activity mActivity;
    public Context mContext;
    public static boolean isRecording;
    public MediaRecorder mediaRecorder;
    public MediaProjectionManager projectionManager;
    public MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    private static final int REQUEST_CODE = 1000;

    public Recorder(Activity activity, Context context){
        this.mActivity = activity;
        this.mContext = context;
        this.isRecording = false;
    }

    private void initRecorder() {
        String mFileName =  Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/Movies/"+System.currentTimeMillis()+"test.mp4";
        try {
            isRecording = true;
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(mFileName);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("녹화", "An error occurred", e);
        }
    }

    public void initVirtualDisplay() {
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenDensity = metrics.densityDpi;

        Surface surface = mediaRecorder.getSurface();

        virtualDisplay = mediaProjection.createVirtualDisplay("MeetingActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null /*Callbacks*/, null /*Handler*/);
    }

    public void startScreenCapture() {
        initRecorder();
        Intent serviceIntent = new Intent(mContext, YourForegroundService.class);
        ContextCompat.startForegroundService(mContext, serviceIntent);

        projectionManager = (MediaProjectionManager) mContext.getSystemService(mContext.MEDIA_PROJECTION_SERVICE);
        mActivity.startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    public void stopRecording() {
        isRecording = false;
        TextView recorderView = mActivity.findViewById(R.id.recorderView);
        recorderView.setVisibility(View.GONE);

        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
    }
    private String getFilePath() {
        final String directory = Environment.getExternalStorageDirectory() + File.separator + "Recordings";
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

        }

        final File folder = new File(directory);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            String filePath = directory + File.separator + "capture_" + System.currentTimeMillis() + ".mp4";
            return filePath;
        } else {
        }
        return null;
    }


}
