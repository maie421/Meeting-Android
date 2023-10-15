package com.example.meeting_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.meeting_android.api.user.User;
import com.example.meeting_android.api.user.UserService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomDialog extends Dialog {
    public Button copyButton;
    public Button closeButton;
    public UserService userService;

    public CustomDialog(@NonNull Context context, Activity activity, String roomId) {
        super(context);

        setContentView(R.layout.activity_custom_dialog);

        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        userService = new UserService(context, activity);
        userService.getUser( new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    TextView nameTextView = findViewById(R.id.nameTextView);
                    TextView roomTextView = findViewById(R.id.roomTextView);

                    nameTextView.setText(user.name);
                    roomTextView.setText(roomId);
                }else {
                    Toast.makeText(context, "토큰 만료", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }
    public CustomDialog(@NonNull Context context, Activity activity, String roomId, String name) {
        super(context);
        setContentView(R.layout.activity_custom_dialog);

        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        TextView nameTextView = findViewById(R.id.nameTextView);
        TextView roomTextView = findViewById(R.id.roomTextView);

        nameTextView.setText(name);
        roomTextView.setText(roomId);

    }
}
