package com.example.meeting_android.api.user;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

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
                }else {

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                String errorMessage = t.getMessage(); // 예외 메시지 가져오기
                Toast.makeText(mContext, "실패했습니다. 오류: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
