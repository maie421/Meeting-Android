package com.example.meeting_android.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.example.meeting_android.R;
import com.example.meeting_android.api.user.UserController;
import com.example.meeting_android.api.user.UserService;
import com.example.meeting_android.databinding.ActivityJoinRoomBinding;

public class JoinRoomActivity extends AppCompatActivity {
    private ActivityJoinRoomBinding binding;
    private UserController userController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join_room);

        userController = new UserController(this, this);
        userController.getUser();

        binding.meetingButton.setOnClickListener(v->{
            String name = binding.nameEditText.getText().toString();
            String joinRoom = binding.joinRoomEditText.getText().toString();

            Intent intent = new Intent(this, MeetingActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("joinRoom", joinRoom);
            startActivity(intent);
        });
    }
}