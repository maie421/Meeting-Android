package com.example.meeting_android.api.room;

import com.example.meeting_android.api.user.Authorization;
import com.example.meeting_android.api.user.User;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RoomApi {
    @POST("rooms")
    Call<Room> createRoom(@Body Room room, @Header("Authorization") String token);

    @GET("rooms/{room_id}")
    Call<Room> getRoom(@Path("room_id") String roomId);

    @DELETE("rooms/{room_id}")
    Call<Room> deleteRoom(@Path("room_id") String roomId);
}
