package com.example.meeting_android.api.notification;

import android.app.Activity;
import android.content.Context;

import com.example.meeting_android.api.ApiService;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


public class NotificationService extends ApiService {
    public Context mContext;
    public Activity mActivity;
    public NotificationService(Context mContext, Activity mActivity){
        this.mContext = mContext;
        this.mActivity = mActivity;
    }
    public void sendMail(String email, Callback<ResponseBody> callback){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", email);

        NotificationApi notificationApi = retrofit.create(NotificationApi.class);
        Call<ResponseBody> call = notificationApi.sendMail(jsonObject);
        call.enqueue(callback);
    }

}
