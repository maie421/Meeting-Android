package com.example.meeting_android.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.example.meeting_android.R;
import com.example.meeting_android.api.notification.NotificationController;
import com.example.meeting_android.databinding.ActivityLoginBinding;
import com.example.meeting_android.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);

        NotificationController notificationController = new NotificationController(this, this);

        binding.verificationRequest.setOnClickListener(v->{
            notificationController.sendMain(binding.emailEditText.getText().toString());
        });
    }
}