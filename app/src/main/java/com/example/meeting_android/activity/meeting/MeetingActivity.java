package com.example.meeting_android.activity.meeting;

import static com.example.meeting_android.activity.chatting.MessageAdapter.messages;
import static com.example.meeting_android.activity.meeting.Recorder.RECORDER;
import static com.example.meeting_android.activity.meeting.Recorder.REQUEST_RECORDER_CODE;
import static com.example.meeting_android.activity.meeting.Recorder.REQUEST_SCREEN_CODE;
import static com.example.meeting_android.activity.meeting.Recorder.SCREEN;
import static com.example.meeting_android.activity.meeting.Recorder.isRecording;
import static com.example.meeting_android.activity.meeting.SurfaceRendererViewHolder.customVideoSink;
import static com.example.meeting_android.activity.meeting.SurfaceRendererViewHolder.localVideoTrack;
import static com.example.meeting_android.webrtc.PeerConnectionClient.peerDataChannelnMap;
import static com.example.meeting_android.webrtc.WebSocketClientManager.sendLeave;
import static com.example.meeting_android.webrtc.WebSocketClientManager.sendRecorderRoom;
import static com.example.meeting_android.webrtc.WebSocketClientManager.sendStopRecorderRoom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meeting_android.CustomDialog;
import com.example.meeting_android.R;
import com.example.meeting_android.activity.MainActivity;
import com.example.meeting_android.activity.MeetingMainActivity;
import com.example.meeting_android.activity.chatting.ChattingMainActivity;
import com.example.meeting_android.api.room.Room;
import com.example.meeting_android.api.room.RoomController;
import com.example.meeting_android.api.room.RoomService;
import com.example.meeting_android.api.user.User;
import com.example.meeting_android.api.user.UserService;
import com.example.meeting_android.common.TokenManager;
import com.example.meeting_android.webrtc.WebSocketClientManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST = 2;
    public static int messageCount = 0;
    public BottomNavigationView bottomNavigationView;
    public TextView recorderView;
    public WebSocketClientManager webSocketClientManager;
    public Button buttonDialog;
    public static CustomDialog customDialog;
    private String randomNumberAsString;
    public static String name;
    public static String hostRecordName;
    public UserService userService;
    public RoomService roomService;
    public RoomController roomController;
    public Recorder recorder;
    public Recorder screen;
    private boolean isButtonClicked = false;
    private boolean isButtonRecorderClicked = false;
    private String[] filterColor = {"없음","초록","회색"};
    private AlertDialog selectFilterDialog;
    private SurfaceViewRenderer surfaceScreenRenderer;
    private EglBase.Context eglBaseContext;

    private int selectBlockFilter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        buttonDialog = findViewById(R.id.buttonDialog);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        recorderView = findViewById(R.id.recorderView);
        surfaceScreenRenderer = findViewById(R.id.surfaceScreenRenderer);

        roomController = new RoomController(this, this);
        roomService = new RoomService(this, this);
        userService = new UserService(this, this);
        recorder = new Recorder(this, this, RECORDER);
        screen = new Recorder(this, this, SCREEN);

        initDialog();

        onClickButtonNavigation();
        requestPermissions();
        initWebSocketClient();

        buttonDialog.setOnClickListener(v->{
            customDialog.show();
        });

        selectFilterDialog = new AlertDialog.Builder(this )
                .setItems(filterColor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        customVideoSink.selectFilter = i;
                        selectBlockFilter = i;
                    }
                })
                .setTitle("비디오 필터")
                .create();
    }

    private void initDialog() {
        Intent intent = getIntent();
        //비회원
        if (intent.hasExtra("name") && intent.hasExtra("joinRoom") && intent.hasExtra("hostName")) {
            name = intent.getStringExtra("name");
            hostRecordName = intent.getStringExtra("hostName");
            randomNumberAsString = intent.getStringExtra("joinRoom");
        }else{
            //방생성
            Random random = new Random();
            int randomNumber = random.nextInt(10000);
            randomNumberAsString = Integer.toString(randomNumber);
        }
    }

    private void onClickButtonNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
