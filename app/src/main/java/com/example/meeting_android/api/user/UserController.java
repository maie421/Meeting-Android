package com.example.meeting_android.api.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.meeting_android.activity.MainActivity;
import com.example.meeting_android.activity.MeetingMainActivity;
import com.example.meeting_android.activity.SplashActivity;
import com.example.meeting_android.common.Common;
import com.example.meeting_android.common.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserController {

    public UserService userService;
    private Context mContext;
    private Activity mActivity;
    private Common common;

    public UserController(Context mContext, Activity mActivity) {
        userService = new UserService(mContext,mActivity);
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.common = new Common(mContext, mActivity);
    }

    public void createUser(User user) {
        userService.createUser(user, new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("성공");
                    builder.setMessage("회원가입이 완료 되었습니다.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mActivity.finish();
                        }
                    });
                    builder.create().show();

                }else {
                    Toast.makeText(mContext, "실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                String errorMessage = t.getMessage();
                Toast.makeText(mContext, "실패했습니다. 오류: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loginUser(String email, String password){
        userService.loginUser(email, password, new Callback<Authorization>() {
            @Override
            public void onResponse(Call<Authorization> call, Response<Authorization> response) {
                if (response.isSuccessful()) {
                    Authorization authorization = response.body();

                    TokenManager tokenManager = new TokenManager(mContext);
                    tokenManager.saveToken(authorization.token);

                    //기존에 있던 화면 테스크 제거
                    Intent intent = new Intent(mContext, MeetingMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                }else {
                    Toast.makeText(mContext, "실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Authorization> call, Throwable t) {

            }
        });

    }
}
