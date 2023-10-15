package com.example.meeting_android.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.example.meeting_android.R;
import com.example.meeting_android.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.registerButton.setOnClickListener(v->{
            Intent intent = new Intent(this, TermsActivity.class);
            startActivity(intent);
        });

        binding.loginButton.setOnClickListener(v->{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        binding.meetingButton.setOnClickListener(v->{
            Intent intent = new Intent(this, JoinRoomActivity.class);
            startActivity(intent);
        });
    }
}