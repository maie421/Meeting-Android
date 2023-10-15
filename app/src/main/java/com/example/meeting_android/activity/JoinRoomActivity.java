package com.example.meeting_android.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.example.meeting_android.R;
import com.example.meeting_android.databinding.ActivityJoinRoomBinding;

public class JoinRoomActivity extends AppCompatActivity {
    private ActivityJoinRoomBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join_room);
    }
}