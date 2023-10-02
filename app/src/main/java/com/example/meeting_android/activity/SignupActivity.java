package com.example.meeting_android.activity;

import static com.example.meeting_android.common.Common.isValidEmail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.meeting_android.R;
import com.example.meeting_android.api.notification.NotificationController;
import com.example.meeting_android.api.user.User;
import com.example.meeting_android.api.user.UserController;
import com.example.meeting_android.common.Common;
import com.example.meeting_android.databinding.ActivityLoginBinding;
import com.example.meeting_android.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private UserController userController;
    private Common common;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);

        common = new Common(this,this);
        userController = new UserController(this,this);

        binding.registerButton.setOnClickListener(v->{
            String email = binding.emailEditText.getText().toString();
            String name = binding.nameEditText.getText().toString();
            String password = binding.passwordEditText.getText().toString();
            String checkPassword = binding.checkPasswordEditText.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "닉네임이 빈칸입니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일이 빈칸입니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail(email)) {
                Toast.makeText(this, "유효하지 않은 이메일 주소입니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(checkPassword)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User(email, name, password);
            userController.createUser(user);
        });
    }
}