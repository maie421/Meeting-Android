package com.example.meeting_android.api.user;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserApi {
    @POST("users/register")
    Call<User> createUser(@Body User user);

    @POST("login")
    Call<Authorization> loginUser(@Body JsonObject body);

    @GET("user")
    Call<User> getUser(@Header("Authorization") String token);
}
