package com.example.meeting_android.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.meeting_android.CustomDialog;
import com.example.meeting_android.R;
import com.example.meeting_android.webrtc.WebSocketClientManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Random;

public class MeetingActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST = 2;
    public BottomNavigationView bottomNavigationView;
    public WebSocketClientManager webSocketClientManager;
    public Button buttonDialog;
    private CustomDialog customDialog;
    private String randomNumberAsString;
    public String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        buttonDialog = findViewById(R.id.buttonDialog);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
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
        if (intent.hasExtra("name") && intent.hasExtra("joinRoom")) {
            name = intent.getStringExtra("name");
            randomNumberAsString = intent.getStringExtra("joinRoom");
            customDialog = new CustomDialog(this, this, randomNumberAsString, name);
        }else{
            Random random = new Random();
            int randomNumber = random.nextInt(10000);
            randomNumberAsString = Integer.toString(randomNumber);
            customDialog = new CustomDialog(this, this, randomNumberAsString);
        }
    }

    private void onClickButtonNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.tab_mic) {
                return true;
            }
            if (itemId == R.id.tab_video) {
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
        webSocketClientManager = new WebSocketClientManager(this, this, randomNumberAsString);
    }
}