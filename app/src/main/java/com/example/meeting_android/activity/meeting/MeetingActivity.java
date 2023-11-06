package com.example.meeting_android.activity.meeting;

import static com.example.meeting_android.activity.chatting.MessageAdapter.messages;
import static com.example.meeting_android.activity.meeting.Recorder.isRecording;
import static com.example.meeting_android.activity.meeting.SurfaceRendererViewHolder.customVideoSink;
import static com.example.meeting_android.activity.meeting.SurfaceRendererViewHolder.localVideoTrack;
import static com.example.meeting_android.webrtc.PeerConnectionClient.peerDataChannelnMap;
import static com.example.meeting_android.webrtc.WebSocketClientManager.sendLeave;
import static com.example.meeting_android.webrtc.WebSocketClientManager.sendRecorderRoom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

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
    private CustomDialog customDialog;
    private String randomNumberAsString;
    public String name;
    public String hostName;
    public UserService userService;
    public RoomService roomService;
    public RoomController roomController;
    public Recorder recorder;
    private boolean isButtonClicked = false;
    private boolean isButtonRecorderClicked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        buttonDialog = findViewById(R.id.buttonDialog);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        recorderView = findViewById(R.id.recorderView);
        roomController = new RoomController(this, this);
        roomService = new RoomService(this, this);
        userService = new UserService(this, this);
        recorder = new Recorder(this, this);

        initDialog();

        onClickButtonNavigation();
        requestPermissions();
        initWebSocketClient();

        buttonDialog.setOnClickListener(v->{
            customDialog.show();
        });
    }

    private void initDialog() {
        Intent intent = getIntent();
        //비회원
        if (intent.hasExtra("name") && intent.hasExtra("joinRoom") && intent.hasExtra("hostName")) {
            name = intent.getStringExtra("name");
            hostName = intent.getStringExtra("hostName");
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
            if (itemId == R.id.tab_mic) {
                if (webSocketClientManager.peerConnectionClient.isAudio == true) {
                    item.setIcon(R.drawable.mic_close);
                }else{
                    item.setIcon(R.drawable.mic);
                }
                webSocketClientManager.peerConnectionClient.onAudioTrackSwitch();
                return true;
            }
            if (itemId == R.id.tab_video) {
                if (webSocketClientManager.peerConnectionClient.isCamera == true) {
                    item.setIcon(R.drawable.video_icon_close);
                }else{
                    item.setIcon(R.drawable.video_icon_128703);
                }
                webSocketClientManager.peerConnectionClient.onCameraSwitch();
                return true;
            }
            if (itemId == R.id.tab_background) {
                if (customVideoSink.filterEnabled){
                    customVideoSink.filterEnabled = false;
                }else{
                    customVideoSink.filterEnabled = true;
                    localVideoTrack.addSink(customVideoSink);
//                    webSocketClientManager.peerConnectionClient.peerConnectionMap.get(name).addTrack(localVideoTrack);
                }
                return true;

            }
            if (itemId == R.id.tab_recorder) {
                isButtonRecorderClicked = true;
                if (hostName == null || hostName.equals(name)){
                    if (isRecording){
                        recorder.stopRecording();
                        sendRecorderRoom();
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
            customDialog = new CustomDialog(this, this, randomNumberAsString, hostName);
            webSocketClientManager = new WebSocketClientManager(this, this, randomNumberAsString, name);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            if (hostName == null || hostName.equals(name)) {
                recorder.stopRecording();
                sendRecorderRoom();
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
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK && data != null) {
                recorder.mediaProjection = recorder.projectionManager.getMediaProjection(resultCode, data);
                recorder.initVirtualDisplay();
                recorder.mediaRecorder.start();
                recorderView.setVisibility(View.VISIBLE);
                isRecording = true;
                sendRecorderRoom();
            }
        }
    }

}