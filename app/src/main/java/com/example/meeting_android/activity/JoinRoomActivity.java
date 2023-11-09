package com.example.meeting_android.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.example.meeting_android.R;
import com.example.meeting_android.api.room.RoomController;
import com.example.meeting_android.api.user.UserController;
import com.example.meeting_android.common.TokenManager;
import com.example.meeting_android.databinding.ActivityJoinRoomBinding;

public class JoinRoomActivity extends AppCompatActivity {
    private ActivityJoinRoomBinding binding;
    private UserController userController;
    private RoomController roomController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join_room);
        roomController = new RoomController(this, this);

        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        if (token != null) {
            userController = new UserController(this, this);
            userController.getUser();
        }

        binding.meetingButton.setOnClickListener(v->{
            String name = binding.nameEditText.getText().toString();
            String joinRoom = binding.joinRoomEditText.getText().toString();
            roomController.getRoom(joinRoom, name);
        });
    }
}