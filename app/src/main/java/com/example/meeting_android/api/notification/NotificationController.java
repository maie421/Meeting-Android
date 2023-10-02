package com.example.meeting_android.api.notification;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meeting_android.R;
import com.example.meeting_android.common.Common;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationController {

    public NotificationService notificationService;
    private Context mContext;
    private Activity mActivity;
    private Common mCommon;

    public NotificationController(Context mContext, Activity mActivity) {
        notificationService = new NotificationService(mContext, mActivity);
        this.mCommon = new Common(mContext, mActivity);
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    /*
    public void sendMain(String email) {
        notificationService.sendMail(email, new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mCommon.alertDialog(mContext, "전송여부", "성공하였습니다.");

                    EditText editText = mActivity.findViewById(R.id.verificationCodeEditText);
                    Button button = mActivity.findViewById(R.id.verificationRequest);
                    TextView timerTextView = mActivity.findViewById(R.id.timerTextView);

                    timerTextView.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.VISIBLE);

                    button.setText("인증 확인");

                    long durationMillis = 180000;
                    CountDownTimer countDownTimer = new CountDownTimer(durationMillis, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            // 남은 시간을 분과 초로 변환하여 TextView에 표시합니다.
                            long minutes = millisUntilFinished / 60000;
                            long seconds = (millisUntilFinished % 60000) / 1000;
                            String timeRemaining = String.format("%02d:%02d", minutes, seconds);
                            timerTextView.setText(timeRemaining);
                        }

                        @Override
                        public void onFinish() {
                        }
                    };

                    // 타이머를 시작합니다.
                    countDownTimer.start();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mCommon.alertDialog(mContext, "전송여부", "실패했습니다.");
            }
        });
    }
    */
}
