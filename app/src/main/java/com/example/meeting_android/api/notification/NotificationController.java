package com.example.meeting_android.api.notification;

import android.app.Activity;
import android.content.Context;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationController {

    public NotificationService notificationService;
    private Context mContext;
    private Activity mActivity;

    public NotificationController(Context mContext, Activity mActivity) {
        notificationService = new NotificationService(mContext, mActivity);
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    public void sendMain(String email) {
        notificationService.sendMail(email, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }

        });
    }
}
