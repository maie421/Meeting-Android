package com.example.meeting_android.api.user;

import android.app.Activity;
import android.content.Context;

import com.example.meeting_android.api.notification.NotificationApi;
import com.example.meeting_android.common.TokenManager;
import com.example.meeting_android.api.ApiService;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


public class UserService extends ApiService {
    public Context mContext;
    public Activity mActivity;
    public UserApi userApi;
    public UserService(Context mContext, Activity mActivity){
        this.mContext = mContext;
        this.mActivity = mActivity;
        userApi = retrofit.create(UserApi.class);
    }

    public void createUser(User user, Callback<User> callback){
        Call<User> call = userApi.createUser(user);
        call.enqueue(callback);
    }
    public void loginUser(String email,String password, Callback<Authorization> callback){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("password", password);

        Call<Authorization> call = userApi.loginUser(jsonObject);
        call.enqueue(callback);
    }

    public void getUser(Callback<User> callback) {
        TokenManager tokenManager = new TokenManager(mContext);
        String token = tokenManager.getToken();

        UserApi userApi = retrofit.create(UserApi.class); // UserApi 인터페이스 생성
        Call<User> call = userApi.getUser("Bearer " + token); // createUser 메소드 호출
        call.enqueue(callback);
    }
}