//            if (itemId == R.id.tab_mic) {
//                if (webSocketClientManager.peerConnectionClient.isAudio == true) {
//                    item.setIcon(R.drawable.mic_close);
//                }else{
//                    item.setIcon(R.drawable.mic);
//                }
//                webSocketClientManager.peerConnectionClient.onAudioTrackSwitch();
//                return true;
//            }
            if (itemId == R.id.tab_video) {
                if (webSocketClientManager.peerConnectionClient.isCamera == true) {
                    item.setIcon(R.drawable.video_icon_close);
                    customVideoSink.selectFilter = 0;
                }else{
                    item.setIcon(R.drawable.video_icon_128703);
                    customVideoSink.selectFilter = selectBlockFilter;
                }
                webSocketClientManager.peerConnectionClient.onCameraSwitch();
                return true;
            }
            if (itemId == R.id.tab_background) {
                selectFilterDialog.show();
                return true;
            }
            if (itemId == R.id.tab_screen) {
                screen.startScreenCapture();
                return true;
            }
            if (itemId == R.id.tab_recorder) {
                isButtonRecorderClicked = true;
                if (hostRecordName.equals(name)){
                    if (isRecording){
                        recorder.stopRecording();
                        sendStopRecorderRoom();
                        item.setIcon(R.drawable.recorder);
                    }else{
                        recorder.startScreenCapture();
                        item.setIcon(R.drawable.stop_circled);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isButtonRecorderClicked = false;
                        }
                    }, 3000);  // 예: 1초 동안 클릭 무시
                }else{
                    Toast.makeText(this, "호스트만 기록할 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            if (itemId == R.id.tab_chat) {
                if (!isButtonClicked) {
                    isButtonClicked = true;
                    Intent intent = new Intent(this, ChattingMainActivity.class);
                    startActivity(intent);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isButtonClicked = false;
                        }
                    }, 1000);  // 예: 1초 동안 클릭 무시
                }
                return true;

            }
            return false;
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        String[] missingPermissions = getMissingPermissions();
        if (missingPermissions.length != 0) {
            requestPermissions(missingPermissions, PERMISSION_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private String[] getMissingPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new String[0];
        }

        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("TAG", "Failed to retrieve permissions.");
            return new String[0];
        }

        if (info.requestedPermissions == null) {
            Log.w("TAG", "No requested permissions.");
            return new String[0];
        }

        ArrayList<String> missingPermissions = new ArrayList<>();
        for (int i = 0; i < info.requestedPermissions.length; i++) {
            if ((info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                missingPermissions.add(info.requestedPermissions[i]);
            }
        }
        Log.d("TAG", "Missing permissions: " + missingPermissions);

        return missingPermissions.toArray(new String[missingPermissions.size()]);

    }

    private void initWebSocketClient() {
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        if (token != null) {
            userService.getUser(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        User user = response.body();
                        hostRecordName = user.name;
                        name = user.name;

                        customDialog = new CustomDialog(userService.mContext, userService.mActivity, randomNumberAsString, user.name);
                        webSocketClientManager = new WebSocketClientManager(userService.mContext, userService.mActivity, randomNumberAsString, user.name);

                        Room room = new Room(randomNumberAsString);
                        roomController.createRoom(room);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                }
            });
        }else {
            customDialog = new CustomDialog(this, this, randomNumberAsString, hostRecordName);
            webSocketClientManager = new WebSocketClientManager(this, this, randomNumberAsString, name);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customVideoSink.selectFilter = 0;
        Iterator<String> keys = webSocketClientManager.peerConnectionClient.peerConnectionMap.keySet().iterator();

        while( keys.hasNext() ){
            String strKey = keys.next();
            webSocketClientManager.peerConnectionClient.peerConnectionMap.get(strKey).close();
        }

        webSocketClientManager.peerConnectionClient.surfaceRendererAdapter.clearMeetingVideo();
        webSocketClientManager.peerConnectionClient.peerConnectionMap.clear();
        peerDataChannelnMap.clear();
        webSocketClientManager.offerList.clear();
        messages.clear();
        sendLeave();

        if (isRecording){
            //host 만
            if (hostRecordName.equals(name)) {
                recorder.stopRecording();
                sendStopRecorderRoom();
            }
            isRecording = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView textView = findViewById(R.id.messageCount);
        textView.setVisibility(View.GONE);
        messageCount = 0;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RECORDER_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                recorder.mediaProjection = recorder.projectionManager.getMediaProjection(resultCode, data);
                recorder.initVirtualDisplay();
                recorder.mediaRecorder.start();
                recorderView.setVisibility(View.VISIBLE);
                isRecording = true;
                sendRecorderRoom();
            }
        }

        if (requestCode == REQUEST_SCREEN_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                screen.mediaProjection = screen.projectionManager.getMediaProjection(resultCode, data);
                screen.initVirtualDisplay();
                screen.mediaRecorder.start();
                initWebRTC(data);
            }
        }
    }

    private void initWebRTC(Intent data){
        VideoCapturer videoCapturer = createVideoCapturer(data);
        MediaStream mediaStream = createMediaStream(videoCapturer);

        surfaceScreenRenderer.setVisibility(View.VISIBLE);

        surfaceScreenRenderer.init(eglBaseContext,  new RendererCommon.RendererEvents() {
            //            첫 번째 프레임이 렌더링되면 콜백이 실행됩니다.
            @Override
            public void onFirstFrameRendered() {
                Log.i("RendererEvents","onFirstFrameRendered");
            }
            //            렌더링된 프레임 해상도 또는 회전이 변경되면 콜백이 실행됩니다.
            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {
                Log.i("RendererEvents","onFrameResolutionChanged");
            }
        });
    }
    public MediaStream createMediaStream(VideoCapturer videoCapturer){
        EglBase rootEglBase = EglBase.create();
        eglBaseContext = rootEglBase.getEglBaseContext();
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName(), eglBaseContext);


        // MediaStream 생성
        PeerConnectionFactory peerConnectionFactory = webSocketClientManager.peerConnectionClient.peerConnectionFactory;
        MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");

        // 비디오 소스와 트랙 생성
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);

        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
        videoCapturer.startCapture(200, 200, 100);

        // 오디오 소스와 트랙 생성
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        AudioTrack audioTrack = peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource);

        // 미디어 스트림에 비디오 및 오디오 트랙 추가
        mediaStream.addTrack(videoTrack);
        mediaStream.addTrack(audioTrack);

        VideoTrack stream = peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
        stream.addSink(surfaceScreenRenderer);

        return mediaStream;
    }
    private VideoCapturer createVideoCapturer(Intent mediaProjectionPermissionResultData) {
        return new ScreenCapturerAndroid(mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                Log.i("video ","video capture 멈춤");
            }
        });
    }

}