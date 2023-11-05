package com.example.meeting_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
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
    public Button closeButton;
    public TextView roomTextView;
    public TextView hostView;

    public CustomDialog(@NonNull Context context, Activity activity, String roomId, String name) {
        super(context);
        setContentView(R.layout.activity_custom_dialog);

        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());
        roomTextView = findViewById(R.id.roomTextView);
        hostView = findViewById(R.id.hostView);

        roomTextView.setText(roomId);
        hostView.setText(name);
    }
}
