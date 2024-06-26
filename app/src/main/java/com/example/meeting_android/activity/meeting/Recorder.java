package com.example.meeting_android.activity.meeting;

import static com.example.meeting_android.webrtc.WebSocketClientManager.sendStopRecorderRoom;
import static com.example.meeting_android.webrtc.WebSocketClientManager.sendStopScreenRoom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.example.meeting_android.R;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Recorder {
    public Activity mActivity;
    public Context mContext;
    public static boolean isRecording;
    public boolean isScreen;
    public static Map<String, Boolean> isPermissionMap = new HashMap<>();
    public MediaRecorder mediaRecorder;
    public MediaProjectionManager projectionManager;
    public MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    public static final int REQUEST_RECORDER_CODE = 1000;
    public static final int REQUEST_SCREEN_CODE = 2000;
    public static final String SCREEN = "screen";
    public static final String RECORDER = "recorder";
    private String mFileName;
    private String type;

    public Recorder(Activity activity, Context context, String type){
        this.mActivity = activity;
        this.mContext = context;
        this.type = type;
        this.isRecording = false;
    }

    private void initRecorder() {
        mFileName =  Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/Movies/"+System.currentTimeMillis()+"test.mp4";
        try {
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
        int code =  REQUEST_RECORDER_CODE;

        if (Objects.equals(type, SCREEN)){
            code = REQUEST_SCREEN_CODE;
        }

        initRecorder();
        Intent serviceIntent = new Intent(mContext, YourForegroundService.class);
        ContextCompat.startForegroundService(mContext, serviceIntent);

        projectionManager = (MediaProjectionManager) mContext.getSystemService(mContext.MEDIA_PROJECTION_SERVICE);
        mActivity.startActivityForResult(projectionManager.createScreenCaptureIntent(), code);
    }

    public void stopRecording() {
        isRecording = false;
        TextView recorderView = mActivity.findViewById(R.id.recorderView);
        recorderView.setVisibility(View.GONE);

        stopMediaRecorder();

        String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String audioFileName = "AUDIO_" + timeStamp + ".mp3"; // Use ".mp3" or ".aac" if required
        String fullAudioFilePath = baseDirectory + "/Music/" + audioFileName;
        extractAudioFromVideo(mFileName, fullAudioFilePath);

        sendStopRecorderRoom();
    }

    public void stopScreen(){
        isScreen = false;
        stopMediaRecorder();
        sendStopScreenRoom();
    }

    private void stopMediaRecorder() {
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

    private void extractAudioFromVideo(String videoFilePath, String outputAudioFilePath) {
        String cmd = "-i \"" + videoFilePath + "\" -vn -ar 44100 -ac 2 -ab 192k -f mp3 \"" + outputAudioFilePath;

        FFmpeg.executeAsync(cmd, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    Log.i("FFmpeg", "Audio extraction successful");
                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    Log.i("FFmpeg", "Audio extraction cancelled");
                } else {
                    Log.e("FFmpeg", "Audio extraction failed with return code: " + returnCode);
                }
            }
        });
    }
}
