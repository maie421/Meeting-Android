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
    @Multipart
    @POST("users")
    Call<ResponseBody> createUser(@Part("user") User user, @Part MultipartBody.Part image);

    @Multipart
    @POST("users/update")
    Call<User> updateUser(@Part("name") String name, @Part MultipartBody.Part image, @Header("Authorization") String token);

    @POST("login")
    Call<ResponseBody> loginUser(@Body JsonObject body);

    @GET("users/phone/{phone}")
    Call<ResponseBody> findUserByPhone(@Path("phone") String phone);

    @GET("usersAuth")
    Call<User> getUser(@Header("Authorization") String token);
}
