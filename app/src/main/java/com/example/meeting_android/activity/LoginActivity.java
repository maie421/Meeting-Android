package com.example.meeting_android.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.meeting_android.R;
import com.example.meeting_android.api.user.UserController;
import com.example.meeting_android.common.Common;
import com.example.meeting_android.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private UserController userController;
    private Common common;
    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        common = new Common(this,this);
        userController = new UserController(this,this);

        binding.loginButton.setOnClickListener(v->{
            String email = binding.emailEditText.getText().toString();
            String password = binding.passwordEditText.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일이 빈칸입니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "비밀번호가 빈칸입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            userController.loginUser(email, password);
        });

        binding.registerTextView.setOnClickListener(v->{
            Intent intent = new Intent(this, TermsActivity.class);
            startActivity(intent);
            finish();
        });

    }
}