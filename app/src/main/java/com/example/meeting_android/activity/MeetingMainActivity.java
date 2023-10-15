package com.example.meeting_android.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.example.meeting_android.R;
import com.example.meeting_android.databinding.ActivityMeetingMainBinding;

public class MeetingMainActivity extends AppCompatActivity {
    private ActivityMeetingMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_meeting_main);

        binding.createRoomButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MeetingActivity.class);
            startActivity(intent);
        });
    }
}