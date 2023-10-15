package com.example.meeting_android.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.meeting_android.R;

public class CustomDialog extends Dialog {
    Button copyButton;
    Button closeButton;
    public CustomDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.activity_custom_dialog);

        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
