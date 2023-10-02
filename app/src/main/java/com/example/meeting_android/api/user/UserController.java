package com.example.meeting_android.api.user;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.meeting_android.common.Common;

import okhttp3.ResponseBody;
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
        this.common = new Common(mContext,mActivity);
    }

    public void createUser(User user) {
        userService.createUser(user, new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    common.alertDialog(mContext,"성공","회원가입이 완료되었습니다.");
                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }
}
