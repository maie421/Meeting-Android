package com.example.meeting_android.activity.meeting;

import static com.example.meeting_android.webrtc.WebSocketClientManager.sendLeave;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.meeting_android.CustomDialog;
import com.example.meeting_android.R;
import com.example.meeting_android.api.room.Room;
import com.example.meeting_android.api.room.RoomController;
import com.example.meeting_android.api.room.RoomService;
import com.example.meeting_android.api.user.User;
import com.example.meeting_android.api.user.UserService;
import com.example.meeting_android.common.TokenManager;
import com.example.meeting_android.webrtc.WebSocketClientManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST = 2;
    public BottomNavigationView bottomNavigationView;
    public WebSocketClientManager webSocketClientManager;
    public Button buttonDialog;
    private CustomDialog customDialog;
    private String randomNumberAsString;
    public String name;
    public String hostName;
    public UserService userService;
    public RoomService roomService;
    public RoomController roomController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        buttonDialog = findViewById(R.id.buttonDialog);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        roomController = new RoomController(this, this);
        roomService = new RoomService(this, this);
        userService = new UserService(this, this);

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
            if (itemId == R.id.tab_chat) {
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
        webSocketClientManager.peerConnectionClient.surfaceRendererAdapter.clearMeetingVideo();
        webSocketClientManager.peerConnectionClient.peerConnectionMap.clear();
        webSocketClientManager.offerList.clear();
        sendLeave();
    }
}